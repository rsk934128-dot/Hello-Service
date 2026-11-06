package com.example.noorlink.network

import com.example.noorlink.model.DeviceInfo
import com.example.noorlink.model.DeviceNode
import com.example.noorlink.model.DeviceStatus
import com.example.noorlink.model.EventType
import com.example.noorlink.model.RelayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

/**
 * Embedded NoorLink Relay Core Hub
 * Implements the server-side handshake protocol, session registry, heartbeat ping/pong,
 * and command relay matching the server.js architecture.
 */
class LocalRelayHub(
    private val scope: CoroutineScope
) {
    private val _devices = MutableStateFlow<Map<String, DeviceNode>>(emptyMap())
    val devices: StateFlow<Map<String, DeviceNode>> = _devices.asStateFlow()

    private val _events = MutableStateFlow<List<RelayEvent>>(emptyList())
    val events: StateFlow<List<RelayEvent>> = _events.asStateFlow()

    private val _totalHandshakes = MutableStateFlow(0)
    val totalHandshakes: StateFlow<Int> = _totalHandshakes.asStateFlow()

    private val _averageLatency = MutableStateFlow(18L)
    val averageLatency: StateFlow<Long> = _averageLatency.asStateFlow()

    private var heartbeatJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        // Seed default nodes representing IoT and microservices
        bootstrapDefaultDevices()
        startHeartbeatLoop()
        startTelemetryLoop()
    }

    private fun bootstrapDefaultDevices() {
        val seed = listOf(
            DeviceNode(
                deviceId = "SENSOR_01",
                name = "DHT22 Climate Sensor",
                capabilities = listOf("temperature", "humidity", "telemetry", "control"),
                status = DeviceStatus.ONLINE,
                info = DeviceInfo(
                    os = "FreeRTOS",
                    version = "1.2.0",
                    hardwareModel = "ESP32-WROOM",
                    battery = 94,
                    ipAddress = "192.168.1.101",
                    uptimeSeconds = 34500L
                ),
                token = "jwt.sensor01.valid",
                lastPingMs = 14,
                avgLatencyMs = 16,
                latencyHistory = listOf(14, 18, 15, 17, 14, 16, 15),
                telemetry = mapOf("temp_c" to "26.4°C", "humidity" to "61%", "signal_rssi" to "-58 dBm")
            ),
            DeviceNode(
                deviceId = "CAM_GATE_02",
                name = "Gate Security Cam 1080p",
                capabilities = listOf("camera", "video_stream", "night_vision", "motion_alert"),
                status = DeviceStatus.ONLINE,
                info = DeviceInfo(
                    os = "Linux (Embedded)",
                    version = "2.1.0",
                    hardwareModel = "ARM Cortex-A7",
                    battery = 100, // Mains powered
                    ipAddress = "192.168.1.102",
                    uptimeSeconds = 129000L
                ),
                token = "jwt.cam02.valid",
                lastPingMs = 28,
                avgLatencyMs = 31,
                latencyHistory = listOf(28, 35, 30, 27, 33, 29, 31),
                telemetry = mapOf("fps" to "30", "bitrate" to "2.4 Mbps", "motion" to "idle")
            ),
            DeviceNode(
                deviceId = "VALVE_ACTUATOR_03",
                name = "Smart Irrigation Valve",
                capabilities = listOf("actuator", "flow_meter", "failsafe"),
                status = DeviceStatus.ONLINE,
                info = DeviceInfo(
                    os = "Zephyr RTOS",
                    version = "3.4.0",
                    hardwareModel = "STM32F4",
                    battery = 88,
                    ipAddress = "192.168.1.103",
                    uptimeSeconds = 72000L
                ),
                token = "jwt.valve03.valid",
                lastPingMs = 21,
                avgLatencyMs = 24,
                latencyHistory = listOf(20, 22, 26, 21, 23, 25, 21),
                telemetry = mapOf("valve_state" to "CLOSED", "flow_rate" to "0.0 L/m", "pressure" to "42 PSI")
            ),
            DeviceNode(
                deviceId = "CORE_GATEWAY_NODE",
                name = "NoorLink Gateway Hub",
                capabilities = listOf("gateway", "mDNS_broadcast", "packet_forwarding", "mesh_sync"),
                status = DeviceStatus.ONLINE,
                info = DeviceInfo(
                    os = "Linux Alpine",
                    version = "1.0.0",
                    hardwareModel = "Raspberry Pi 4B",
                    battery = 100,
                    ipAddress = "192.168.1.1",
                    uptimeSeconds = 450000L
                ),
                token = "jwt.gateway.valid",
                lastPingMs = 8,
                avgLatencyMs = 9,
                latencyHistory = listOf(7, 9, 8, 10, 8, 7, 9),
                telemetry = mapOf("active_sockets" to "4", "packets_sec" to "128", "cpu_load" to "14%")
            )
        )

        val map = seed.associateBy { it.deviceId }
        _devices.value = map
        _totalHandshakes.value = map.size

        logEvent(
            type = EventType.HELLO_ACK,
            source = "CORE_RELAY",
            target = "ALL",
            payload = "{\"status\": \"NoorLink Service Active\", \"version\": \"1.0.0\", \"activeNodes\": 4}"
        )
    }

    /**
     * Process an incoming Hello Handshake
     * Exactly mimics:
     * if (data.type === 'hello') {
     *   devices.set(data.deviceId, { ws, info: data.info, capabilities });
     *   ws.send(JSON.stringify({ type: 'welcome', status: 'connected' }));
     * }
     */
    fun processHelloHandshake(
        deviceId: String,
        token: String,
        capabilities: List<String>,
        info: DeviceInfo,
        isLocalDevice: Boolean = false
    ): DeviceNode {
        val node = DeviceNode(
            deviceId = deviceId,
            name = if (isLocalDevice) "This Android Device (Node)" else "Device $deviceId",
            capabilities = capabilities,
            status = DeviceStatus.ONLINE,
            info = info,
            token = token,
            lastPingMs = 12,
            avgLatencyMs = 15,
            latencyHistory = listOf(12, 14, 15, 11, 16),
            lastSeenTimestamp = System.currentTimeMillis(),
            telemetry = if (isLocalDevice) mapOf("battery" to "${info.battery}%", "os" to info.os) else emptyMap(),
            isLocalDevice = isLocalDevice
        )

        _devices.value = _devices.value + (deviceId to node)
        _totalHandshakes.value = _totalHandshakes.value + 1

        logEvent(
            type = EventType.HELLO,
            source = deviceId,
            target = "CORE_RELAY",
            payload = "{\"type\":\"hello\",\"deviceId\":\"$deviceId\",\"capabilities\":${capabilities.joinToString(prefix="[", postfix="]") { "\"$it\"" }}}"
        )

        logEvent(
            type = EventType.HELLO_ACK,
            source = "CORE_RELAY",
            target = deviceId,
            payload = "{\"type\":\"welcome\",\"status\":\"connected\",\"message\":\"Registered Successfully\"}"
        )

        return node
    }

    /**
     * Process Ping to calculate Round-Trip Time (RTT) Heartbeat
     */
    fun sendPingToDevice(deviceId: String, onComplete: ((Long) -> Unit)? = null) {
        val currentDevice = _devices.value[deviceId] ?: return
        val originTime = System.currentTimeMillis()

        scope.launch {
            // Simulate realistic network delay (5-45ms)
            val networkDelay = if (currentDevice.isLocalDevice) 4L else (12L + Random.nextLong(25))
            delay(networkDelay)

            val latency = System.currentTimeMillis() - originTime
            val updatedHistory = (currentDevice.latencyHistory + latency.toInt()).takeLast(10)
            val newAvg = updatedHistory.average().toLong()

            val updatedDevice = currentDevice.copy(
                lastPingMs = latency,
                avgLatencyMs = newAvg,
                latencyHistory = updatedHistory,
                lastSeenTimestamp = System.currentTimeMillis(),
                status = DeviceStatus.ONLINE
            )
            _devices.value = _devices.value + (deviceId to updatedDevice)
            recalculateAverageLatency()

            logEvent(
                type = EventType.PONG,
                source = deviceId,
                target = "PANEL",
                payload = "{\"type\":\"pong\",\"seq\":${System.currentTimeMillis()},\"latencyMs\":$latency}",
                latencyMs = latency
            )

            onComplete?.invoke(latency)
        }
    }

    /**
     * Broadcast Ping to all connected devices
     */
    fun broadcastPing() {
        logEvent(
            type = EventType.PING,
            source = "PANEL",
            target = "ALL",
            payload = "{\"type\":\"ping_broadcast\",\"timestamp\":${System.currentTimeMillis()}}"
        )
        _devices.value.keys.forEach { devId ->
            sendPingToDevice(devId)
        }
    }

    /**
     * Relay a Command from Panel to Device
     * Matches:
     * if (data.type === 'command') {
     *   target.ws.send(JSON.stringify(data.payload));
     * }
     */
    fun relayCommand(
        targetId: String,
        commandName: String,
        params: Map<String, String> = emptyMap()
    ) {
        val device = _devices.value[targetId]
        if (device == null) {
            logEvent(
                type = EventType.ERROR,
                source = "CORE_RELAY",
                target = "PANEL",
                payload = "{\"error\":\"Device $targetId not found in registry\"}"
            )
            return
        }

        logEvent(
            type = EventType.COMMAND,
            source = "PANEL",
            target = targetId,
            payload = "{\"type\":\"command\",\"action\":\"$commandName\",\"params\":$params}"
        )

        // Execute action effects on telemetry
        scope.launch {
            delay(40)
            val updatedTelemetry = device.telemetry.toMutableMap()
            when (commandName) {
                "REBOOT" -> {
                    updatedTelemetry["uptime"] = "0s (Just rebooted)"
                    updatedTelemetry["status"] = "REBOOTED_OK"
                }
                "FETCH_TELEMETRY" -> {
                    updatedTelemetry["last_fetch"] = "${System.currentTimeMillis()}"
                    updatedTelemetry["refreshed"] = "true"
                }
                "TOGGLE_VALVE" -> {
                    val curr = updatedTelemetry["valve_state"] ?: "CLOSED"
                    updatedTelemetry["valve_state"] = if (curr == "OPEN") "CLOSED" else "OPEN"
                    updatedTelemetry["flow_rate"] = if (curr == "CLOSED") "14.2 L/m" else "0.0 L/m"
                }
                "CAPTURE_FRAME" -> {
                    updatedTelemetry["last_snapshot"] = "frame_${System.currentTimeMillis()}.jpg (1080p)"
                }
                "SET_SAMPLE_RATE" -> {
                    updatedTelemetry["sample_rate"] = params["rate"] ?: "1000ms"
                }
            }

            val updatedNode = device.copy(
                telemetry = updatedTelemetry,
                lastSeenTimestamp = System.currentTimeMillis()
            )
            _devices.value = _devices.value + (targetId to updatedNode)

            logEvent(
                type = EventType.COMMAND_ACK,
                source = targetId,
                target = "PANEL",
                payload = "{\"type\":\"command_ack\",\"status\":\"EXECUTED\",\"action\":\"$commandName\"}"
            )
        }
    }

    /**
     * Remove or mark a device offline
     */
    fun setDeviceStatus(deviceId: String, status: DeviceStatus) {
        val node = _devices.value[deviceId] ?: return
        _devices.value = _devices.value + (deviceId to node.copy(status = status))
        logEvent(
            type = EventType.RELAY,
            source = "CORE_RELAY",
            target = "PANEL",
            payload = "{\"type\":\"device_status_change\",\"deviceId\":\"$deviceId\",\"status\":\"$status\"}"
        )
    }

    fun removeDevice(deviceId: String) {
        _devices.value = _devices.value - deviceId
        logEvent(
            type = EventType.RELAY,
            source = "CORE_RELAY",
            target = "PANEL",
            payload = "{\"type\":\"device_unregistered\",\"deviceId\":\"$deviceId\"}"
        )
    }

    fun clearLogs() {
        _events.value = emptyList()
    }

    private fun logEvent(
        type: EventType,
        source: String,
        target: String,
        payload: String,
        latencyMs: Long? = null
    ) {
        val event = RelayEvent(
            type = type,
            source = source,
            target = target,
            payloadJson = payload,
            latencyMs = latencyMs,
            isOutgoing = (source == "PANEL")
        )
        _events.value = (listOf(event) + _events.value).take(60) // Keep latest 60
    }

    private fun recalculateAverageLatency() {
        val list = _devices.value.values.map { it.avgLatencyMs }.filter { it > 0 }
        if (list.isNotEmpty()) {
            _averageLatency.value = list.average().toLong()
        }
    }

    private fun startHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(6000) // Pulse every 6 seconds
                val keys = _devices.value.keys.toList()
                if (keys.isNotEmpty()) {
                    val randomDevice = keys.random()
                    sendPingToDevice(randomDevice)
                }
            }
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(4000)
                val currentMap = _devices.value.toMutableMap()
                val sensor = currentMap["SENSOR_01"]
                if (sensor != null && sensor.status == DeviceStatus.ONLINE) {
                    val t = 24.0 + (Random.nextDouble() * 5.0)
                    val h = 55 + Random.nextInt(15)
                    val updatedTelem = sensor.telemetry + mapOf(
                        "temp_c" to String.format("%.1f°C", t),
                        "humidity" to "$h%"
                    )
                    currentMap["SENSOR_01"] = sensor.copy(telemetry = updatedTelem)
                    _devices.value = currentMap
                }
            }
        }
    }
}
