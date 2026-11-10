package com.example.laundryslot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.laundryslot.viewmodel.LaundryViewModel
import com.example.laundryslot.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * Profile Screen
 * Displays student credentials, laundry facility details, edit dialog,
 * and session logout functionality.
 */
@Composable
fun ProfileScreen(
    viewModel: LaundryViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit = {}
) {
    val studentName   by viewModel.studentName.collectAsState()
    val rollNumber    by viewModel.studentRollNumber.collectAsState()
    val hostelName    by viewModel.hostelName.collectAsState()
    val roomNumber    by viewModel.roomNumber.collectAsState()

    var showEditDialog   by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Collect events on this screen too
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            if (event is UiEvent.ShowMessage) {
                snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ─── Profile Header ────────────────────────────────────────────────────
        Surface(
            modifier       = Modifier.fillMaxWidth(),
            shape          = MaterialTheme.shapes.extraLarge,
            color          = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = studentName,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = "Room $roomNumber  •  $hostelName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showEditDialog = true }
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Edit Profile")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Student Info ──────────────────────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text       = "Student Details",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                ProfileRow(icon = Icons.Filled.Person,   label = "Name",        value = studentName)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.Person,   label = "Roll / Student ID", value = rollNumber)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.Home,     label = "Hostel",      value = hostelName)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.CheckCircle, label = "Room No.", value = roomNumber)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Laundry Room Info ─────────────────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text       = "Laundry Room Information",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                ProfileRow(icon = Icons.Filled.LocationOn, label = "Location",  value = "Ground Floor, Block A & First Floor, Block C")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.CheckCircle, label = "Hours",    value = "6:00 AM – 9:00 PM (Daily)")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.Home,        label = "Machines", value = "5 Washing Machines")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileRow(icon = Icons.Filled.CheckCircle, label = "Contact",  value = "Warden Office: +91 98765 43210")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── App Info ──────────────────────────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = MaterialTheme.shapes.large,
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text  = "P21 – LaundrySlot  •  v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = "Hostel Laundry Management · Built with Kotlin & Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Logout Button ─────────────────────────────────────────────────────
        OutlinedButton(
            onClick  = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = MaterialTheme.shapes.large,
            colors   = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Logout from Account", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }

    // ─── Logout Confirmation Dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Confirm Logout?", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to log out from LaundrySlot?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ─── Edit Profile Dialog ───────────────────────────────────────────────────
    if (showEditDialog) {
        EditProfileDialog(
            currentName   = studentName,
            currentHostel = hostelName,
            currentRoom   = roomNumber,
            onDismiss     = { showEditDialog = false },
            onSave        = { name, hostel, room ->
                viewModel.updateProfile(name, hostel, room)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(20.dp)
        )
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentHostel: String,
    currentRoom: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name   by remember { mutableStateOf(currentName) }
    var hostel by remember { mutableStateOf(currentHostel) }
    var room   by remember { mutableStateOf(currentRoom) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Student Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = hostel,
                    onValueChange = { hostel = it },
                    label         = { Text("Hostel Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = room,
                    onValueChange = { room = it },
                    label         = { Text("Room Number") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim(), hostel.trim(), room.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

