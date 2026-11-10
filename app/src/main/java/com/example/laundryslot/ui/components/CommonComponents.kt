package com.example.laundryslot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laundryslot.data.BookingStatus
import com.example.laundryslot.data.MachineStatus
import com.example.laundryslot.ui.theme.GreenAvailable
import com.example.laundryslot.ui.theme.OrangeOccupied
import com.example.laundryslot.ui.theme.RedMaintenance

/**
 * Unit I: Reusable composable for machine status badge.
 * Uses animateColorAsState for smooth color transitions.
 */
@Composable
fun MachineStatusBadge(status: MachineStatus, modifier: Modifier = Modifier) {
    val targetColor = when (status) {
        MachineStatus.AVAILABLE   -> GreenAvailable
        MachineStatus.OCCUPIED    -> OrangeOccupied
        MachineStatus.MAINTENANCE -> RedMaintenance
    }

    // Unit I: Compose animation — smooth color change
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "statusColor"
    )

    val label = when (status) {
        MachineStatus.AVAILABLE   -> "Available"
        MachineStatus.OCCUPIED    -> "Occupied"
        MachineStatus.MAINTENANCE -> "Maintenance"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(animatedColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = animatedColor,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Reusable badge for booking status.
 */
@Composable
fun BookingStatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        BookingStatus.UPCOMING    -> Pair(Color(0xFF1565C0), "Upcoming")
        BookingStatus.IN_PROGRESS -> Pair(Color(0xFFE65100), "In Progress")
        BookingStatus.COMPLETED   -> Pair(GreenAvailable,    "Completed")
        BookingStatus.CANCELLED   -> Pair(RedMaintenance,    "Cancelled")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = color,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Section header used across all screens for consistent UI.
 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.titleMedium,
        color      = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        modifier   = modifier.padding(vertical = 8.dp)
    )
}

