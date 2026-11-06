const { verifyToken, generateToken } = require('./auth');

console.log('--- Testing Enhanced Server Handshake Error Logging ---');

function logAuthFailure({ deviceId = 'UNKNOWN', reason, code, clientIp = '127.0.0.1', extra = {} }) {
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

function processHandshakeMessage(data, clientIp = '192.168.1.105') {
    const { deviceId, token, capabilities, info } = data;
    const safeDeviceId = (typeof deviceId === 'string' && deviceId.trim()) ? deviceId.trim() : 'UNKNOWN_DEVICE';

    if (!deviceId || typeof deviceId !== 'string') {
        logAuthFailure({
            deviceId: 'ANONYMOUS',
            code: 'MISSING_DEVICE_ID',
            reason: 'Handshake rejected: missing or invalid "deviceId" parameter',
            clientIp,
            extra: { rawPayload: data }
        });
        return { success: false, code: 'MISSING_DEVICE_ID' };
    }

    if (!token || typeof token !== 'string') {
        logAuthFailure({
            deviceId: safeDeviceId,
            code: 'TOKEN_REQUIRED',
            reason: `Device "${safeDeviceId}" attempted connection without providing a JWT credentials token`,
            clientIp,
            extra: { info: info || {} }
        });
        return { success: false, code: 'TOKEN_REQUIRED' };
    }

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
        return { success: false, code: 'INVALID_TOKEN' };
    }

    const payload = verification.payload;
    if (payload.role !== 'admin' && payload.sub !== safeDeviceId) {
        logAuthFailure({
            deviceId: safeDeviceId,
            code: 'DEVICE_ID_MISMATCH',
            reason: `Security violation (Spoofing): Token subject "${payload.sub}" does not match claimed deviceId "${safeDeviceId}"`,
            clientIp,
            extra: { claimedDeviceId: safeDeviceId, tokenSubject: payload.sub }
        });
        return { success: false, code: 'DEVICE_ID_MISMATCH' };
    }

    console.log(`[HANDSHAKE_VERIFIED] [${new Date().toISOString()}] Device: "${safeDeviceId}" successfully authenticated.`);
    return { success: true };
}

// 1. Missing deviceId
console.log('\nCase 1: Anonymous / Missing Device ID:');
processHandshakeMessage({ type: 'hello' });

// 2. Missing Token
console.log('\nCase 2: Missing Token for SENSOR_GATEWAY:');
processHandshakeMessage({ type: 'hello', deviceId: 'SENSOR_GATEWAY' });

// 3. Invalid/Tampered Token
console.log('\nCase 3: Tampered Token for CAM_01:');
processHandshakeMessage({ type: 'hello', deviceId: 'CAM_01', token: 'invalid.signature.header' });

// 4. Device ID Spoofing
console.log('\nCase 4: Spoofing Attempt (Token for NODE_A used by NODE_B):');
const tokenForNodeA = generateToken({ deviceId: 'NODE_A' });
processHandshakeMessage({ type: 'hello', deviceId: 'NODE_B', token: tokenForNodeA });

// 5. Successful Handshake
console.log('\nCase 5: Valid Handshake for NODE_A:');
processHandshakeMessage({ type: 'hello', deviceId: 'NODE_A', token: tokenForNodeA });
