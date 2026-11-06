package com.example.noorlink.model

enum class DeviceStatus {
    ONLINE,
    STANDBY,
    OFFLINE
}

data class DeviceInfo(
    val os: String = "Android",
    val version: String = "1.0.0",
    val hardwareModel: String = "Node-X",
    val battery: Int = 100,
    val ipAddress: String = "127.0.0.1",
    val uptimeSeconds: Long = 0L
)

data class DeviceNode(
    val deviceId: String,
    val name: String,
    val capabilities: List<String>,
    val status: DeviceStatus = DeviceStatus.ONLINE,
    val info: DeviceInfo = DeviceInfo(),
    val token: String = "",
    val lastPingMs: Long = 18L,
    val avgLatencyMs: Long = 22L,
    val latencyHistory: List<Int> = listOf(22, 19, 25, 21, 18, 24, 20),
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val telemetry: Map<String, String> = emptyMap(),
    val isLocalDevice: Boolean = false
)

enum class EventType {
    HELLO,
    HELLO_ACK,
    PING,
    PONG,
    COMMAND,
    COMMAND_ACK,
    RELAY,
    DISCOVERY,
    ERROR
}

data class RelayEvent(
    val id: String = java.util.UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val type: EventType,
    val source: String,
    val target: String = "BROADCAST",
    val payloadJson: String,
    val latencyMs: Long? = null,
    val isOutgoing: Boolean = false
)

data class HelloHandshakeMessage(
    val type: String = "hello",
    val deviceId: String,
    val token: String,
    val capabilities: List<String>,
    val info: DeviceInfo
)

data class HeartbeatPingMessage(
    val type: String = "ping",
    val seq: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String
)

data class HeartbeatPongMessage(
    val type: String = "pong",
    val seq: Long,
    val originTimestamp: Long,
    val responderTimestamp: Long = System.currentTimeMillis(),
    val deviceId: String
)

data class CommandMessage(
    val type: String = "command",
    val targetId: String,
    val command: String,
    val payload: Map<String, String> = emptyMap()
)

data class AuthToken(
    val rawToken: String,
    val deviceId: String,
    val role: String, // "device", "panel", "admin"
    val scopes: List<String>,
    val issuedAt: Long,
    val expiresAt: Long,
    val isValid: Boolean = true
)
