package com.example.noorlink.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noorlink.model.DeviceNode
import com.example.noorlink.model.DeviceStatus
import com.example.noorlink.model.RelayEvent
import com.example.noorlink.network.ConnectionStatus
import com.example.noorlink.ui.components.DeviceCard
import com.example.noorlink.ui.components.MetricStatCard
import com.example.noorlink.viewmodel.RelayMode
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalPurple
import com.example.ui.theme.SignalRed
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isBengali: Boolean,
    devices: Map<String, DeviceNode>,
    averageLatency: Long,
    totalHandshakes: Int,
    events: List<RelayEvent>,
    relayMode: RelayMode,
    remoteStatus: ConnectionStatus,
    onRelayModeChange: (RelayMode) -> Unit,
    onConnectRemote: (String) -> Unit,
    onDisconnectRemote: () -> Unit,
    onBroadcastPing: () -> Unit,
    onPingDevice: (String) -> Unit,
    onRelayCommand: (String, String, Map<String, String>) -> Unit,
    onToggleDeviceStatus: (String, DeviceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTargetDeviceForCommand by remember { mutableStateOf<DeviceNode?>(null) }
    var remoteWsUrlInput by remember { mutableStateOf("wss://rubel-ai-assistant-coding-studio-563399721287.us-west1.run.app") }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    val activeCount = devices.values.count { it.status == DeviceStatus.ONLINE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Relay Hub Mode Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "কানেক্টিভিটি হাব / গেটওয়ে মোড" else "Relay Core Mode",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (relayMode == RelayMode.EMBEDDED_HUB) CyberCyan.copy(alpha = 0.15f) else NeonBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (relayMode == RelayMode.EMBEDDED_HUB)
                                    (if (isBengali) "লোকাল হাব (এমবেডেড)" else "Embedded Hub")
                                else (if (isBengali) "ক্লাউড WSS" else "Cloud WSS"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (relayMode == RelayMode.EMBEDDED_HUB) CyberCyan else NeonBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = if (relayMode == RelayMode.EMBEDDED_HUB) 0 else 1,
                        containerColor = TechDarkSurfaceVariant,
                        contentColor = CyberCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (relayMode == RelayMode.EMBEDDED_HUB) 0 else 1]),
                                color = CyberCyan
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                    ) {
                        Tab(
                            selected = relayMode == RelayMode.EMBEDDED_HUB,
                            onClick = { onRelayModeChange(RelayMode.EMBEDDED_HUB) },
                            text = {
                                Text(
                                    text = if (isBengali) "লোকাল এমবেডেড হাব" else "Local Hub (Zero-Setup)",
                                    fontWeight = if (relayMode == RelayMode.EMBEDDED_HUB) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = relayMode == RelayMode.REMOTE_WEBSOCKET,
                            onClick = { onRelayModeChange(RelayMode.REMOTE_WEBSOCKET) },
                            text = {
                                Text(
                                    text = if (isBengali) "ক্লাউড রিলে (Cloud Run)" else "Cloud Relay (Cloud Run)",
                                    fontWeight = if (relayMode == RelayMode.REMOTE_WEBSOCKET) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    // Remote WebSocket input when selected
                    if (relayMode == RelayMode.REMOTE_WEBSOCKET) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = remoteWsUrlInput,
                                    onValueChange = { remoteWsUrlInput = it },
                                    label = { Text("Relay URL (WSS / WS)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("remote_ws_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = TechDarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                if (remoteStatus == ConnectionStatus.CONNECTED) {
                                    Button(
                                        onClick = onDisconnectRemote,
                                        colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
                                        modifier = Modifier.testTag("ws_disconnect_button")
                                    ) {
                                        Text(if (isBengali) "বিচ্ছিন্ন" else "Disconnect")
                                    }
                                } else {
                                    Button(
                                        onClick = { onConnectRemote(remoteWsUrlInput) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF090D16)),
                                        modifier = Modifier.testTag("ws_connect_button")
                                    ) {
                                        Text(
                                            text = if (remoteStatus == ConnectionStatus.CONNECTING) "..." else (if (isBengali) "কানেক্ট" else "Connect"),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Quick Presets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        remoteWsUrlInput = "wss://rubel-ai-assistant-coding-studio-563399721287.us-west1.run.app"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TechDarkSurfaceVariant,
                                        contentColor = CyberCyan
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rubel AI Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        remoteWsUrlInput = "ws://localhost:8088"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TechDarkSurfaceVariant,
                                        contentColor = TextSecondary
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Text("Local:8088", fontSize = 11.sp)
                                }
                            }

                            // Open in Browser action
                            FilledTonalButton(
                                onClick = {
                                    uriHandler.openUri("https://rubel-ai-assistant-coding-studio-563399721287.us-west1.run.app/")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElectricIndigo.copy(alpha = 0.2f),
                                    contentColor = NeonBlue
                                )
                            ) {
                                Text(
                                    text = if (isBengali) "রুবল এআই স্টুডিও খুলুন ↗" else "Open Rubel AI Studio in Browser ↗",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4 KPI Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (isBengali) "সক্রিয় ডিভাইস" else "Active Nodes",
                    value = "$activeCount / ${devices.size}",
                    subtitle = if (isBengali) "অনলাইন রেজিস্ট্রি" else "In Registry",
                    icon = Icons.Default.Devices,
                    accentColor = SignalGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = if (isBengali) "গড় লেটেন্সি" else "Avg Latency",
                    value = "$averageLatency ms",
                    subtitle = if (isBengali) "হার্টবিট RTT" else "Ping-Pong RTT",
                    icon = Icons.Default.Speed,
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (isBengali) "হ্যান্ডশেক কাউন্ট" else "Handshakes",
                    value = "$totalHandshakes",
                    subtitle = if (isBengali) "হ্যালো যাচাইকৃত" else "Verified Hello",
                    icon = Icons.Default.Handshake,
                    accentColor = ElectricIndigo,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = if (isBengali) "ইভেন্ট প্যাকেট" else "Relayed Events",
                    value = "${events.size}",
                    subtitle = if (isBengali) "লাইভ বাফার" else "Buffer Total",
                    icon = Icons.Default.DynamicFeed,
                    accentColor = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Toolbar: Broadcast Ping & Device Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "রেজিস্টার্ড ডিভাইসসমূহ (${devices.size})" else "Registered Nodes (${devices.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Button(
                    onClick = onBroadcastPing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.2f),
                        contentColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("broadcast_ping_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Broadcast Ping",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBengali) "পিং ব্রডকাস্ট" else "Broadcast Ping",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Device Cards list
        items(devices.values.toList(), key = { it.deviceId }) { device ->
            DeviceCard(
                device = device,
                isBengali = isBengali,
                onPing = { onPingDevice(device.deviceId) },
                onCommand = { selectedTargetDeviceForCommand = device },
                onToggleStatus = { onToggleDeviceStatus(device.deviceId, device.status) }
            )
        }

        // Empty state if no devices
        if (devices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBengali) "কোনো ডিভাইস কানেক্টেড নেই" else "No Connected Devices",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBengali) "অটো-ডিসকভারি ট্যাব থেকে ডিভাইস খুঁজুন বা যোগ করুন।" else "Scan in Auto-Discovery or switch to Device Node mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Quick Event Log preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBengali) "রিসেন্ট প্যাকেট স্ট্রিম" else "Recent Packet Stream",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )

                    events.take(4).forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TechDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (event.type.name) {
                                        "HELLO", "HELLO_ACK" -> ElectricIndigo
                                        "PING", "PONG" -> CyberCyan
                                        "COMMAND", "COMMAND_ACK" -> NeonBlue
                                        else -> SignalAmber
                                    }.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = event.type.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${event.source} ➔ ${event.target}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = TextSecondary
                                )
                            }

                            if (event.latencyMs != null) {
                                Text(
                                    text = "${event.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Command Dispatcher Modal Dialog
    selectedTargetDeviceForCommand?.let { targetDevice ->
        CommandDispatcherDialog(
            isBengali = isBengali,
            targetDevice = targetDevice,
            onDismiss = { selectedTargetDeviceForCommand = null },
            onExecute = { cmdName, params ->
                onRelayCommand(targetDevice.deviceId, cmdName, params)
                selectedTargetDeviceForCommand = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandDispatcherDialog(
    isBengali: Boolean,
    targetDevice: DeviceNode,
    onDismiss: () -> Unit,
    onExecute: (String, Map<String, String>) -> Unit
) {
    val predefinedCommands = when {
        targetDevice.capabilities.contains("camera") -> listOf("CAPTURE_FRAME", "FETCH_TELEMETRY", "REBOOT", "SET_RESOLUTION")
        targetDevice.capabilities.contains("actuator") -> listOf("TOGGLE_VALVE", "FETCH_TELEMETRY", "SET_PRESSURE_LIMIT", "REBOOT")
        targetDevice.capabilities.contains("temperature") -> listOf("FETCH_TELEMETRY", "SET_SAMPLE_RATE", "REBOOT", "CALIBRATE")
        else -> listOf("FETCH_TELEMETRY", "REBOOT", "PING", "CUSTOM")
    }

    var selectedCommand by remember { mutableStateOf(predefinedCommands.first()) }
    var customCommandText by remember { mutableStateOf("") }
    var paramKey by remember { mutableStateOf("sample_rate") }
    var paramValue by remember { mutableStateOf("2000ms") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBengali) "কমান্ড পাঠান: ${targetDevice.deviceId}" else "Relay Command to ${targetDevice.deviceId}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBengali) "কমান্ড অ্যাকশন বেছে নিন:" else "Select Command Action:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    predefinedCommands.forEach { cmd ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCommand == cmd) CyberCyan.copy(alpha = 0.2f) else TechDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedCommand == cmd) CyberCyan else TechDarkBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("select_command_$cmd")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cmd,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (selectedCommand == cmd) CyberCyan else TextPrimary
                                )
                                OutlinedButton(
                                    onClick = { selectedCommand = cmd },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (selectedCommand == cmd) CyberCyan else TextSecondary
                                    )
                                ) {
                                    Text(if (selectedCommand == cmd) "Selected" else "Pick")
                                }
                            }
                        }
                    }
                }

                if (selectedCommand == "SET_SAMPLE_RATE" || selectedCommand == "SET_PRESSURE_LIMIT") {
                    OutlinedTextField(
                        value = paramValue,
                        onValueChange = { paramValue = it },
                        label = { Text("Parameter Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCmd = if (selectedCommand == "CUSTOM") customCommandText.ifBlank { "PING" } else selectedCommand
                    val params = if (paramValue.isNotBlank()) mapOf("value" to paramValue) else emptyMap()
                    onExecute(finalCmd, params)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF090D16)
                ),
                modifier = Modifier.testTag("dialog_execute_command_button")
            ) {
                Text(
                    text = if (isBengali) "এক্সিকিউট করুন" else "Dispatch Command",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isBengali) "বাতিল" else "Cancel")
            }
        },
        containerColor = TechDarkSurface,
        titleContentColor = TextPrimary
    )
}
