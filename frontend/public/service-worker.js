// SoIce MES - Service Worker
// 오프라인 지원 및 캐싱

const CACHE_NAME = 'soice-mes-v2';
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
  '/pop/work-orders',
  '/pop/scanner',
  '/pop/sop',
  '/pop/performance'
];

// API 캐시 전략
const API_CACHE_NAME = 'soice-mes-api-v2';
const API_CACHE_DURATION = 5 * 60 * 1000; // 5분

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
          if (cacheName !== CACHE_NAME && cacheName !== API_CACHE_NAME) {
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

  // API 요청 처리
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(networkFirstStrategy(request));
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

// 백그라운드 동기화 (Background Sync)
self.addEventListener('sync', (event) => {
  console.log('[Service Worker] Background sync:', event.tag);

  if (event.tag === 'sync-inventory') {
    event.waitUntil(syncInventoryData());
  }
});

async function syncInventoryData() {
  // IndexedDB에서 대기 중인 데이터 가져오기
  // 서버로 전송
  console.log('[Service Worker] Syncing inventory data...');

  // TODO: 실제 동기화 로직 구현
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
