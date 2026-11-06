package com.example.noorlink.viewmodel

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noorlink.model.AuthToken
import com.example.noorlink.model.DeviceInfo
import com.example.noorlink.model.DeviceNode
import com.example.noorlink.model.DeviceStatus
import com.example.noorlink.model.EventType
import com.example.noorlink.model.RelayEvent
import com.example.noorlink.network.ConnectionStatus
import com.example.noorlink.network.LocalRelayHub
import com.example.noorlink.network.NoorLinkClient
import com.example.noorlink.security.AuthManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    DASHBOARD,
    DISCOVERY,
    DEVICE_MODE,
    HEARTBEAT,
    CONSOLE
}

enum class RelayMode {
    EMBEDDED_HUB,
    REMOTE_WEBSOCKET
}

data class DiscoveredBeacon(
    val deviceId: String,
    val suggestedName: String,
    val ip: String,
    val port: Int,
    val capabilities: List<String>,
    val rssi: Int,
    val isRegistered: Boolean = false
)

class NoorLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val localHub = LocalRelayHub(viewModelScope)
    private val remoteClient: NoorLinkClient

    // Language state: false = English, true = Bengali (বাংলা)
    private val _isBengali = MutableStateFlow(true)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Relay Mode (Local vs Remote WebSocket)
    private val _relayMode = MutableStateFlow(RelayMode.EMBEDDED_HUB)
    val relayMode: StateFlow<RelayMode> = _relayMode.asStateFlow()

    // Remote WS connection status & URL
    val remoteStatus: StateFlow<ConnectionStatus>
    val remoteUrl: StateFlow<String>

    // Devices & Stats
    val devices: StateFlow<Map<String, DeviceNode>> = localHub.devices
    val totalHandshakes: StateFlow<Int> = localHub.totalHandshakes
    val averageLatency: StateFlow<Long> = localHub.averageLatency
    val events: StateFlow<List<RelayEvent>> = localHub.events

    // Auto-Discovery beacons
    private val _discoveredBeacons = MutableStateFlow<List<DiscoveredBeacon>>(emptyList())
    val discoveredBeacons: StateFlow<List<DiscoveredBeacon>> = _discoveredBeacons.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Device Node Mode (This Android device as an IoT/Edge Node)
    private val _nodeDeviceId = MutableStateFlow("ANDROID_NODE_${Build.MODEL.replace(" ", "_").take(10)}")
    val nodeDeviceId: StateFlow<String> = _nodeDeviceId.asStateFlow()

    private val _nodeCapabilities = MutableStateFlow(listOf("battery_meter", "motion_sensor", "ambient_light", "remote_notify"))
    val nodeCapabilities: StateFlow<List<String>> = _nodeCapabilities.asStateFlow()

    private val _isNodeRegistered = MutableStateFlow(false)
    val isNodeRegistered: StateFlow<Boolean> = _isNodeRegistered.asStateFlow()

    private val _nodeHeartbeatCount = MutableStateFlow(0)
    val nodeHeartbeatCount: StateFlow<Int> = _nodeHeartbeatCount.asStateFlow()

    private val _nodeLastCommand = MutableStateFlow<String?>(null)
    val nodeLastCommand: StateFlow<String?> = _nodeLastCommand.asStateFlow()

    // JWT Security Console
    private val _jwtToken = MutableStateFlow<AuthToken?>(null)
    val jwtToken: StateFlow<AuthToken?> = _jwtToken.asStateFlow()

    private val _tokenSecret = MutableStateFlow("NOORLINK_SECRET_HMAC_KEY_2026")
    val tokenSecret: StateFlow<String> = _tokenSecret.asStateFlow()

    // Heartbeat settings
    private val _pingIntervalSeconds = MutableStateFlow(3)
    val pingIntervalSeconds: StateFlow<Int> = _pingIntervalSeconds.asStateFlow()

    private val _isPingingActive = MutableStateFlow(true)
    val isPingingActive: StateFlow<Boolean> = _isPingingActive.asStateFlow()

    private var autoPingJob: Job? = null

    init {
        remoteClient = NoorLinkClient(viewModelScope) { event ->
            // Forward event to local hub log if in remote mode
            // (or handle remote welcome / ack)
        }
        remoteStatus = remoteClient.status
        remoteUrl = remoteClient.serverUrl

        // Generate initial JWT for admin/panel
        generateJwt("PANEL_OPERATOR", "admin", listOf("relay:all", "command:exec", "heartbeat:monitor"))

        // Seed some nearby discovery beacons
        seedDiscoveredBeacons()

        // Start heartbeat monitoring
        startAutoPinging()
    }

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setRelayMode(mode: RelayMode) {
        _relayMode.value = mode
    }

    // Dashboard Actions
    fun broadcastPing() {
        if (_relayMode.value == RelayMode.EMBEDDED_HUB) {
            localHub.broadcastPing()
        } else {
            remoteClient.sendPing(_nodeDeviceId.value)
        }
    }

    fun sendPingTo(deviceId: String) {
        localHub.sendPingToDevice(deviceId)
    }

    fun relayCommand(targetId: String, command: String, params: Map<String, String> = emptyMap()) {
        if (_relayMode.value == RelayMode.EMBEDDED_HUB) {
            localHub.relayCommand(targetId, command, params)
        } else {
            remoteClient.sendCommand(targetId, command, params)
        }
    }

    fun toggleDeviceOnline(deviceId: String, currentStatus: DeviceStatus) {
        val nextStatus = if (currentStatus == DeviceStatus.ONLINE) DeviceStatus.OFFLINE else DeviceStatus.ONLINE
        localHub.setDeviceStatus(deviceId, nextStatus)
    }

    fun deleteDevice(deviceId: String) {
        localHub.removeDevice(deviceId)
    }

    fun clearLogs() {
        localHub.clearLogs()
    }

    // Remote WebSocket Connect / Disconnect
    fun connectRemote(url: String) {
        val token = _jwtToken.value?.rawToken ?: ""
        remoteClient.connect(url, _nodeDeviceId.value, token, _nodeCapabilities.value)
    }

    fun disconnectRemote() {
        remoteClient.disconnect()
    }

    // Auto-Discovery actions
    fun startScan() {
        _isScanning.value = true
        viewModelScope.launch {
            delay(1500)
            seedDiscoveredBeacons()
            _isScanning.value = false
        }
    }

    private fun seedDiscoveredBeacons() {
        _discoveredBeacons.value = listOf(
            DiscoveredBeacon(
                deviceId = "ESP32_LIGHT_7A",
                suggestedName = "Living Room Ambient Light",
                ip = "192.168.1.140",
                port = 8080,
                capabilities = listOf("rgb_light", "dimmer", "timer"),
                rssi = -46
            ),
            DiscoveredBeacon(
                deviceId = "AIR_QUALITY_B2",
                suggestedName = "Office PM2.5 & CO2 Monitor",
                ip = "192.168.1.148",
                port = 8080,
                capabilities = listOf("co2_sensor", "pm25", "temperature"),
                rssi = -62
            ),
            DiscoveredBeacon(
                deviceId = "RPI_BACKUP_RELAY",
                suggestedName = "NoorLink Secondary Relay",
                ip = "192.168.1.250",
                port = 8080,
                capabilities = listOf("gateway", "storage", "mDNS"),
                rssi = -38
            )
        )
    }

    fun registerDiscoveredBeacon(beacon: DiscoveredBeacon) {
        val info = DeviceInfo(
            os = "MicroPython / RTOS",
            version = "1.0.4",
            hardwareModel = beacon.deviceId.take(5),
            battery = 95,
            ipAddress = "${beacon.ip}:${beacon.port}"
        )
        val token = AuthManager.createJwtToken(beacon.deviceId).rawToken
        localHub.processHelloHandshake(
            deviceId = beacon.deviceId,
            token = token,
            capabilities = beacon.capabilities,
            info = info
        )

        _discoveredBeacons.value = _discoveredBeacons.value.map {
            if (it.deviceId == beacon.deviceId) it.copy(isRegistered = true) else it
        }
    }

    // Device Node Mode Actions (Android Device acting as Edge Node)
    fun registerThisDeviceAsNode() {
        val context = getApplication<Application>()
        val batteryLevel = getBatteryLevel(context)
        val info = DeviceInfo(
            os = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
            version = "2.0.0",
            hardwareModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            battery = batteryLevel,
            ipAddress = "192.168.1.75"
        )

        val token = AuthManager.createJwtToken(_nodeDeviceId.value, role = "device").rawToken
        localHub.processHelloHandshake(
            deviceId = _nodeDeviceId.value,
            token = token,
            capabilities = _nodeCapabilities.value,
            info = info,
            isLocalDevice = true
        )
        _isNodeRegistered.value = true
    }

    fun addCapability(capability: String) {
        if (capability.isNotBlank() && !_nodeCapabilities.value.contains(capability)) {
            _nodeCapabilities.value = _nodeCapabilities.value + capability.trim().lowercase()
        }
    }

    fun removeCapability(capability: String) {
        _nodeCapabilities.value = _nodeCapabilities.value - capability
    }

    fun sendNodePingPongHeartbeat() {
        _nodeHeartbeatCount.value += 1
        localHub.sendPingToDevice(_nodeDeviceId.value)
    }

    // JWT Security Actions
    fun generateJwt(deviceId: String, role: String, scopes: List<String>) {
        val token = AuthManager.createJwtToken(
            deviceId = deviceId,
            role = role,
            scopes = scopes,
            secret = _tokenSecret.value
        )
        _jwtToken.value = token
    }

    fun updateSecret(newSecret: String) {
        _tokenSecret.value = newSecret
        _jwtToken.value?.let { curr ->
            _jwtToken.value = AuthManager.parseAndValidateToken(curr.rawToken, newSecret)
        }
    }

    // Heartbeat loop
    fun setPingInterval(seconds: Int) {
        _pingIntervalSeconds.value = seconds
        startAutoPinging()
    }

    fun toggleAutoPinging(active: Boolean) {
        _isPingingActive.value = active
        if (active) startAutoPinging() else autoPingJob?.cancel()
    }

    private fun startAutoPinging() {
        autoPingJob?.cancel()
        autoPingJob = viewModelScope.launch {
            while (isActive && _isPingingActive.value) {
                delay(_pingIntervalSeconds.value * 1000L)
                val allDevices = localHub.devices.value.values.filter { it.status == DeviceStatus.ONLINE }
                if (allDevices.isNotEmpty()) {
                    val pick = allDevices.random()
                    localHub.sendPingToDevice(pick.deviceId)
                }
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
    }
}
