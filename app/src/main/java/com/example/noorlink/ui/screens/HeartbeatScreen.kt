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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.noorlink.model.DeviceNode
import com.example.noorlink.model.DeviceStatus
import com.example.noorlink.ui.components.HeartbeatPulseRadar
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeartbeatScreen(
    isBengali: Boolean,
    devices: Map<String, DeviceNode>,
    averageLatency: Long,
    isPingingActive: Boolean,
    pingIntervalSeconds: Int,
    onTogglePinging: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onSendSinglePing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Live Radar & Heartbeat Pulse Center
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
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(if (isPingingActive) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, CyberCyan.copy(alpha = 0.4f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(CyberCyan, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heartbeat Pulse",
                                tint = Color(0xFF090D16),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isBengali) "লাইভ হার্টবিট ও হেলথ চেক (Ping-Pong)" else "Continuous Heartbeat & Health Check",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        text = if (isBengali)
                            "সার্ভার ও ক্লায়েন্টের মধ্যে নিয়মিত {\"type\":\"ping\"} ও {\"type\":\"pong\"} আদান-প্রদান করে সংযোগের প্রাপ্যতা (Availability) নিশ্চিত করা হচ্ছে।"
                        else
                            "Real-time bidirectional ping-pong telemetry ensuring zero silent disconnects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Auto-Ping toggle switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TechDarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPingingActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = if (isPingingActive) SignalGreen else SignalAmber
                            )
                            Text(
                                text = if (isBengali) "স্বয়ংক্রিয় হার্টবিট সাইকেল" else "Auto Heartbeat Pinging",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Switch(
                            checked = isPingingActive,
                            onCheckedChange = onTogglePinging,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF090D16),
                                checkedTrackColor = CyberCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = TechDarkSurface
                            ),
                            modifier = Modifier.testTag("auto_ping_switch")
                        )
                    }

                    // Ping Interval Selector Chips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isBengali) "হার্টবিট ফ্রিকোয়েন্সি: প্রতি $pingIntervalSeconds সেকেন্ডে" else "Heartbeat Interval: Every ${pingIntervalSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 3, 5, 10).forEach { sec ->
                                FilterChip(
                                    selected = pingIntervalSeconds == sec,
                                    onClick = { onIntervalChange(sec) },
                                    label = { Text("${sec}s") },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberCyan,
                                        selectedLabelColor = Color(0xFF090D16),
                                        containerColor = TechDarkSurfaceVariant,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // QoS / Telemetry Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Packet Loss", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("0.0%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SignalGreen)
                        Text("0 dropped / 148", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Jitter (±)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("1.8 ms", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = CyberCyan)
                        Text("Min: 8ms | Max: 35ms", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Uptime SLA", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("99.9%", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = ElectricIndigo)
                        Text("Availability", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // Per-Device Latency Bar List
        item {
            Text(
                text = if (isBengali) "ডিভাইসভিত্তিক লেটেন্সি ও আরটিটি (RTT)" else "Per-Node Round Trip Latency",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        items(devices.values.toList(), key = { it.deviceId }) { dev ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (dev.status == DeviceStatus.ONLINE) SignalGreen else SignalRed)
                            )
                            Text(
                                text = dev.deviceId,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${dev.lastPingMs} ms",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = CyberCyan
                            )

                            Button(
                                onClick = { onSendSinglePing(dev.deviceId) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TechDarkSurfaceVariant,
                                    contentColor = CyberCyan
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Ping", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Simulated mini sparkline representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(TechDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dev.latencyHistory.forEach { pingVal ->
                            val heightFrac = (pingVal / 50f).coerceIn(0.15f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(fraction = heightFrac)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (pingVal < 20) SignalGreen else if (pingVal < 35) CyberCyan else SignalAmber
                                    )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
