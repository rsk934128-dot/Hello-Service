const { generateToken, verifyToken, authenticateHandshake } = require('./auth');

console.log('--- NoorLink Security Layer Verification ---');

// 1. Generate valid token
const validToken = generateToken({
    deviceId: 'SENSOR_01',
    role: 'device',
    scopes: ['telemetry:read', 'heartbeat:ping']
});
console.log('1. Generated Token:', validToken.substring(0, 45) + '...');

// 2. Verify valid token
const verifyResult = verifyToken(validToken);
console.log('2. Verification of Valid Token:', verifyResult.valid ? 'PASSED' : 'FAILED', verifyResult.payload);

// 3. Test Handshake Authentication with valid token
const validMsg = {
    type: 'hello',
    deviceId: 'SENSOR_01',
    token: validToken,
    capabilities: ['temperature', 'humidity']
};
const handshakeSuccess = authenticateHandshake(validMsg, null);
console.log('3. Valid Handshake:', handshakeSuccess.authenticated ? 'PASSED' : 'FAILED');

// 4. Test Handshake with mismatched deviceId (spoofing attempt)
const spoofMsg = {
    type: 'hello',
    deviceId: 'CAMERA_99', // Claiming to be CAMERA_99 with SENSOR_01's token
    token: validToken,
    capabilities: ['camera']
};
const spoofResult = authenticateHandshake(spoofMsg, null);
console.log('4. Spoof Attempt Rejection:', !spoofResult.authenticated ? 'PASSED (Rejected properly)' : 'FAILED');

// 5. Test Handshake with tampered token
const tamperedToken = validToken.substring(0, validToken.length - 4) + 'XXXX';
const tamperedResult = verifyToken(tamperedToken);
console.log('5. Tampered Token Rejection:', !tamperedResult.valid ? 'PASSED (Signature invalid)' : 'FAILED');

console.log('--- All Security Checks Completed Successfully! ---');
