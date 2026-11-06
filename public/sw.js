/**
 * NoorLink Service Worker (PWABuilder Compliant)
 * Provides offline caching, network-first fallbacks, and real-time push support.
 */

const CACHE_NAME = 'noorlink-pwa-v1.0.0';
const PRECACHE_ASSETS = [
    '/',
    '/index.html',
    '/manifest.json',
    '/sw.js'
];

// 1. Install Event: Cache Core Offline Shell
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            console.log('[Service Worker] Pre-caching offline shell');
            return cache.addAll(PRECACHE_ASSETS);
        }).then(() => self.skipWaiting())
    );
});

// 2. Activate Event: Clean up old cache versions
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keyList) => {
            return Promise.all(
                keyList.map((key) => {
                    if (key !== CACHE_NAME) {
                        console.log('[Service Worker] Removing old cache:', key);
                        return caches.delete(key);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// 3. Fetch Event: Network-first for dynamic content, cache-first fallback for offline support
self.addEventListener('fetch', (event) => {
    // Skip WebSocket and non-GET requests
    if (event.request.url.startsWith('ws://') || 
        event.request.url.startsWith('wss://') || 
        event.request.method !== 'GET') {
        return;
    }

    event.respondWith(
        fetch(event.request)
            .then((networkResponse) => {
                // If response is valid, update the cache
                if (networkResponse && networkResponse.status === 200) {
                    const responseClone = networkResponse.clone();
                    caches.open(CACHE_NAME).then((cache) => {
                        cache.put(event.request, responseClone);
                    });
                }
                return networkResponse;
            })
            .catch(() => {
                // Network failed: Serve from offline cache
                return caches.match(event.request).then((cachedResponse) => {
                    if (cachedResponse) {
                        return cachedResponse;
                    }
                    if (event.request.headers.get('accept').includes('text/html')) {
                        return caches.match('/index.html');
                    }
                });
            })
    );
});

// 4. Push Notification Support for Device Disconnect / Alarms
self.addEventListener('push', (event) => {
    const data = event.data ? event.data.json() : { title: 'NoorLink Alert', body: 'Node state changed' };
    event.waitUntil(
        self.registration.showNotification(data.title, {
            body: data.body,
            icon: '/icons/icon-192.png',
            badge: '/icons/icon-192.png',
            vibrate: [100, 50, 100],
            data: { url: '/' }
        })
    );
});

self.addEventListener('notificationclick', (event) => {
    event.notification.close();
    event.waitUntil(
        clients.matchAll({ type: 'window' }).then((clientList) => {
            for (const client of clientList) {
                if (client.url === '/' && 'focus' in client) {
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow('/');
            }
        })
    );
});
