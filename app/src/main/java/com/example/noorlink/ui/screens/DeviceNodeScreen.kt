package com.example.noorlink.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DeviceNodeScreen(
    isBengali: Boolean,
    nodeDeviceId: String,
    capabilities: List<String>,
    isRegistered: Boolean,
    heartbeatCount: Int,
    lastCommand: String?,
    onRegisterDevice: () -> Unit,
    onAddCapability: (String) -> Unit,
    onRemoveCapability: (String) -> Unit,
    onSendManualPingPong: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newCapabilityInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Node Status Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRegistered) CyberCyan else TechDarkBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isRegistered) CyberCyan.copy(alpha = 0.2f) else TechDarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isRegistered) CyberCyan else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isBengali) "এই অ্যান্ড্রয়েড ডিভাইস মোড" else "This Device as an IoT Node",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = nodeDeviceId,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = CyberCyan
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRegistered) SignalGreen.copy(alpha = 0.15f) else TechDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isRegistered) SignalGreen else TechDarkBorder
                            )
                        ) {
                            Text(
                                text = if (isRegistered)
                                    (if (isBengali) "রেজিস্টার্ড (অনলাইন)" else "REGISTERED")
                                else (if (isBengali) "অনিবন্ধিত" else "UNREGISTERED"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRegistered) SignalGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isBengali)
                            "এই স্মার্টফোনটিকে একটি নূরলিংক ডিভাইস নোড হিসেবে রেজিস্টার করুন। এটি স্বয়ংক্রিয়ভাবে সার্ভারে 'Hello' হ্যান্ডশেক বার্তা পাঠাবে এবং প্যানেলের কমান্ড গ্রহণ করবে।"
                        else
                            "Run this device as a client node. Broadcast your device capabilities to the relay and listen for remote panel commands.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = onRegisterDevice,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegistered) SignalGreen else CyberCyan,
                            contentColor = Color(0xFF090D16)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("node_register_handshake_button")
                    ) {
                        Icon(
                            imageVector = if (isRegistered) Icons.Default.CheckCircle else Icons.Default.Handshake,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegistered)
                                (if (isBengali) "পুনরায় হ্যালো পাঠান (Re-Handshake)" else "Broadcast Hello Again")
                            else (if (isBengali) "হ্যালো হ্যান্ডশেক পাঠান (Register Node)" else "Send Hello Handshake"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Capabilities Manager
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
                        text = if (isBengali) "ডিভাইস সক্ষমতা (Capabilities)" else "Device Capabilities Registry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )

                    Text(
                        text = if (isBengali)
                            "হ্যান্ডশেক বার্তার সাথে নোড তার ফিচার লিস্ট সার্ভারকে জানায় যাতে প্যানেলে কন্ট্রোল অপশন লোড হতে পারে।"
                        else
                            "Capabilities advertised in the Hello message dictate which commands the operator panel can dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        capabilities.forEach { cap ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TechDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = cap,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NeonBlue
                                    )
                                    IconButton(
                                        onClick = { onRemoveCapability(cap) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add new capability input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCapabilityInput,
                            onValueChange = { newCapabilityInput = it },
                            placeholder = { Text("e.g. camera_1080p, gps_tracker") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("capability_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = TechDarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        FilledTonalButton(
                            onClick = {
                                if (newCapabilityInput.isNotBlank()) {
                                    onAddCapability(newCapabilityInput)
                                    newCapabilityInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CyberCyan.copy(alpha = 0.2f),
                                contentColor = CyberCyan
                            ),
                            modifier = Modifier.testTag("add_capability_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isBengali) "যোগ" else "Add")
                        }
                    }
                }
            }
        }

        // Live Handshake JSON Sample Preview (As requested in user's prompt step 4)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TechDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (isBengali) "হ্যান্ডশেক পে-লোড (Device.js)" else "Handshake JSON Payload",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = CyberCyan
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TechDarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = """{
  "type": "hello",
  "deviceId": "$nodeDeviceId",
  "token": "JWT_AUTH_TOKEN_ACTIVE",
  "capabilities": [
    ${capabilities.joinToString(",\n    ") { "\"$it\"" }}
  ],
  "info": {
    "os": "Android",
    "version": "1.0.0"
  }
}""",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Heartbeat & Command Responder Box
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
                        text = if (isBengali) "নোড হার্টবিট ও কমান্ড রেসপন্ডার" else "Heartbeat & Command Listener",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "প্রেরিত হার্টবিট রেসপন্স:" else "Pong Packets Sent:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$heartbeatCount pongs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CyberCyan
                        )
                    }

                    Button(
                        onClick = onSendManualPingPong,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TechDarkSurfaceVariant,
                            contentColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isBengali) "ম্যানুয়াল পং (Pong) পাঠান" else "Trigger Manual Pong")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
