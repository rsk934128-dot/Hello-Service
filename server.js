require('dotenv').config();
const http = require('http');
const fs = require('fs');
const path = require('path');
const { WebSocketServer } = require('ws');
// Import verifyToken from auth.js
const { verifyToken } = require('./auth');

const PORT = process.env.NOORLINK_PORT || process.env.SERVER_PORT || 8088;

/**
 * HTTP Server: Serves NoorLink PWABuilder Web Dashboard & Static PWA Shell
 */
const server = http.createServer((req, res) => {
    const cleanUrl = req.url.split('?')[0];
    const targetFile = cleanUrl === '/' ? 'index.html' : cleanUrl.replace(/^\/+/, '');
    const publicDir = path.join(__dirname, 'public');
    const fullPath = path.join(publicDir, targetFile);

    const mimeTypes = {
        '.html': 'text/html',
        '.js': 'application/javascript',
        '.json': 'application/manifest+json',
        '.png': 'image/png',
        '.jpg': 'image/jpeg',
        '.css': 'text/css'
    };

    // Serve static PWA files if found in public directory
    if (fs.existsSync(fullPath) && fs.statSync(fullPath).isFile()) {
        const ext = path.extname(fullPath).toLowerCase();
        res.writeHead(200, {
            'Content-Type': mimeTypes[ext] || 'application/octet-stream',
            'Service-Worker-Allowed': '/'
        });
        fs.createReadStream(fullPath).pipe(res);
    } else {
        // Fallback API Status JSON
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            service: 'NoorLink Relay Core & PWA Host',
            version: '1.0.0',
            security: 'JWT Protected Handshake',
            pwaDashboard: `http://localhost:${PORT}/`,
            timestamp: Date.now()
        }));
    }
});

const wss = new WebSocketServer({ server });
const devices = new Map(); // deviceId -> { ws, info, capabilities, role, scopes }

/**
 * Comprehensive error logger for failed authentication & handshake attempts
 * Logs Timestamp, Device ID, Failure Reason, Client IP, and Audit Details.
 */
function logAuthFailure({ deviceId = 'UNKNOWN', reason, code, clientIp = 'N/A', extra = {} }) {
    const timestamp = new Date().toISOString();
    console.error(
        `[AUTH_FAILURE] [${timestamp}] ` +
        `Device: "${deviceId}" | ` +
        `Code: [${code}] | ` +
        `Reason: ${reason} | ` +
        `Client IP: ${clientIp} | ` +
        `Details: ${JSON.stringify(extra)}`
    );
}

wss.on('connection', (ws, req) => {
    let authenticatedDeviceId = null;
    const clientIp = req.socket.remoteAddress || req.headers['x-forwarded-for'] || 'UNKNOWN_IP';

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message.toString());

            // ১. Hello Handshake (JWT যাচাইকরণ ও ডিভাইস রেজিস্ট্রি)
            if (data.type === 'hello') {
                const { deviceId, token, capabilities, info } = data;
                const safeDeviceId = (typeof deviceId === 'string' && deviceId.trim()) ? deviceId.trim() : 'UNKNOWN_DEVICE';

                // ১.১. ডিভাইস আইডি অনুপস্থিতি চেক
                if (!deviceId || typeof deviceId !== 'string') {
                    logAuthFailure({
                        deviceId: 'ANONYMOUS',
                        code: 'MISSING_DEVICE_ID',
                        reason: 'Handshake rejected: missing or invalid "deviceId" parameter',
                        clientIp,
                        extra: { rawPayload: data }
                    });

                    ws.send(JSON.stringify({
                        type: 'auth_error',
                        code: 'MISSING_DEVICE_ID',
                        message: 'Handshake requires a valid "deviceId"',
                        timestamp: new Date().toISOString()
                    }));
                    ws.close(4001, 'Missing deviceId');
                    return;
                }

                // ১.২. টোকেন অনুপস্থিতি চেক
                if (!token || typeof token !== 'string') {
                    logAuthFailure({
                        deviceId: safeDeviceId,
                        code: 'TOKEN_REQUIRED',
                        reason: `Device "${safeDeviceId}" attempted connection without providing a JWT credentials token`,
                        clientIp,
                        extra: { info: info || {} }
                    });

                    ws.send(JSON.stringify({
                        type: 'auth_error',
                        code: 'TOKEN_REQUIRED',
                        deviceId: safeDeviceId,
                        message: 'A valid JWT token is strictly required for the hello handshake',
                        timestamp: new Date().toISOString()
                    }));
                    ws.close(4001, 'Valid JWT required');
                    return;
                }

                // ১.৩. verifyToken দিয়ে সিগনেচার ও এক্সপায়ারি যাচাইকরণ
                const verification = verifyToken(token);

                if (!verification.valid) {
                    logAuthFailure({
                        deviceId: safeDeviceId,
                        code: 'INVALID_TOKEN',
                        reason: `Authentication rejected for "${safeDeviceId}": ${verification.error}`,
                        clientIp,
                        extra: {
                            tokenSnippet: token.length > 20 ? token.substring(0, 15) + '...' : token,
                            verificationError: verification.error
                        }
                    });

                    ws.send(JSON.stringify({
                        type: 'auth_error',
                        code: 'INVALID_TOKEN',
                        deviceId: safeDeviceId,
                        message: `Authentication failed: ${verification.error}`,
                        timestamp: new Date().toISOString()
                    }));
                    ws.close(4001, verification.error);
                    return;
                }

                const payload = verification.payload;

                // ১.৪. ডিভাইস আইডি মিসম্যাচ / স্পুফিং রোধ
                if (payload.role !== 'admin' && payload.sub !== safeDeviceId) {
                    logAuthFailure({
                        deviceId: safeDeviceId,
                        code: 'DEVICE_ID_MISMATCH',
                        reason: `Security violation (Spoofing): Token subject "${payload.sub}" does not match claimed deviceId "${safeDeviceId}"`,
                        clientIp,
                        extra: {
                            claimedDeviceId: safeDeviceId,
                            tokenSubject: payload.sub,
                            tokenRole: payload.role
                        }
                    });

                    ws.send(JSON.stringify({
                        type: 'auth_error',
                        code: 'DEVICE_ID_MISMATCH',
                        deviceId: safeDeviceId,
                        message: `Security violation: Token subject "${payload.sub}" does not match deviceId "${safeDeviceId}"`,
                        timestamp: new Date().toISOString()
                    }));
                    ws.close(4001, 'Device ID mismatch');
                    return;
                }

                // ১.৫. সফল হ্যান্ডশেক: সেশনে ডিভাইস রেজিস্ট্রি
                authenticatedDeviceId = safeDeviceId;
                devices.set(authenticatedDeviceId, {
                    ws,
                    info: info || {},
                    capabilities: capabilities || [],
                    role: payload.role || 'device',
                    scopes: payload.scopes || [],
                    connectedAt: Date.now(),
                    clientIp
                });

                const timestamp = new Date().toISOString();
                console.log(
                    `[HANDSHAKE_VERIFIED] [${timestamp}] Device: "${authenticatedDeviceId}" | ` +
                    `Role: "${payload.role}" | Scopes: [${(payload.scopes || []).join(', ')}] | IP: ${clientIp}`
                );

                // ক্লায়েন্টকে ওয়েলকাম বার্তা পাঠানো
                ws.send(JSON.stringify({
                    type: 'welcome',
                    status: 'connected',
                    deviceId: authenticatedDeviceId,
                    role: payload.role,
                    serverTime: Date.now(),
                    message: 'Registered Successfully (JWT Verified)'
                }));
                return;
            }

            // ২. হ্যান্ডশেক ছাড়া অন্য কোনো কমান্ড আসলে রিজেকশন লগিং
            if (!authenticatedDeviceId) {
                const timestamp = new Date().toISOString();
                logAuthFailure({
                    deviceId: 'UNAUTHENTICATED_SOCKET',
                    code: 'UNAUTHENTICATED',
                    reason: `Incoming message of type "${data.type || 'unknown'}" rejected: Device must complete "hello" handshake first`,
                    clientIp,
                    extra: { messageType: data.type }
                });

                ws.send(JSON.stringify({
                    type: 'auth_error',
                    code: 'UNAUTHENTICATED',
                    message: 'Must complete "hello" handshake with valid JWT before communicating',
                    timestamp
                }));
                return;
            }

            // ৩. Heartbeat Ping / Pong
            if (data.type === 'ping') {
                ws.send(JSON.stringify({
                    type: 'pong',
                    seq: data.seq || Date.now(),
                    deviceId: authenticatedDeviceId,
                    timestamp: Date.now()
                }));
                return;
            }

            // ৪. Relay Logic (কমান্ড রিলে)
            if (data.type === 'command') {
                const target = devices.get(data.targetId);
                if (target && target.ws.readyState === 1 /* OPEN */) {
                    target.ws.send(JSON.stringify({
                        type: 'command',
                        from: authenticatedDeviceId,
                        payload: data.payload,
                        timestamp: Date.now()
                    }));
                } else {
                    ws.send(JSON.stringify({
                        type: 'error',
                        code: 'TARGET_OFFLINE',
                        message: `Device "${data.targetId}" is not currently connected`
                    }));
                }
            }
        } catch (err) {
            const timestamp = new Date().toISOString();
            console.error(`[PARSE_ERROR] [${timestamp}] Failed to parse WebSocket packet from ${clientIp}:`, err.message);
        }
    });

    ws.on('close', () => {
        if (authenticatedDeviceId) {
            devices.delete(authenticatedDeviceId);
            const timestamp = new Date().toISOString();
            console.log(`[DISCONNECTED] [${timestamp}] Device unregistered: "${authenticatedDeviceId}"`);
        }
    });
});

server.listen(PORT, () => {
    console.log(`[SERVER_START] [${new Date().toISOString()}] NoorLink Core Relay running on port ${PORT}`);
    console.log(`[PWA_READY] PWABuilder Web Dashboard serving at: http://localhost:${PORT}/`);
});
