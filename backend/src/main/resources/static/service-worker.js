// SoIce MES - Service Worker
// 오프라인 지원 및 캐싱 (Phase 2 Enhanced)

const CACHE_NAME = 'soice-mes-v3';
const OFFLINE_URL = '/offline.html';

// 캐시할 정적 리소스
const STATIC_ASSETS = [
  '/',
  '/offline.html',
  '/manifest.json',
  '/icons/icon-192x192.png',
  '/icons/icon-512x512.png',
  // POP routes
  '/pop',
  '/pop/home',
  '/pop/work-orders',
  '/pop/scanner',
  '/pop/sop',
  '/pop/performance',
  '/pop/work-progress'
];

// API 캐시 전략
const API_CACHE_NAME = 'soice-mes-api-v3';
const API_CACHE_DURATION = 5 * 60 * 1000; // 5분

// POP 페이지 캐시 (더 긴 유효 기간)
const POP_CACHE_NAME = 'soice-mes-pop-v3';
const POP_CACHE_DURATION = 60 * 60 * 1000; // 60분

// IndexedDB 설정
const DB_NAME = 'SoIceMES';
const DB_VERSION = 1;
const QUEUE_STORE = 'offline_queue';

// 설치 이벤트 - 정적 리소스 캐싱
self.addEventListener('install', (event) => {
  console.log('[Service Worker] Install');

  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[Service Worker] Caching static assets');
      return cache.addAll(STATIC_ASSETS);
    })
  );

  // 즉시 활성화
  self.skipWaiting();
});

// 활성화 이벤트 - 오래된 캐시 제거
self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activate');

  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME && cacheName !== API_CACHE_NAME && cacheName !== POP_CACHE_NAME) {
            console.log('[Service Worker] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );

  // 모든 클라이언트 즉시 제어
  return self.clients.claim();
});

// Fetch 이벤트 - 네트워크 요청 가로채기
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // chrome-extension, data, blob 등 비-HTTP(S) URL은 무시
  if (!url.protocol.startsWith('http')) {
    return;
  }

  // POP API 요청 처리 (특별한 오프라인 처리)
  if (url.pathname.startsWith('/api/pop/')) {
    event.respondWith(popApiStrategy(request));
    return;
  }

  // 일반 API 요청 처리
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(networkFirstStrategy(request));
    return;
  }

  // POP 페이지 - Cache First 전략 (빠른 로드)
  if (url.pathname.startsWith('/pop/')) {
    event.respondWith(popPageStrategy(request));
    return;
  }

  // 정적 리소스 처리
  event.respondWith(cacheFirstStrategy(request));
});

// 캐시 우선 전략 (정적 리소스)
async function cacheFirstStrategy(request) {
  const cache = await caches.open(CACHE_NAME);
  const cached = await cache.match(request);

  if (cached) {
    console.log('[Service Worker] Cache hit:', request.url);
    return cached;
  }

  try {
    const response = await fetch(request);

    // 성공적인 응답이면 캐시에 저장
    if (response.status === 200) {
      cache.put(request, response.clone());
    }

    return response;
  } catch (error) {
    console.error('[Service Worker] Fetch failed:', error);

    // 오프라인 페이지 반환
    if (request.destination === 'document') {
      const offlinePage = await cache.match(OFFLINE_URL);
      if (offlinePage) {
        return offlinePage;
      }
    }

    throw error;
  }
}

// 네트워크 우선 전략 (API 요청)
async function networkFirstStrategy(request) {
  const cache = await caches.open(API_CACHE_NAME);

  try {
    const response = await fetch(request);

    // 성공적인 GET 요청이면 캐시에 저장
    if (request.method === 'GET' && response.status === 200) {
      const clonedResponse = response.clone();

      // 캐시 만료 시간 설정
      const cachedResponse = new Response(clonedResponse.body, {
        status: clonedResponse.status,
        statusText: clonedResponse.statusText,
        headers: new Headers(clonedResponse.headers)
      });

      cachedResponse.headers.set('sw-cache-time', Date.now().toString());
      cache.put(request, cachedResponse);
    }

    return response;
  } catch (error) {
    console.warn('[Service Worker] Network failed, trying cache:', error);

    // 네트워크 실패 시 캐시 확인
    const cached = await cache.match(request);

    if (cached) {
      const cacheTime = cached.headers.get('sw-cache-time');
      const age = Date.now() - parseInt(cacheTime || '0', 10);

      // 캐시가 유효 기간 내라면 반환
      if (age < API_CACHE_DURATION) {
        console.log('[Service Worker] Returning cached API response:', request.url);
        return cached;
      }
    }

    throw error;
  }
}

// POP 페이지 전용 캐시 전략 (Cache First for fast loading)
async function popPageStrategy(request) {
  const cache = await caches.open(POP_CACHE_NAME);
  const cached = await cache.match(request);

  if (cached) {
    console.log('[Service Worker] POP page cache hit:', request.url);
    // 백그라운드에서 네트워크 갱신
    fetch(request).then((response) => {
      if (response.status === 200) {
        cache.put(request, response);
      }
    }).catch(() => {
      // 네트워크 실패는 무시 (이미 캐시 반환함)
    });
    return cached;
  }

  try {
    const response = await fetch(request);
    if (response.status === 200) {
      cache.put(request, response.clone());
    }
    return response;
  } catch (error) {
    console.error('[Service Worker] POP page fetch failed:', error);
    // 오프라인 페이지 반환
    const offlinePage = await cache.match(OFFLINE_URL);
    if (offlinePage) {
      return offlinePage;
    }
    throw error;
  }
}

// POP API 전용 전략 (오프라인 큐잉 지원)
async function popApiStrategy(request) {
  const url = new URL(request.url);

  // POST/PUT/PATCH/DELETE 요청은 오프라인 시 큐에 저장
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(request.method)) {
    try {
      const response = await fetch(request);
      return response;
    } catch (error) {
      console.warn('[Service Worker] POP API offline, queueing request:', url.pathname);

      // 요청을 IndexedDB 큐에 저장
      const requestData = {
        url: request.url,
        method: request.method,
        headers: Object.fromEntries(request.headers.entries()),
        body: await request.clone().text(),
        timestamp: Date.now(),
      };

      await queueOfflineRequest(requestData);

      // 백그라운드 동기화 등록
      if ('sync' in self.registration) {
        await self.registration.sync.register('sync-pop-data');
      }

      // 오프라인 응답 반환 (클라이언트에서 처리 가능하도록)
      return new Response(
        JSON.stringify({ offline: true, queued: true }),
        {
          status: 202,
          headers: { 'Content-Type': 'application/json' },
        }
      );
    }
  }

  // GET 요청은 Network First 전략
  return networkFirstStrategy(request);
}

// IndexedDB에 오프라인 요청 저장
async function queueOfflineRequest(requestData) {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onerror = () => reject(request.error);
    request.onsuccess = () => {
      const db = request.result;
      const transaction = db.transaction([QUEUE_STORE], 'readwrite');
      const store = transaction.objectStore(QUEUE_STORE);
      const addRequest = store.add(requestData);

      addRequest.onsuccess = () => resolve();
      addRequest.onerror = () => reject(addRequest.error);
    };

    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains(QUEUE_STORE)) {
        db.createObjectStore(QUEUE_STORE, { keyPath: 'timestamp' });
      }
    };
  });
}

// IndexedDB에서 대기 중인 요청 가져오기
async function getQueuedRequests() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onerror = () => reject(request.error);
    request.onsuccess = () => {
      const db = request.result;
      const transaction = db.transaction([QUEUE_STORE], 'readonly');
      const store = transaction.objectStore(QUEUE_STORE);
      const getAllRequest = store.getAll();

      getAllRequest.onsuccess = () => resolve(getAllRequest.result);
      getAllRequest.onerror = () => reject(getAllRequest.error);
    };
  });
}

// 큐에서 요청 삭제
async function removeQueuedRequest(timestamp) {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onerror = () => reject(request.error);
    request.onsuccess = () => {
      const db = request.result;
      const transaction = db.transaction([QUEUE_STORE], 'readwrite');
      const store = transaction.objectStore(QUEUE_STORE);
      const deleteRequest = store.delete(timestamp);

      deleteRequest.onsuccess = () => resolve();
      deleteRequest.onerror = () => reject(deleteRequest.error);
    };
  });
}

// 백그라운드 동기화 (Background Sync)
self.addEventListener('sync', (event) => {
  console.log('[Service Worker] Background sync:', event.tag);

  if (event.tag === 'sync-pop-data') {
    event.waitUntil(syncPOPData());
  } else if (event.tag === 'sync-inventory') {
    event.waitUntil(syncInventoryData());
  }
});

// POP 데이터 동기화
async function syncPOPData() {
  console.log('[Service Worker] Syncing POP data...');

  try {
    const queuedRequests = await getQueuedRequests();

    for (const requestData of queuedRequests) {
      try {
        const response = await fetch(requestData.url, {
          method: requestData.method,
          headers: requestData.headers,
          body: requestData.body,
        });

        if (response.ok) {
          console.log('[Service Worker] Synced request:', requestData.url);
          await removeQueuedRequest(requestData.timestamp);
        } else {
          console.warn('[Service Worker] Sync failed (non-OK response):', response.status);
        }
      } catch (error) {
        console.error('[Service Worker] Sync failed for request:', error);
        // 실패한 요청은 큐에 유지 (다음 동기화 시 재시도)
      }
    }

    console.log('[Service Worker] POP data sync complete');
  } catch (error) {
    console.error('[Service Worker] POP sync error:', error);
  }
}

async function syncInventoryData() {
  console.log('[Service Worker] Syncing inventory data...');
  // 기존 재고 동기화 로직 유지
}

// 푸시 알림
self.addEventListener('push', (event) => {
  console.log('[Service Worker] Push received:', event);

  const options = {
    body: event.data ? event.data.text() : 'New notification',
    icon: '/icons/icon-192x192.png',
    badge: '/icons/badge-72x72.png',
    vibrate: [200, 100, 200],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    }
  };

  event.waitUntil(
    self.registration.showNotification('SoIce MES', options)
  );
});

// 알림 클릭
self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] Notification clicked:', event);

  event.notification.close();

  event.waitUntil(
    clients.openWindow('/')
  );
});

// 메시지 수신 (클라이언트로부터)
self.addEventListener('message', (event) => {
  console.log('[Service Worker] Message received:', event.data);

  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }

  if (event.data && event.data.type === 'CLEAR_CACHE') {
    event.waitUntil(
      caches.keys().then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => caches.delete(cacheName))
        );
      })
    );
  }
});
