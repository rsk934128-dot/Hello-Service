/**
 * NoorLink Security Layer (server/auth.js)
 * Implements JWT-based security middleware using the 'jsonwebtoken' library
 * to verify device tokens during the NoorLink handshake process.
 */

const jwt = require('jsonwebtoken');

// Load environment secrets or use default secure fallback key
const JWT_SECRET = process.env.JWT_SECRET || 'NOORLINK_SECRET_HMAC_KEY_2026';
const DEFAULT_EXPIRATION = '2h'; // 2 hours validity

/**
 * 1. Generates a signed JWT token using jsonwebtoken
 *
 * @param {Object} options
 * @param {string} options.deviceId - Unique device ID (e.g. 'SENSOR_01', 'GATE_CAM_02')
 * @param {string} [options.role='device'] - Role: 'device', 'panel', or 'admin'
 * @param {Array<string>} [options.scopes] - Permitted scopes (e.g. ['telemetry:read', 'heartbeat:ping', 'command:exec'])
 * @param {string|number} [options.expiresIn='2h'] - Expiration time (e.g. '2h', '24h', 7200)
 * @param {string} [options.secret] - Optional secret override
 * @returns {string} Signed JWT string
 */
function generateToken({ deviceId, role = 'device', scopes = ['telemetry:read', 'heartbeat:ping'], expiresIn = DEFAULT_EXPIRATION, secret = JWT_SECRET }) {
    if (!deviceId) {
        throw new Error('generateToken requires a valid "deviceId"');
    }

    const payload = {
        sub: deviceId,
        role: role,
        scopes: scopes
    };

    return jwt.sign(payload, secret, {
        algorithm: 'HS256',
        expiresIn: expiresIn,
        issuer: 'noorlink-core'
    });
}

/**
 * 2. Verifies and decodes a device JWT token using jsonwebtoken
 *
 * @param {string} token - The raw JWT token string
 * @param {string} [secret=JWT_SECRET] - The secret key used for HMAC SHA256 validation
 * @returns {{ valid: boolean, payload?: Object, error?: string }}
 */
function verifyToken(token, secret = JWT_SECRET) {
    if (!token || typeof token !== 'string') {
        return { valid: false, error: 'Token is missing or empty' };
    }

    try {
        const decoded = jwt.verify(token, secret, {
            algorithms: ['HS256'],
            issuer: 'noorlink-core'
        });

        return {
            valid: true,
            payload: decoded
        };
    } catch (err) {
        if (err instanceof jwt.TokenExpiredError) {
            return {
                valid: false,
                error: 'Token has expired',
                expiredAt: err.expiredAt
            };
        } else if (err instanceof jwt.JsonWebTokenError) {
            return {
                valid: false,
                error: `Invalid signature or malformed token: ${err.message}`
            };
        } else {
            return {
                valid: false,
                error: `Token verification failed: ${err.message}`
            };
        }
    }
}

/**
 * 3. NoorLink Handshake Security Middleware
 * Protects the 'hello' handshake message by verifying the device's JWT token,
 * ensuring device ID binding and role authorization.
 *
 * @param {Object} msg - The parsed JSON incoming message from WebSocket
 * @param {WebSocket} ws - Active WebSocket client connection
 * @returns {{ authenticated: boolean, deviceId?: string, session?: Object, error?: string, code?: string }}
 */
function authenticateHandshake(msg, ws) {
    const { deviceId, token, capabilities } = msg;

    // A. Validate presence of deviceId
    if (!deviceId || typeof deviceId !== 'string' || !deviceId.trim()) {
        const errMsg = 'Handshake rejected: missing or invalid "deviceId"';
        sendAuthError(ws, 'MISSING_DEVICE_ID', errMsg);
        return { authenticated: false, code: 'MISSING_DEVICE_ID', error: errMsg };
    }

    // B. Validate presence of JWT token
    if (!token || typeof token !== 'string' || !token.trim()) {
        const errMsg = `Handshake rejected: Device "${deviceId}" provided no JWT security token`;
        sendAuthError(ws, 'TOKEN_REQUIRED', errMsg);
        return { authenticated: false, code: 'TOKEN_REQUIRED', error: errMsg };
    }

    // C. Verify token signature and expiration via jsonwebtoken
    const verification = verifyToken(token);
    if (!verification.valid) {
        const errMsg = `Authentication failed for "${deviceId}": ${verification.error}`;
        sendAuthError(ws, 'INVALID_TOKEN', errMsg);
        return { authenticated: false, code: 'INVALID_TOKEN', error: errMsg };
    }

    const claims = verification.payload;

    // D. Strict Device ID Binding: Ensure token subject matches claimed device ID
    // (Prevents token replay and device impersonation attacks)
    if (claims.role !== 'admin' && claims.sub !== deviceId.trim()) {
        const errMsg = `Security violation: Token subject "${claims.sub}" does not match claimed deviceId "${deviceId}"`;
        sendAuthError(ws, 'DEVICE_ID_MISMATCH', errMsg);
        return { authenticated: false, code: 'DEVICE_ID_MISMATCH', error: errMsg };
    }

    // E. Authentication Successful
    return {
        authenticated: true,
        deviceId: deviceId.trim(),
        session: {
            deviceId: deviceId.trim(),
            role: claims.role || 'device',
            scopes: claims.scopes || [],
            capabilities: capabilities || [],
            authenticatedAt: Date.now()
        }
    };
}

/**
 * Sends a structured authentication error packet to the WebSocket client
 */
function sendAuthError(ws, code, message) {
    if (ws && ws.readyState === 1 /* OPEN */) {
        ws.send(JSON.stringify({
            type: 'auth_error',
            code: code,
            message: message,
            timestamp: new Date().toISOString()
        }));
    }
}

module.exports = {
    JWT_SECRET,
    generateToken,
    verifyToken,
    authenticateHandshake,
    sendAuthError
};
