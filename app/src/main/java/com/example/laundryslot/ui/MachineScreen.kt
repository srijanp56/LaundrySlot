package com.example.laundryslot.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laundryslot.data.Machine
import com.example.laundryslot.data.MachineStatus
import com.example.laundryslot.ui.components.MachineStatusBadge
import com.example.laundryslot.viewmodel.LaundryViewModel

/**
 * Machines Screen
 * Unit I: LazyColumn, AlertDialog, custom composables
 * Unit II: collectAsState from StateFlow
 */
@Composable
fun MachineScreen(viewModel: LaundryViewModel, onNavigateToBooking: () -> Unit) {
    val machines         by viewModel.machines.collectAsState()
    val selectedMachineId by viewModel.selectedMachineId.collectAsState()

    var machineToView by remember { mutableStateOf<Machine?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text       = "Washing Machines",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "Tap a machine to book a slot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Unit I: LazyColumn — efficient list that only renders visible items
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(machines, key = { _, m -> m.id }) { index, machine ->
                // Unit I: Staggered entry animation
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn(tween(300 + index * 80)) + expandVertically(tween(300 + index * 80))
                ) {
                    MachineCard(
                        machine    = machine,
                        isSelected = machine.id == selectedMachineId,
                        onClick    = {
                            if (machine.status != MachineStatus.MAINTENANCE) {
                                viewModel.selectMachine(machine.id)
                                machineToView = machine
                            } else {
                                machineToView = machine
                            }
                        }
                    )
                }
            }
        }
    }

    // Unit I: AlertDialog — shown when user taps a machine
    machineToView?.let { machine ->
        AlertDialog(
            onDismissRequest = { machineToView = null },
            icon = {
                Icon(
                    imageVector = if (machine.status == MachineStatus.MAINTENANCE)
                        Icons.Filled.Build else Icons.Filled.LocalLaundryService,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(machine.name, fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MachineStatusBadge(status = machine.status)
                    Spacer(Modifier.height(4.dp))
                    InfoRow(label = "Location", value = machine.location)
                    InfoRow(label = "Capacity", value = machine.capacity)
                    InfoRow(label = "Last Serviced", value = machine.lastServiced)
                }
            },
            confirmButton = {
                if (machine.status != MachineStatus.MAINTENANCE) {
                    TextButton(onClick = {
                        viewModel.selectMachine(machine.id)
                        machineToView = null
                        onNavigateToBooking()
                    }) {
                        Text("Book this Machine")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { machineToView = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun MachineCard(
    machine: Machine,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = MaterialTheme.shapes.large,
        border    = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.LocalLaundryService,
                contentDescription = null,
                tint               = when (machine.status) {
                    MachineStatus.AVAILABLE   -> MaterialTheme.colorScheme.primary
                    MachineStatus.OCCUPIED    -> MaterialTheme.colorScheme.error
                    MachineStatus.MAINTENANCE -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = machine.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = machine.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "Capacity: ${machine.capacity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MachineStatusBadge(status = machine.status)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

