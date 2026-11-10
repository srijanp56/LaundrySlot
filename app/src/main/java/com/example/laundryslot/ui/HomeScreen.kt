package com.example.laundryslot.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laundryslot.viewmodel.LaundryViewModel

/**
 * Home Screen
 * Unit I: LaunchedEffect, AnimatedVisibility, custom composables
 * Unit III: Collecting StateFlow, state hoisting
 */
@Composable
fun HomeScreen(viewModel: LaundryViewModel) {
    val totalMachines    by viewModel.totalMachineCount.collectAsState()
    val availableCount   by viewModel.availableCount.collectAsState()
    val occupiedCount    by viewModel.occupiedCount.collectAsState()
    val nextBooking      by viewModel.nextUpcomingBooking.collectAsState()
    val studentName      by viewModel.studentName.collectAsState()
    val hostelName       by viewModel.hostelName.collectAsState()

    // Unit I: LaunchedEffect — triggers animation once on first composition
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ─── Header ───────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { -50 }
        ) {
            Surface(
                modifier      = Modifier.fillMaxWidth(),
                shape         = MaterialTheme.shapes.extraLarge,
                color         = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "👋 Hello, ${studentName.split(" ").first()}!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = hostelName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text  = "Laundry Slot Management",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Stats Row ─────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(800)) + slideInVertically(tween(800)) { 50 }
        ) {
            Column {
                Text(
                    text       = "Machine Status",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Total",
                        value    = totalMachines.toString(),
                        icon     = Icons.Filled.LocalLaundryService,
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        onColor  = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Available",
                        value    = availableCount.toString(),
                        icon     = Icons.Filled.CheckCircle,
                        color    = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                        onColor  = androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Occupied",
                        value    = occupiedCount.toString(),
                        icon     = Icons.Filled.LocalLaundryService,
                        color    = androidx.compose.ui.graphics.Color(0xFFFFF3E0),
                        onColor  = androidx.compose.ui.graphics.Color(0xFFE65100)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Next Booking ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { 50 }
        ) {
            Column {
                Text(
                    text       = "Upcoming Booking",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (nextBooking == null) {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.large,
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "No upcoming bookings.\nBook a slot to get started!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.large,
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier  = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.CalendarMonth,
                                contentDescription = "Booking",
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Column {
                                val title = if (nextBooking!!.serviceType == com.example.laundryslot.data.ServiceType.FULL_SERVICE) {
                                    "${nextBooking!!.packageType} (₹${nextBooking!!.totalAmount.toInt()})"
                                } else {
                                    nextBooking!!.machineName
                                }
                                Text(
                                    text       = title,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text  = "${nextBooking!!.date}  •  ${nextBooking!!.timeSlot}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Quick Tips ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(1200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.large,
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "💡 Tips",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "• Book your slot at least a day in advance.",
                        "• Cancel unused bookings so others can use the machine.",
                        "• Peak hours are 7–9 AM and 7–9 PM."
                    ).forEach { tip ->
                        Text(
                            text     = tip,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape    = MaterialTheme.shapes.large,
        colors   = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = onColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = onColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = onColor.copy(0.8f))
        }
    }
}

