const { generateToken, verifyToken } = require('../auth');

module.exports = (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method === 'POST') {
        const { action, deviceId, role, scopes, token } = req.body || {};

        if (action === 'generate') {
            if (!deviceId) {
                return res.status(400).json({ error: 'deviceId is required' });
            }
            try {
                const newToken = generateToken({
                    deviceId,
                    role: role || 'device',
                    scopes: scopes || ['telemetry:read', 'heartbeat:ping']
                });
                return res.status(200).json({ success: true, token: newToken });
            } catch (err) {
                return res.status(500).json({ error: err.message });
            }
        }

        if (action === 'verify') {
            if (!token) {
                return res.status(400).json({ error: 'token is required' });
            }
            const result = verifyToken(token);
            return res.status(result.valid ? 200 : 401).json(result);
        }
    }

    return res.status(200).json({
        usage: 'POST with { action: "generate"|"verify", deviceId, token }'
    });
};
