package com.example.laundryslot.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laundryslot.data.Booking
import com.example.laundryslot.data.BookingStatus
import com.example.laundryslot.data.PaymentMethod
import com.example.laundryslot.data.PaymentStatus
import com.example.laundryslot.data.ServiceType
import com.example.laundryslot.ui.components.BookingStatusBadge
import com.example.laundryslot.viewmodel.LaundryViewModel

/**
 * My Bookings Screen
 * Displays both self-service slot bookings and all-in-one wash & iron orders.
 * Shows payment status, price, garment count, and allows cancellation.
 */
@Composable
fun MyBookingsScreen(viewModel: LaundryViewModel) {
    val upcomingBookings by viewModel.upcomingBookings.collectAsState()
    val pastBookings     by viewModel.pastBookings.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text       = "My Laundry Bookings",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text       = "Track machine slots & wash + iron requests",
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
            listOf("Active & Upcoming (${upcomingBookings.size})", "Past History (${pastBookings.size})")
                .forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
        }

        val displayList = if (selectedTab == 0) upcomingBookings else pastBookings

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text      = if (selectedTab == 0) "No active bookings or laundry orders.\nGo to 'Book' tab to schedule one!"
                                else                  "No booking history found.",
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayList, key = { it.id }) { booking ->
                    DetailedBookingCard(
                        booking          = booking,
                        showCancelButton = booking.status == BookingStatus.UPCOMING,
                        onCancelClick    = { bookingToCancel = booking }
                    )
                }
            }
        }
    }

    // Cancel Confirmation Dialog
    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            icon  = {
                Icon(
                    imageVector        = Icons.Filled.Cancel,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Cancel This Booking?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Are you sure you want to cancel:\n\n" +
                    "• Service: ${if (booking.serviceType == ServiceType.FULL_SERVICE) booking.packageType else booking.machineName}\n" +
                    "• Date: ${booking.date} (${booking.timeSlot})\n" +
                    if (booking.totalAmount > 0) "• Amount: ₹${booking.totalAmount.toInt()} (Refund/Void)" else ""
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelBooking(booking.id)
                        bookingToCancel = null
                    }
                ) {
                    Text("Yes, Cancel", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) { Text("Keep Booking") }
            }
        )
    }
}

@Composable
private fun DetailedBookingCard(
    booking: Booking,
    showCancelButton: Boolean,
    onCancelClick: () -> Unit
) {
    val isFullService = booking.serviceType == ServiceType.FULL_SERVICE

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isFullService) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type & Status
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Type Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFullService) Icons.Filled.Iron else Icons.Filled.LocalLaundryService,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isFullService) "Full Service (Wash + Iron)" else "Self-Service Wash",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BookingStatusBadge(status = booking.status)
                    if (showCancelButton) {
                        IconButton(onClick = onCancelClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector        = Icons.Filled.Cancel,
                                contentDescription = "Cancel",
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Main Details
            Text(
                text = if (isFullService) booking.packageType else booking.machineName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isFullService && booking.clothesCount > 0) {
                Text(
                    text = "🧺 ${booking.clothesCount} Garments • Handled by Hostel Staff",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "🫧 Machine Slot Reservation • Room ${booking.roomNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (booking.instructions.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "📝 Note: \"${booking.instructions}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // Chips Row: Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoTag(icon = "📅", text = booking.date)
                InfoTag(icon = "⏰", text = booking.timeSlot)
            }

            // Payment Section
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Method & Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (booking.paymentMethod == PaymentMethod.UPI) Icons.Filled.QrCode else Icons.Filled.Payments,
                        contentDescription = null,
                        tint = if (booking.paymentStatus == PaymentStatus.PAID) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = when {
                            booking.totalAmount == 0.0 -> "Free Slot"
                            booking.paymentStatus == PaymentStatus.PAID -> "Paid via ${booking.paymentMethod.name}"
                            else -> "Pending (Pay on Delivery)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (booking.paymentStatus == PaymentStatus.PAID) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }

                // Price Tag
                if (booking.totalAmount > 0) {
                    Text(
                        text = "₹${booking.totalAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoTag(icon: String, text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text     = "$icon  $text",
            style    = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
