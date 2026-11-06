package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noorlink.network.ConnectionStatus
import com.example.noorlink.ui.components.NoorLinkTopAppBar
import com.example.noorlink.ui.screens.ConsoleScreen
import com.example.noorlink.ui.screens.DashboardScreen
import com.example.noorlink.ui.screens.DeviceNodeScreen
import com.example.noorlink.ui.screens.DiscoveryScreen
import com.example.noorlink.ui.screens.HeartbeatScreen
import com.example.noorlink.viewmodel.AppTab
import com.example.noorlink.viewmodel.NoorLinkViewModel
import com.example.noorlink.viewmodel.RelayMode
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NoorLinkApp()
            }
        }
    }
}

data class NavItem(
    val tab: AppTab,
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun NoorLinkApp(
    viewModel: NoorLinkViewModel = viewModel()
) {
    val isBengali by viewModel.isBengali.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val relayMode by viewModel.relayMode.collectAsStateWithLifecycle()
    val remoteStatus by viewModel.remoteStatus.collectAsStateWithLifecycle()

    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val averageLatency by viewModel.averageLatency.collectAsStateWithLifecycle()
    val totalHandshakes by viewModel.totalHandshakes.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()

    val beacons by viewModel.discoveredBeacons.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    val nodeDeviceId by viewModel.nodeDeviceId.collectAsStateWithLifecycle()
    val nodeCapabilities by viewModel.nodeCapabilities.collectAsStateWithLifecycle()
    val isNodeRegistered by viewModel.isNodeRegistered.collectAsStateWithLifecycle()
    val nodeHeartbeatCount by viewModel.nodeHeartbeatCount.collectAsStateWithLifecycle()
    val nodeLastCommand by viewModel.nodeLastCommand.collectAsStateWithLifecycle()

    val jwtToken by viewModel.jwtToken.collectAsStateWithLifecycle()
    val secret by viewModel.tokenSecret.collectAsStateWithLifecycle()

    val isPingingActive by viewModel.isPingingActive.collectAsStateWithLifecycle()
    val pingIntervalSeconds by viewModel.pingIntervalSeconds.collectAsStateWithLifecycle()

    // Handle back button to return to Dashboard if on a sub-tab
    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.setTab(AppTab.DASHBOARD)
    }

    val navItems = listOf(
        NavItem(AppTab.DASHBOARD, "Dashboard", "ড্যাশবোর্ড", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem(AppTab.DISCOVERY, "Discovery", "ডিসকভারি", Icons.Default.Radar, "nav_discovery"),
        NavItem(AppTab.DEVICE_MODE, "Device Node", "ডিভাইস", Icons.Default.PhoneAndroid, "nav_device_mode"),
        NavItem(AppTab.HEARTBEAT, "Heartbeat", "হার্টবিট", Icons.Default.Favorite, "nav_heartbeat"),
        NavItem(AppTab.CONSOLE, "Console", "কনসোল", Icons.Default.Terminal, "nav_console")
    )

    val relayConnected = if (relayMode == RelayMode.EMBEDDED_HUB) true else (remoteStatus == ConnectionStatus.CONNECTED)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TechDarkBackground,
        topBar = {
            NoorLinkTopAppBar(
                isBengali = isBengali,
                onToggleLanguage = { viewModel.toggleLanguage() },
                relayConnected = relayConnected
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TechDarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val selected = currentTab == item.tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.titleEn,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isBengali) item.titleBn else item.titleEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF090D16),
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    isBengali = isBengali,
                    devices = devices,
                    averageLatency = averageLatency,
                    totalHandshakes = totalHandshakes,
                    events = events,
                    relayMode = relayMode,
                    remoteStatus = remoteStatus,
                    onRelayModeChange = { viewModel.setRelayMode(it) },
                    onConnectRemote = { viewModel.connectRemote(it) },
                    onDisconnectRemote = { viewModel.disconnectRemote() },
                    onBroadcastPing = { viewModel.broadcastPing() },
                    onPingDevice = { viewModel.sendPingTo(it) },
                    onRelayCommand = { target, cmd, params -> viewModel.relayCommand(target, cmd, params) },
                    onToggleDeviceStatus = { id, st -> viewModel.toggleDeviceOnline(id, st) }
                )

                AppTab.DISCOVERY -> DiscoveryScreen(
                    isBengali = isBengali,
                    isScanning = isScanning,
                    beacons = beacons,
                    onStartScan = { viewModel.startScan() },
                    onRegisterBeacon = { viewModel.registerDiscoveredBeacon(it) },
                    onManualRegister = { id, caps, ip ->
                        viewModel.registerDiscoveredBeacon(
                            com.example.noorlink.viewmodel.DiscoveredBeacon(
                                deviceId = id,
                                suggestedName = "Manual $id",
                                ip = ip.substringBefore(":"),
                                port = ip.substringAfter(":", "8080").toIntOrNull() ?: 8080,
                                capabilities = caps,
                                rssi = -50
                            )
                        )
                    }
                )

                AppTab.DEVICE_MODE -> DeviceNodeScreen(
                    isBengali = isBengali,
                    nodeDeviceId = nodeDeviceId,
                    capabilities = nodeCapabilities,
                    isRegistered = isNodeRegistered,
                    heartbeatCount = nodeHeartbeatCount,
                    lastCommand = nodeLastCommand,
                    onRegisterDevice = { viewModel.registerThisDeviceAsNode() },
                    onAddCapability = { viewModel.addCapability(it) },
                    onRemoveCapability = { viewModel.removeCapability(it) },
                    onSendManualPingPong = { viewModel.sendNodePingPongHeartbeat() }
                )

                AppTab.HEARTBEAT -> HeartbeatScreen(
                    isBengali = isBengali,
                    devices = devices,
                    averageLatency = averageLatency,
                    isPingingActive = isPingingActive,
                    pingIntervalSeconds = pingIntervalSeconds,
                    onTogglePinging = { viewModel.toggleAutoPinging(it) },
                    onIntervalChange = { viewModel.setPingInterval(it) },
                    onSendSinglePing = { viewModel.sendPingTo(it) }
                )

                AppTab.CONSOLE -> ConsoleScreen(
                    isBengali = isBengali,
                    events = events,
                    jwtToken = jwtToken,
                    secret = secret,
                    onGenerateJwt = { id, role, scopes -> viewModel.generateJwt(id, role, scopes) },
                    onUpdateSecret = { viewModel.updateSecret(it) },
                    onClearLogs = { viewModel.clearLogs() }
                )
            }
        }
    }
}
