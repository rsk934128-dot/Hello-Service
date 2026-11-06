package com.example.noorlink.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noorlink.model.DeviceNode
import com.example.noorlink.model.DeviceStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalPurple
import com.example.ui.theme.SignalRed
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoorLinkTopAppBar(
    isBengali: Boolean,
    onToggleLanguage: () -> Unit,
    relayConnected: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(CyberCyan, NeonBlue))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "NoorLink Logo",
                        tint = Color(0xFF090D16),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "NoorLink",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = if (isBengali) "হ্যালো সার্ভিস ও রিমোট ইকোসিস্টেম" else "Hello Service & Relay Hub",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }
            }
        },
        actions = {
            // Relay Online pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (relayConnected) SignalGreen.copy(alpha = 0.15f) else SignalAmber.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (relayConnected) SignalGreen.copy(alpha = 0.5f) else SignalAmber.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (relayConnected) SignalGreen else SignalAmber)
                    )
                    Text(
                        text = if (relayConnected) (if (isBengali) "সক্রিয়" else "ONLINE") else (if (isBengali) "অফলাইন" else "IDLE"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (relayConnected) SignalGreen else SignalAmber
                    )
                }
            }

            // Language Toggle Button
            FilledTonalButton(
                onClick = onToggleLanguage,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("toggle_language_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = TechDarkSurfaceVariant,
                    contentColor = CyberCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Change Language",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBengali) "বাংলা" else "EN",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF090D16)
        ),
        modifier = modifier
    )
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceCard(
    device: DeviceNode,
    isBengali: Boolean,
    onPing: () -> Unit,
    onCommand: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_card_${device.deviceId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (device.isLocalDevice) CyberCyan.copy(alpha = 0.5f) else TechDarkBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title, Local Tag & Status
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (device.status) {
                                    DeviceStatus.ONLINE -> SignalGreen
                                    DeviceStatus.STANDBY -> SignalAmber
                                    DeviceStatus.OFFLINE -> SignalRed
                                }
                            )
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = device.deviceId,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                            if (device.isLocalDevice) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyberCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isBengali) "এই ডিভাইস" else "LOCAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CyberCyan,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Latency Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TechDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Ping",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${device.lastPingMs} ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CyberCyan
                        )
                    }
                }
            }

            // Hardware info & Specs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TechDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "OS: ${device.info.os} (${device.info.hardwareModel})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "🔋 ${device.info.battery}% | ${device.info.ipAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Capabilities chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                device.capabilities.forEach { cap ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricIndigo.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.3f))
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

            // Telemetry Preview if available
            if (device.telemetry.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF090D16),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (isBengali) "রিয়েল-টাইম টেলিমেট্রি:" else "LIVE TELEMETRY:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberCyan
                        )
                        device.telemetry.entries.forEach { (k, v) ->
                            Text(
                                text = "• $k: $v",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ping Button
                FilledTonalButton(
                    onClick = onPing,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ping_device_${device.deviceId}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TechDarkSurfaceVariant,
                        contentColor = CyberCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Ping",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBengali) "পিং" else "Ping",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Command Button
                Button(
                    onClick = onCommand,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_device_${device.deviceId}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF090D16)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Command",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBengali) "কমান্ড" else "Command",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Toggle Online/Offline
                IconButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.testTag("toggle_status_${device.deviceId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Status",
                        tint = if (device.status == DeviceStatus.ONLINE) SignalGreen else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun HeartbeatPulseRadar(
    isPinging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_scale"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(90.dp)
                .scale(if (isPinging) scale else 1f)
                .clip(CircleShape)
                .background(CyberCyan.copy(alpha = 0.12f))
        )
        // Mid ring
        Box(
            modifier = Modifier
                .size(65.dp)
                .clip(CircleShape)
                .border(2.dp, CyberCyan.copy(alpha = 0.4f), CircleShape)
        )
        // Center core
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(CyberCyan, NeonBlue))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = Color(0xFF090D16),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
