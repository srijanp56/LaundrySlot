package com.example.laundryslot.ui

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import java.util.Locale

private const val TAG = "LocationScreen"

// Fixed hostel coordinates (latitude, longitude)
private const val HOSTEL_LAT = 28.6139
private const val HOSTEL_LNG = 77.2090

/**
 * Unit V: Location Screen — demonstrates all required concepts WITHOUT Google Maps.
 *
 * Concepts covered:
 * ✅ Runtime location permissions (Accompanist)
 * ✅ Fused Location Provider Client
 * ✅ Basic location updates (lastLocation)
 * ✅ Reverse Geocoding (Geocoder — lat/lng → human address)
 * ✅ Distance calculation (Haversine formula)
 * ✅ Location-aware UI
 * ✅ DisposableEffect for cleanup
 * ✅ LaunchedEffect for location fetch
 *
 * No Google Maps API key needed — ZERO cost.
 */
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationScreen() {
    val context = LocalContext.current

    var currentAddress  by remember { mutableStateOf("Fetching your location...") }
    var currentLat      by remember { mutableStateOf<Double?>(null) }
    var currentLng      by remember { mutableStateOf<Double?>(null) }
    var distanceToHostel by remember { mutableStateOf<String?>(null) }
    var locationStatus  by remember { mutableStateOf("Waiting for permission...") }
    var isVisible       by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Unit V: Runtime permission state
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Unit I: LaunchedEffect — run when permission status changes
    LaunchedEffect(locationPermission.status.isGranted) {
        isVisible = true
        if (locationPermission.status.isGranted) {
            locationStatus = "Getting location..."
            Log.d(TAG, "Permission granted — fetching location")

            // Unit V: Fused Location Provider
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude
                        Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")

                        // Unit V: Reverse Geocoding — lat/lng → readable address
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                    currentAddress = addresses.firstOrNull()?.getAddressLine(0)
                                        ?: "Address not available"
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                currentAddress = addresses?.firstOrNull()?.getAddressLine(0)
                                    ?: "Address not available"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Geocoding failed: ${e.message}")
                            currentAddress = "Could not resolve address"
                        }

                        // Distance calculation using Haversine formula
                        val distanceKm = haversineDistance(
                            lat1 = location.latitude, lng1 = location.longitude,
                            lat2 = HOSTEL_LAT,         lng2 = HOSTEL_LNG
                        )
                        distanceToHostel = if (distanceKm < 1.0) {
                            "${(distanceKm * 1000).toInt()} meters from hostel"
                        } else {
                            "${"%.1f".format(distanceKm)} km from hostel"
                        }
                        locationStatus = "Location found ✓"
                    } else {
                        currentAddress = "Location unavailable — enable GPS"
                        locationStatus = "GPS not responding"
                        Log.w(TAG, "Location is null — GPS may be off")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Location error: ${e.message}")
                    locationStatus = "Error: ${e.message}"
                    currentAddress = "Failed to get location"
                }
        }
    }

    // Unit I: DisposableEffect — demonstrates lifecycle-aware cleanup
    DisposableEffect(Unit) {
        Log.d(TAG, "LocationScreen: entered composition")
        onDispose {
            // In a real app: stop location updates here
            Log.d(TAG, "LocationScreen: left composition — stopping location updates")
        }
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
            enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
        ) {
            Column {
                Text(
                    text       = "Hostel Location",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "Find your laundry room",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            // ─── Permission Granted ────────────────────────────────────────────
            locationPermission.status.isGranted -> {

                // Current location card
                AnimatedVisibility(
                    visible = isVisible,
                    enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { 40 }
                ) {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.large,
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(22.dp)
                                )
                                Text(
                                    text       = "Your Current Location",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text  = currentAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (currentLat != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = "📍 ${"%.5f".format(currentLat)}, ${"%.5f".format(currentLng)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                                )
                            }
                            distanceToHostel?.let { dist ->
                                Spacer(Modifier.height(6.dp))
                                Card(
                                    shape  = MaterialTheme.shapes.small,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text     = "🏠 $dist",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Mini map placeholder (coordinate grid)
                AnimatedVisibility(
                    visible = isVisible,
                    enter   = fadeIn(tween(700))
                ) {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text       = "📍 Map View",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))

                            // Static visual map representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color(0xFFE8F4FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.primary,
                                        modifier           = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text       = "Sunrise Boys Hostel",
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text      = "Lat: $HOSTEL_LAT  •  Lng: $HOSTEL_LNG",
                                        style     = MaterialTheme.typography.labelSmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    if (currentLat != null) {
                                        Text(
                                            text  = distanceToHostel ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ─── Show Rationale (denied once) ─────────────────────────────────
            locationPermission.status.shouldShowRationale -> {
                PermissionCard(
                    title   = "Location Permission Needed",
                    message = "This screen uses your location to show how far you are from the hostel laundry room. No data is stored or shared.",
                    buttonLabel = "Grant Permission",
                    isError = false,
                    onRequest   = { locationPermission.launchPermissionRequest() }
                )
            }

            // ─── Not yet asked ─────────────────────────────────────────────────
            else -> {
                PermissionCard(
                    title   = "Enable Location",
                    message = "Tap below to allow location access. This is used to calculate your distance to the hostel's laundry room.",
                    buttonLabel = "Allow Location",
                    isError = false,
                    onRequest   = { locationPermission.launchPermissionRequest() }
                )
            }
        }

        // ─── Hostel & Laundry Room Info ────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(900)) + slideInVertically(tween(900)) { 50 }
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "🏠 Hostel Details",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    val details = listOf(
                        "Hostel Name"      to "Sunrise Boys Hostel",
                        "Laundry Room A"   to "Ground Floor, Block A — Machines 1 & 2",
                        "Laundry Room B"   to "First Floor, Block C — Machines 3 & 4",
                        "Coordinates"      to "$HOSTEL_LAT°N, $HOSTEL_LNG°E",
                        "Open Hours"       to "6:00 AM – 9:00 PM (Daily)",
                        "Warden Contact"   to "+91 98765 43210",
                        "Emergency"        to "+91 98765 00000"
                    )

                    details.forEachIndexed { index, (label, value) ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text  = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text       = value,
                                    style      = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (index < details.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Location Status Footer ────────────────────────────────────────────
        if (locationPermission.status.isGranted) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1000))) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.medium,
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (currentLat != null) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                        )
                        Text(
                            text  = locationStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    message: String,
    buttonLabel: String,
    isError: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        colors    = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                            else         MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector        = if (isError) Icons.Filled.Warning else Icons.Filled.LocationOn,
                contentDescription = null,
                tint               = if (isError) MaterialTheme.colorScheme.onErrorContainer
                                     else         MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Button(onClick = onRequest) { Text(buttonLabel) }
        }
    }
    Spacer(Modifier.height(16.dp))
}

/**
 * Unit V: Haversine formula — calculates straight-line distance
 * between two GPS coordinates in kilometers.
 * Used to show "X km from hostel" without any Maps API.
 */
fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadiusKm * c
}
