package com.example.noorlink.network

import com.example.noorlink.model.DeviceInfo
import com.example.noorlink.model.EventType
import com.example.noorlink.model.RelayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Real OkHttp WebSocket client connecting to external/local NoorLink servers.
 * Implements the Hello handshake, ping-pong heartbeat, and command relay.
 */
class NoorLinkClient(
    private val scope: CoroutineScope,
    private val onEventReceived: (RelayEvent) -> Unit
) {
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _serverUrl = MutableStateFlow("wss://rubel-ai-assistant-coding-studio-563399721287.us-west1.run.app")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    fun connect(url: String, deviceId: String = "ANDROID_NODE", token: String = "", capabilities: List<String> = listOf("sensor", "telemetry")) {
        disconnect()
        _serverUrl.value = url
        _status.value = ConnectionStatus.CONNECTING

        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            _status.value = ConnectionStatus.ERROR
            onEventReceived(
                RelayEvent(
                    type = EventType.ERROR,
                    source = "CLIENT",
                    target = "SERVER",
                    payloadJson = "{\"error\":\"Invalid URL format: ${e.message}\"}"
                )
            )
            return
        }

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                scope.launch(Dispatchers.Main) {
                    _status.value = ConnectionStatus.CONNECTED
                    onEventReceived(
                        RelayEvent(
                            type = EventType.HELLO,
                            source = deviceId,
                            target = "SERVER",
                            payloadJson = "{\"status\":\"WebSocket Connected to $url\"}"
                        )
                    )
                    // Auto send hello handshake
                    sendHelloHandshake(
                        deviceId = deviceId,
                        token = token,
                        capabilities = capabilities,
                        info = DeviceInfo(os = "Android", version = "1.0.0")
                    )
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch(Dispatchers.Main) {
                    try {
                        val json = JSONObject(text)
                        val typeStr = json.optString("type", "unknown")
                        val eventType = when (typeStr) {
                            "welcome", "hello_ack" -> EventType.HELLO_ACK
                            "pong" -> EventType.PONG
                            "command" -> EventType.COMMAND
                            "command_ack" -> EventType.COMMAND_ACK
                            else -> EventType.RELAY
                        }

                        onEventReceived(
                            RelayEvent(
                                type = eventType,
                                source = "REMOTE_SERVER",
                                target = deviceId,
                                payloadJson = text
                            )
                        )
                    } catch (e: Exception) {
                        onEventReceived(
                            RelayEvent(
                                type = EventType.RELAY,
                                source = "REMOTE_SERVER",
                                target = deviceId,
                                payloadJson = text
                            )
                        )
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scope.launch(Dispatchers.Main) {
                    _status.value = ConnectionStatus.ERROR
                    onEventReceived(
                        RelayEvent(
                            type = EventType.ERROR,
                            source = "CLIENT",
                            target = "SERVER",
                            payloadJson = "{\"error\":\"${t.localizedMessage ?: "Connection failed"}\"}"
                        )
                    )
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                scope.launch(Dispatchers.Main) {
                    _status.value = ConnectionStatus.DISCONNECTED
                }
            }
        })
    }

    fun sendHelloHandshake(
        deviceId: String,
        token: String,
        capabilities: List<String>,
        info: DeviceInfo
    ) {
        val payload = JSONObject().apply {
            put("type", "hello")
            put("deviceId", deviceId)
            put("token", token)
            put("capabilities", org.json.JSONArray(capabilities))
            put("info", JSONObject().apply {
                put("os", info.os)
                put("version", info.version)
                put("battery", info.battery)
                put("hardware", info.hardwareModel)
            })
        }
        val sent = webSocket?.send(payload.toString()) ?: false
        if (sent) {
            onEventReceived(
                RelayEvent(
                    type = EventType.HELLO,
                    source = deviceId,
                    target = "REMOTE_SERVER",
                    payloadJson = payload.toString(),
                    isOutgoing = true
                )
            )
        }
    }

    fun sendPing(deviceId: String) {
        val payload = JSONObject().apply {
            put("type", "ping")
            put("deviceId", deviceId)
            put("timestamp", System.currentTimeMillis())
        }
        val sent = webSocket?.send(payload.toString()) ?: false
        if (sent) {
            onEventReceived(
                RelayEvent(
                    type = EventType.PING,
                    source = deviceId,
                    target = "REMOTE_SERVER",
                    payloadJson = payload.toString(),
                    isOutgoing = true
                )
            )
        }
    }

    fun sendCommand(targetId: String, command: String, params: Map<String, String> = emptyMap()) {
        val payload = JSONObject().apply {
            put("type", "command")
            put("targetId", targetId)
            put("payload", JSONObject().apply {
                put("action", command)
                params.forEach { (k, v) -> put(k, v) }
            })
        }
        val sent = webSocket?.send(payload.toString()) ?: false
        if (sent) {
            onEventReceived(
                RelayEvent(
                    type = EventType.COMMAND,
                    source = "PANEL",
                    target = targetId,
                    payloadJson = payload.toString(),
                    isOutgoing = true
                )
            )
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        webSocket = null
        _status.value = ConnectionStatus.DISCONNECTED
    }
}
