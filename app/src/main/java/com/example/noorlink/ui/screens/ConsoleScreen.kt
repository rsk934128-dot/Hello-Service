package com.example.noorlink.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noorlink.model.AuthToken
import com.example.noorlink.model.EventType
import com.example.noorlink.model.RelayEvent
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsoleScreen(
    isBengali: Boolean,
    events: List<RelayEvent>,
    jwtToken: AuthToken?,
    secret: String,
    onGenerateJwt: (String, String, List<String>) -> Unit,
    onUpdateSecret: (String) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var targetSubjectInput by remember { mutableStateOf("MY_SERVER_01") }
    var roleInput by remember { mutableStateOf("device") }
    var secretInput by remember { mutableStateOf(secret) }

    val filteredEvents = remember(events, selectedFilter) {
        if (selectedFilter == "ALL") events
        else events.filter { it.type.name == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // JWT Security Inspector Card (Fulfills Step 1 from prompt)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isBengali) "সিকিউরিটি লেয়ার (JWT Auth Handler)" else "Security Layer (JWT Auth)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isBengali) "হ্যান্ডশেক ভেরিফিকেশন ও ডিজিটাল সিগনেচার" else "HMAC SHA256 Signature Verification",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberCyan
                                )
                            }
                        }

                        if (jwtToken != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (jwtToken.isValid) SignalGreen.copy(alpha = 0.15f) else SignalRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (jwtToken.isValid) "VERIFIED" else "INVALID",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (jwtToken.isValid) SignalGreen else SignalRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Token input / generation parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetSubjectInput,
                            onValueChange = { targetSubjectInput = it },
                            label = { Text("Subject (Device ID)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = TechDarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = roleInput,
                            onValueChange = { roleInput = it },
                            label = { Text("Role") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = TechDarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onGenerateJwt(
                                targetSubjectInput,
                                roleInput,
                                listOf("telemetry:read", "heartbeat:ping", "command:exec")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_jwt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "নতুন JWT টোকেন জেনারেট করুন" else "Generate Signed JWT Token",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Render Token String & Copy button
                    jwtToken?.let { token ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF090D16),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RAW TOKEN (HS256):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CyberCyan
                                    )

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("NoorLink Token", token.rawToken))
                                            Toast.makeText(context, "Token Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Text(
                                    text = token.rawToken,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = NeonBlue
                                )

                                Text(
                                    text = "Claims: sub=${token.deviceId} | role=${token.role} | exp=in 2h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Event Packet Feed Header & Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "লাইভ ইভেন্ট ও প্যাকেট কনসোল" else "Live Packet Inspector",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Button(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechDarkSurfaceVariant,
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBengali) "ক্লিয়ার" else "Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Filter Chips Row
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("ALL", "HELLO", "PING", "PONG", "COMMAND").forEach { filterType ->
                    FilterChip(
                        selected = selectedFilter == filterType,
                        onClick = { selectedFilter = filterType },
                        label = { Text(filterType) },
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

        // Event Packets List
        items(filteredEvents, key = { it.id }) { event ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (event.type) {
                                    EventType.HELLO, EventType.HELLO_ACK -> ElectricIndigo
                                    EventType.PING, EventType.PONG -> CyberCyan
                                    EventType.COMMAND, EventType.COMMAND_ACK -> NeonBlue
                                    EventType.ERROR -> SignalRed
                                    else -> SignalAmber
                                }.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = event.type.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = when (event.type) {
                                        EventType.HELLO, EventType.HELLO_ACK -> ElectricIndigo
                                        EventType.PING, EventType.PONG -> CyberCyan
                                        EventType.COMMAND, EventType.COMMAND_ACK -> NeonBlue
                                        EventType.ERROR -> SignalRed
                                        else -> SignalAmber
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${event.source} ➔ ${event.target}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                        }

                        if (event.latencyMs != null) {
                            Text(
                                text = "${event.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CyberCyan
                            )
                        }
                    }

                    // JSON Payload body
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF090D16),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = event.payloadJson,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        if (filteredEvents.isEmpty()) {
            item {
                Text(
                    text = if (isBengali) "কোনো প্যাকেট ইভেন্ট পাওয়া যায়নি।" else "No packet events recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
