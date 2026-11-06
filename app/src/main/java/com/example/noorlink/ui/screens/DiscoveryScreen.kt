package com.example.noorlink.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noorlink.viewmodel.DiscoveredBeacon
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoveryScreen(
    isBengali: Boolean,
    isScanning: Boolean,
    beacons: List<DiscoveredBeacon>,
    onStartScan: () -> Unit,
    onRegisterBeacon: (DiscoveredBeacon) -> Unit,
    onManualRegister: (String, List<String>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customDeviceId by remember { mutableStateOf("ESP_SENSOR_NEW") }
    var customCapabilities by remember { mutableStateOf("temperature, humidity, relay") }
    var customIp by remember { mutableStateOf("192.168.1.185:8080") }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_ring")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Radar Scanner Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Pulsing Radar Graphic
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(if (isScanning) radarScale else 1f)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.12f))
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, CyberCyan.copy(alpha = 0.35f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(CyberCyan, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = "Scanning Radar",
                                tint = Color(0xFF090D16),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isBengali) "mDNS ও UDP অটো-ডিসকভারি" else "Auto-Discovery & Beacon Sniffer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        text = if (isBengali)
                            "লোকাল নেটওয়ার্কে সক্রিয় NoorLink নোড স্বয়ংক্রিয়ভাবে সনাক্ত করুন ও হ্যান্ডশেক পাঠান।"
                        else
                            "Automatically discover unprovisioned NoorLink nodes broadcasting on LAN / mDNS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Button(
                        onClick = onStartScan,
                        enabled = !isScanning,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF090D16)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_scan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF090D16),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "স্ক্যানিং চলছে..." else "Scanning Network...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "নেটওয়ার্ক স্ক্যান শুরু করুন" else "Scan Local Network",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Discovered Beacons Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "সনাক্তকৃত ডিভাইসসমূহ (${beacons.size})" else "Discovered Beacons (${beacons.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                FilledTonalButton(
                    onClick = onStartScan,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TechDarkSurfaceVariant,
                        contentColor = CyberCyan
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isBengali) "রিফ্রেশ" else "Refresh", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Beacon List items
        items(beacons, key = { it.deviceId }) { beacon ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("beacon_item_${beacon.deviceId}")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = beacon.deviceId,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = beacon.suggestedName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // Signal RSSI Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TechDarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = "Signal",
                                    tint = SignalGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${beacon.rssi} dBm",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = SignalGreen
                                )
                            }
                        }
                    }

                    Text(
                        text = "IP: ${beacon.ip}:${beacon.port} • Protocol: UDP/mDNS Hello",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextSecondary
                    )

                    // Capabilities
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        beacon.capabilities.forEach { cap ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ElectricIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = cap,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Register Button
                    if (beacon.isRegistered) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = SignalGreen.copy(alpha = 0.2f),
                                disabledContentColor = SignalGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isBengali) "হ্যান্ডশেক সম্পন্ন (রেজিস্টার্ড)" else "Handshake Verified")
                        }
                    } else {
                        Button(
                            onClick = { onRegisterBeacon(beacon) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color(0xFF090D16)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_beacon_${beacon.deviceId}")
                        ) {
                            Icon(imageVector = Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "হ্যালো হ্যান্ডশেক পাঠান ও রেজিস্টার করুন" else "Send Hello & Register",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Manual Device Registration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBengali) "ম্যানুয়াল ডিভাইস রেজিস্ট্রি" else "Manual Node Provisioning",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )

                    OutlinedTextField(
                        value = customDeviceId,
                        onValueChange = { customDeviceId = it },
                        label = { Text("Device ID") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_device_id_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = TechDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = customCapabilities,
                        onValueChange = { customCapabilities = it },
                        label = { Text("Capabilities (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = TechDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = customIp,
                        onValueChange = { customIp = it },
                        label = { Text("IP Address & Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = TechDarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Button(
                        onClick = {
                            val caps = customCapabilities.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onManualRegister(customDeviceId, caps, customIp)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_register_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "হ্যান্ডশেক দিয়ে রেজিস্টার করুন" else "Register with Hello Handshake",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
