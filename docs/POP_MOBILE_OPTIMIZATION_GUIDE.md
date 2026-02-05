# POP Mobile Optimization Guide

**SoIce MES - Mobile & PWA Optimization**
**Version**: 1.0
**Author**: Moon Myung-seop
**Last Updated**: 2025-02-05

---

## Table of Contents

1. [Introduction](#introduction)
2. [PWA Configuration](#pwa-configuration)
3. [Touch Interface Design](#touch-interface-design)
4. [Offline Mode](#offline-mode)
5. [Performance Optimization](#performance-optimization)
6. [Device Testing](#device-testing)
7. [Deployment Checklist](#deployment-checklist)
8. [Troubleshooting](#troubleshooting)

---

## Introduction

### Target Devices

The POP system is optimized for factory floor mobile devices:

**Primary Targets**:
- Industrial tablets (7-10 inch, Android/Windows)
- iPad (9.7 inch and above)
- Large smartphones (6+ inch)

**Operating Systems**:
- Android 8.0+
- iOS 13.0+
- Windows 10+

**Browsers**:
- Chrome/Edge (Chromium) - Recommended
- Safari (iOS)
- Samsung Internet

### Design Philosophy

1. **Touch-First**: All interactions optimized for touch, not mouse
2. **Large Targets**: Minimum 44x44px touch targets
3. **High Visibility**: Works in bright factory lighting
4. **Offline-Ready**: Functions without network connection
5. **PWA**: Installable app experience

---

## PWA Configuration

### Manifest File

Location: `/public/manifest.json`

```json
{
  "name": "SoIce MES POP",
  "short_name": "POP",
  "description": "Point of Production System for factory operators",
  "start_url": "/pop",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "orientation": "any",
  "icons": [
    {
      "src": "/icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-96x96.png",
      "sizes": "96x96",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-128x128.png",
      "sizes": "128x128",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-144x144.png",
      "sizes": "144x144",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-152x152.png",
      "sizes": "152x152",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-384x384.png",
      "sizes": "384x384",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ],
  "shortcuts": [
    {
      "name": "작업 지시",
      "short_name": "작업",
      "description": "작업지시 목록 보기",
      "url": "/pop/work-orders",
      "icons": [{ "src": "/icons/work-orders.png", "sizes": "96x96" }]
    },
    {
      "name": "스캐너",
      "short_name": "스캔",
      "description": "바코드 스캔",
      "url": "/pop/scanner",
      "icons": [{ "src": "/icons/scanner.png", "sizes": "96x96" }]
    },
    {
      "name": "성과",
      "short_name": "성과",
      "description": "생산 성과 보기",
      "url": "/pop/performance",
      "icons": [{ "src": "/icons/performance.png", "sizes": "96x96" }]
    }
  ],
  "categories": ["productivity", "business"],
  "screenshots": [
    {
      "src": "/screenshots/work-orders.png",
      "sizes": "1280x720",
      "type": "image/png"
    },
    {
      "src": "/screenshots/scanner.png",
      "sizes": "1280x720",
      "type": "image/png"
    }
  ]
}
```

### Service Worker Registration

Location: `/src/index.tsx` or `/src/main.tsx`

```typescript
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/service-worker.js')
      .then(registration => {
        console.log('SW registered:', registration);

        // Check for updates every hour
        setInterval(() => {
          registration.update();
        }, 3600000);
      })
      .catch(error => {
        console.error('SW registration failed:', error);
      });
  });
}
```

### Install Prompt

```typescript
let deferredPrompt: any;

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;

  // Show install button
  showInstallButton();
});

function installApp() {
  if (!deferredPrompt) return;

  deferredPrompt.prompt();

  deferredPrompt.userChoice.then((choiceResult: any) => {
    if (choiceResult.outcome === 'accepted') {
      console.log('User accepted install');
    }
    deferredPrompt = null;
  });
}
```

### Update Notification

```typescript
navigator.serviceWorker.addEventListener('controllerchange', () => {
  // Show update available notification
  showUpdateNotification();
});

function showUpdateNotification() {
  if (confirm('새 버전이 있습니다. 업데이트 하시겠습니까?')) {
    window.location.reload();
  }
}
```

---

## Touch Interface Design

### Touch Target Sizes

**Minimum Sizes** (According to WCAG 2.1):
- Primary actions: **80x80px** (optimal for gloved hands)
- Secondary actions: **60x60px**
- Tertiary actions: **44x44px** (minimum)

**Implementation Example**:

```tsx
// Primary button - Work Start
<Button
  variant="contained"
  color="success"
  onClick={onStart}
  sx={{
    minHeight: 80,
    minWidth: 200,
    fontSize: '1.5rem',
    fontWeight: 'bold'
  }}
>
  작업 시작
</Button>

// Touch quantity input
<IconButton
  onClick={handleIncrement}
  sx={{
    width: 80,
    height: 80,
    bgcolor: 'success.main',
    color: 'white',
    fontSize: 32
  }}
>
  <AddIcon fontSize="inherit" />
</IconButton>
```

### Spacing Between Targets

Minimum **8px gap** between touch targets to prevent mis-taps:

```tsx
<Box sx={{ display: 'flex', gap: 2 }}> {/* gap: 2 = 16px */}
  <Button sx={{ minHeight: 60, flex: 1 }}>Pass</Button>
  <Button sx={{ minHeight: 60, flex: 1 }}>Fail</Button>
</Box>
```

### Typography for Mobile

**Font Sizes**:
- Body text: **16px minimum** (prevents auto-zoom on iOS)
- Headings: **24px+**
- Numbers (quantities): **32px+**
- Buttons: **18px+**

**Line Height**: 1.5 minimum for readability

```css
body {
  font-size: 16px;
  line-height: 1.5;
  -webkit-text-size-adjust: 100%; /* Prevent auto-zoom */
}

.quantity-display {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}
```

### Color Contrast

**WCAG AA Compliance**: 4.5:1 ratio for normal text

**High Visibility Colors** (works in bright factory light):
- Success: `#2e7d32` (dark green)
- Error: `#c62828` (dark red)
- Warning: `#f57c00` (dark orange)
- Info: `#0277bd` (dark blue)

**Avoid**:
- Light colors (yellow, light blue) - hard to see in bright light
- Low contrast (gray on white) - poor visibility

### Haptic Feedback

```typescript
function triggerHaptic() {
  if ('vibrate' in navigator) {
    navigator.vibrate(10); // 10ms vibration
  }
}

// Use on button press
<Button onClick={() => {
  triggerHaptic();
  handleAction();
}}>
  Record
</Button>
```

**Vibration Patterns**:
- Single tap: `10ms`
- Success: `[10, 50, 10]`
- Error: `[50, 30, 50, 30, 50]`
- Warning: `[30, 50, 30]`

---

## Offline Mode

### Service Worker Caching Strategy

**Cache First** (for POP pages):
```javascript
async function popPageStrategy(request) {
  const cache = await caches.open(POP_CACHE_NAME);
  const cached = await cache.match(request);

  if (cached) {
    // Return from cache, update in background
    fetch(request).then(response => {
      if (response.status === 200) {
        cache.put(request, response);
      }
    }).catch(() => {});
    return cached;
  }

  // Fetch from network, cache result
  const response = await fetch(request);
  if (response.status === 200) {
    cache.put(request, response.clone());
  }
  return response;
}
```

**Network First** (for API calls):
```javascript
async function networkFirstStrategy(request) {
  try {
    const response = await fetch(request);
    const cache = await caches.open(API_CACHE_NAME);
    cache.put(request, response.clone());
    return response;
  } catch (error) {
    const cached = await caches.match(request);
    if (cached) return cached;
    throw error;
  }
}
```

### Offline Queue Management

**Using IndexedDB** (via localforage):

```typescript
import localforage from 'localforage';

const offlineQueue = localforage.createInstance({
  name: 'SoIceMES',
  storeName: 'offline_queue'
});

// Queue request when offline
async function queueRequest(request: any) {
  const id = Date.now() + '_' + Math.random();
  await offlineQueue.setItem(id, {
    ...request,
    timestamp: Date.now(),
    retryCount: 0
  });
}

// Sync when online
async function syncQueue() {
  const keys = await offlineQueue.keys();

  for (const key of keys) {
    const request = await offlineQueue.getItem(key);
    try {
      await sendToServer(request);
      await offlineQueue.removeItem(key);
    } catch (error) {
      // Increment retry count
      request.retryCount++;
      if (request.retryCount < 3) {
        await offlineQueue.setItem(key, request);
      } else {
        // Move to failed queue
        console.error('Max retries exceeded:', key);
      }
    }
  }
}

// Listen for online event
window.addEventListener('online', syncQueue);
```

### Offline UI Indicators

```tsx
function OfflineIndicator() {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [queuedItems, setQueuedItems] = useState(0);

  useEffect(() => {
    function handleOnline() {
      setIsOnline(true);
      syncQueue();
    }

    function handleOffline() {
      setIsOnline(false);
    }

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (isOnline && queuedItems === 0) return null;

  return (
    <Alert severity={isOnline ? 'info' : 'warning'}>
      {isOnline
        ? `동기화 중... (${queuedItems}건 대기)`
        : `오프라인 모드 (${queuedItems}건 대기)`}
    </Alert>
  );
}
```

---

## Performance Optimization

### Code Splitting

```typescript
// Lazy load POP pages
const POPWorkOrderPage = lazy(() => import('./pages/POP/POPWorkOrderPage'));
const POPScannerPage = lazy(() => import('./pages/POP/POPScannerPage'));
const POPSOPPage = lazy(() => import('./pages/POP/POPSOPPage'));

// Routes with Suspense
<Suspense fallback={<LoadingSpinner />}>
  <Routes>
    <Route path="/pop/work-orders" element={<POPWorkOrderPage />} />
    <Route path="/pop/scanner" element={<POPScannerPage />} />
    <Route path="/pop/sop" element={<POPSOPPage />} />
  </Routes>
</Suspense>
```

### Image Optimization

**Responsive Images**:
```html
<img
  src="/images/icon-512.png"
  srcset="
    /images/icon-192.png 192w,
    /images/icon-384.png 384w,
    /images/icon-512.png 512w
  "
  sizes="(max-width: 600px) 192px, 384px"
  alt="Icon"
  loading="lazy"
/>
```

**WebP with fallback**:
```html
<picture>
  <source srcset="/images/photo.webp" type="image/webp" />
  <source srcset="/images/photo.jpg" type="image/jpeg" />
  <img src="/images/photo.jpg" alt="Photo" />
</picture>
```

### Bundle Size Optimization

**Vite Configuration**:
```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['react', 'react-dom', 'react-router-dom'],
          'mui': ['@mui/material', '@mui/icons-material'],
          'websocket': ['@stomp/stompjs', 'sockjs-client']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
});
```

### Performance Budget

**Target Metrics** (Lighthouse scores):
- Performance: **90+**
- Accessibility: **95+**
- Best Practices: **95+**
- SEO: **90+**
- PWA: **100**

**Bundle Sizes**:
- Initial JS: **< 200 KB** (gzipped)
- Initial CSS: **< 50 KB** (gzipped)
- Total page size: **< 500 KB**

**Runtime Performance**:
- First Contentful Paint (FCP): **< 1.5s**
- Largest Contentful Paint (LCP): **< 2.5s**
- Time to Interactive (TTI): **< 3.0s**
- Cumulative Layout Shift (CLS): **< 0.1**
- First Input Delay (FID): **< 100ms**

### Network Optimization

**HTTP/2 Server Push**:
```nginx
# nginx.conf
location / {
  http2_push /static/css/main.css;
  http2_push /static/js/main.js;
}
```

**Compression**:
```nginx
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types
  text/plain
  text/css
  text/javascript
  application/javascript
  application/json;
```

**CDN for Static Assets**:
```typescript
const CDN_URL = import.meta.env.VITE_CDN_URL || '';

function getAssetUrl(path: string) {
  return CDN_URL + path;
}

<img src={getAssetUrl('/icons/icon-192.png')} />
```

---

## Device Testing

### Test Matrix

| Device Type | Screen Size | OS | Browser | Priority |
|-------------|-------------|----|---------|----|
| Industrial Tablet | 7-10" | Android 10+ | Chrome | High |
| iPad | 9.7"-12.9" | iOS 14+ | Safari | High |
| Samsung Galaxy Tab | 10.1" | Android 11+ | Samsung Internet | High |
| Windows Tablet | 10-12" | Windows 10+ | Edge | Medium |
| Large Phone | 6-7" | Android/iOS | Chrome/Safari | Medium |
| Rugged Tablet | 8-10" | Android 9+ | Chrome | High |

### Testing Checklist

#### Visual Testing
- [ ] Touch target sizes ≥ 44x44px
- [ ] Text readable in bright light
- [ ] Color contrast ≥ 4.5:1
- [ ] No horizontal scrolling
- [ ] Proper viewport scaling
- [ ] Status bar styling correct

#### Functional Testing
- [ ] All buttons responsive to touch
- [ ] Quantity input works with touch
- [ ] Barcode scanner activates camera
- [ ] Haptic feedback works (if supported)
- [ ] Offline mode functions correctly
- [ ] WebSocket reconnects after network loss

#### Performance Testing
- [ ] Page load < 3 seconds
- [ ] Smooth 60 FPS scrolling
- [ ] No janky animations
- [ ] Minimal battery drain
- [ ] Efficient memory usage

#### Offline Testing
- [ ] App loads without network
- [ ] Offline indicator shows
- [ ] Actions queue correctly
- [ ] Sync works on reconnect
- [ ] Failed sync handled gracefully

### Testing Tools

**Lighthouse CI**:
```bash
npm install -g @lhci/cli
lhci autorun --config=lighthouserc.js
```

**Remote Debugging**:
- Chrome DevTools: `chrome://inspect`
- Safari Web Inspector: Develop menu on Mac
- Weinre for older devices

**Performance Monitoring**:
```typescript
// Use Performance API
const navigationTiming = performance.getEntriesByType('navigation')[0];
console.log('Page load time:', navigationTiming.duration);

// Report to analytics
if ('sendBeacon' in navigator) {
  navigator.sendBeacon('/api/metrics', JSON.stringify({
    fcp: performance.getEntriesByName('first-contentful-paint')[0].startTime,
    lcp: largestContentfulPaint,
    fid: firstInputDelay
  }));
}
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] Run production build: `npm run build`
- [ ] Test PWA installation locally
- [ ] Verify service worker registration
- [ ] Check manifest.json is accessible
- [ ] Validate all icons are present (72-512px)
- [ ] Test offline mode functionality
- [ ] Run Lighthouse audit (score 90+)
- [ ] Test on 3+ target devices
- [ ] Verify HTTPS configuration

### Deployment

- [ ] Enable HTTPS (required for PWA)
- [ ] Configure proper CORS headers
- [ ] Set up HTTP/2
- [ ] Enable gzip/brotli compression
- [ ] Configure CDN for static assets
- [ ] Set cache headers correctly
- [ ] Deploy service worker to root `/`
- [ ] Verify manifest served with correct MIME type

### Post-Deployment

- [ ] Test PWA installation on real devices
- [ ] Verify offline mode works
- [ ] Check WebSocket connectivity
- [ ] Monitor performance metrics
- [ ] Test barcode scanning with real barcodes
- [ ] Verify push notifications (if enabled)
- [ ] Check for console errors
- [ ] Test update mechanism

### HTTPS Configuration

**Required Headers**:
```nginx
# nginx.conf
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;

# PWA specific
location /manifest.json {
  add_header Content-Type application/manifest+json;
  add_header Cache-Control "public, max-age=604800";
}

location /service-worker.js {
  add_header Content-Type application/javascript;
  add_header Cache-Control "no-cache";
}
```

### Cache Headers

```nginx
# Static assets - long cache
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
  expires 1y;
  add_header Cache-Control "public, immutable";
}

# HTML - no cache
location ~* \.html$ {
  expires -1;
  add_header Cache-Control "no-store, must-revalidate";
}

# API - no cache
location /api/ {
  add_header Cache-Control "no-cache";
}
```

---

## Troubleshooting

### Common Issues

#### 1. PWA Not Installing

**Symptoms**: Install banner doesn't show

**Causes**:
- Not served over HTTPS
- Manifest.json not found or invalid
- Service worker not registered
- Missing required icons

**Solutions**:
```bash
# Check manifest
curl -I https://your-domain.com/manifest.json

# Check service worker
curl -I https://your-domain.com/service-worker.js

# Validate manifest
npx pwa-asset-generator validate manifest.json

# Check in Chrome DevTools
# Application > Manifest
# Application > Service Workers
```

#### 2. Offline Mode Not Working

**Symptoms**: App doesn't work offline

**Causes**:
- Service worker not activated
- Caching strategy incorrect
- IndexedDB not supported
- Quota exceeded

**Solutions**:
```javascript
// Check service worker status
navigator.serviceWorker.getRegistrations().then(registrations => {
  console.log('Registrations:', registrations);
});

// Check cache
caches.keys().then(keys => {
  console.log('Cache keys:', keys);
});

// Check IndexedDB quota
navigator.storage.estimate().then(estimate => {
  console.log('Storage used:', estimate.usage);
  console.log('Storage quota:', estimate.quota);
});
```

#### 3. Touch Targets Too Small

**Symptoms**: Difficult to tap buttons

**Solutions**:
```tsx
// Increase button size
<Button sx={{ minHeight: 60, minWidth: 120 }}>
  Button
</Button>

// Add padding
<IconButton sx={{ p: 3 }}>
  <Icon />
</IconButton>

// Increase gap between elements
<Box sx={{ display: 'flex', gap: 2 }}>
  <Button>Button 1</Button>
  <Button>Button 2</Button>
</Box>
```

#### 4. Performance Issues

**Symptoms**: Slow page load, janky scrolling

**Solutions**:
```typescript
// Enable React Profiler
import { Profiler } from 'react';

<Profiler id="POPWorkOrderPage" onRender={onRenderCallback}>
  <POPWorkOrderPage />
</Profiler>

function onRenderCallback(
  id, phase, actualDuration, baseDuration, startTime, commitTime
) {
  console.log(`${id} (${phase}) took ${actualDuration}ms`);
}

// Use React.memo for expensive components
const WorkOrderCard = React.memo(({ workOrder }) => {
  // Component code
});

// Virtualize long lists
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={workOrders.length}
  itemSize={100}
>
  {({ index, style }) => (
    <div style={style}>
      <WorkOrderCard workOrder={workOrders[index]} />
    </div>
  )}
</FixedSizeList>
```

#### 5. Barcode Scanner Not Working

**Symptoms**: Camera doesn't activate

**Causes**:
- Not served over HTTPS
- Camera permission denied
- Unsupported browser

**Solutions**:
```typescript
// Check camera permission
navigator.permissions.query({ name: 'camera' as PermissionName })
  .then(result => {
    console.log('Camera permission:', result.state);
  });

// Request camera access
navigator.mediaDevices.getUserMedia({ video: true })
  .then(stream => {
    console.log('Camera access granted');
    stream.getTracks().forEach(track => track.stop());
  })
  .catch(error => {
    console.error('Camera access denied:', error);
  });

// Provide manual input fallback
{cameraError && (
  <TextField
    label="바코드 수동 입력"
    value={manualBarcode}
    onChange={e => setManualBarcode(e.target.value)}
  />
)}
```

---

## Best Practices Summary

### Design
- ✅ Touch targets ≥ 44x44px (optimal 80x80px)
- ✅ Font size ≥ 16px (prevents auto-zoom)
- ✅ Color contrast ≥ 4.5:1
- ✅ Spacing between targets ≥ 8px
- ✅ Haptic feedback on actions

### Performance
- ✅ Initial load < 3 seconds
- ✅ 60 FPS scrolling
- ✅ Code splitting for routes
- ✅ Lazy load images
- ✅ Minimize bundle size

### Offline
- ✅ Service worker registered
- ✅ Cache First for static assets
- ✅ Network First for API
- ✅ IndexedDB for offline queue
- ✅ Sync on reconnection

### PWA
- ✅ HTTPS required
- ✅ Valid manifest.json
- ✅ Icons 72-512px
- ✅ Service worker at root
- ✅ Installable on home screen

### Testing
- ✅ Test on 3+ real devices
- ✅ Bright light visibility
- ✅ Gloved hand usability
- ✅ Offline functionality
- ✅ Battery efficiency

---

**Version**: 1.0
**Maintained by**: SoftIce Co., Ltd.
**Contact**: dev@softice.co.kr
