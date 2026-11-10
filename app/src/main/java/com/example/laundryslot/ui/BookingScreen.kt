package com.example.laundryslot.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laundryslot.data.MachineStatus
import com.example.laundryslot.data.PaymentMethod
import com.example.laundryslot.viewmodel.LaundryPackage
import com.example.laundryslot.viewmodel.LaundryViewModel
import com.example.laundryslot.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

/**
 * Booking Screen
 * Supports two main modes:
 * 1. Self-Service Machine Slot Booking (Washing machine queue management)
 * 2. Full-Service Laundry (Staff wash + dry + steam iron with UPI/Cash payment)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(viewModel: LaundryViewModel, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current

    // Tab state: 0 = Self-Service (Machine), 1 = Full-Service (Wash + Iron)
    var selectedServiceTab by rememberSaveable { mutableIntStateOf(1) }

    // Machine Slot State
    val machines          by viewModel.machines.collectAsState()
    val selectedMachineId by viewModel.selectedMachineId.collectAsState()
    val selectedDate      by viewModel.selectedDate.collectAsState()
    val selectedTimeSlot  by viewModel.selectedTimeSlot.collectAsState()
    val isLoading         by viewModel.isLoading.collectAsState()
    val timeSlots          = viewModel.getTimeSlots()

    // Full Service State
    val availablePackages  = viewModel.availablePackages
    val selectedPackage   by viewModel.selectedPackage.collectAsState()
    val clothesCount      by viewModel.clothesCount.collectAsState()
    val selectedPayment   by viewModel.selectedPaymentMethod.collectAsState()
    val instructions      by viewModel.instructions.collectAsState()
    var fullServiceDate   by rememberSaveable { mutableStateOf("") }
    var fullServiceSlot   by rememberSaveable { mutableStateOf(viewModel.getFullServiceSlots()[0]) }

    // Payment Dialog state
    var showUpiPaymentDialog by remember { mutableStateOf(false) }

    // Collect SharedFlow UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.BookingSuccess -> snackbarHostState.showSnackbar("✅ ${event.message}")
                is UiEvent.ShowError      -> snackbarHostState.showSnackbar("❌ ${event.message}")
                is UiEvent.ShowMessage    -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text       = "Laundry Booking",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "Choose machine slot or all-in-one wash & iron service",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // ─── Service Toggle Tabs ───────────────────────────────────────────────
        TabRow(selectedTabIndex = selectedServiceTab) {
            Tab(
                selected = selectedServiceTab == 1,
                onClick  = { selectedServiceTab = 1 },
                text     = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Iron, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Wash + Iron", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedServiceTab == 0,
                onClick  = { selectedServiceTab = 0 },
                text     = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalLaundryService, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Self Wash")
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        if (selectedServiceTab == 1) {
            // ═══════════════════════════════════════════════════════════════════
            // FULL SERVICE: WASH + DRY + STEAM IRON (WITH PAYMENT)
            // ═══════════════════════════════════════════════════════════════════

            // Step 1: Select Package
            StepHeader(number = "1", title = "Select Laundry Service")
            Spacer(Modifier.height(8.dp))

            availablePackages.forEach { pkg ->
                val isSelected = pkg.id == selectedPackage.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectPackage(pkg) },
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (pkg.id) {
                                "wash_iron" -> Icons.Filled.DryCleaning
                                "iron_only" -> Icons.Filled.Iron
                                else        -> Icons.Filled.LocalLaundryService
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(pkg.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("⏱ ${pkg.estimatedTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${pkg.pricePerCloth.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            Text("/ cloth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Step 2: Garment Counter & Total Price Calculation
            StepHeader(number = "2", title = "Number of Clothes")
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Garment Quantity", fontWeight = FontWeight.SemiBold)
                        Text("Shirts, trousers, towels, etc.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.setClothesCount(clothesCount - 1) },
                            enabled = clothesCount > 1
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$clothesCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { viewModel.setClothesCount(clothesCount + 1) },
                            enabled = clothesCount < 50
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Step 3: Pickup Date & Slot
            StepHeader(number = "3", title = "Pickup Schedule")
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> fullServiceDate = "%02d/%02d/%04d".format(d, m + 1, y) },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                    ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (fullServiceDate.isBlank()) "Pick Collection Date" else "Date: $fullServiceDate")
            }

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.getFullServiceSlots().forEach { slot ->
                    FilterChip(
                        selected = slot == fullServiceSlot,
                        onClick  = { fullServiceSlot = slot },
                        label    = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Step 4: Special Instructions
            StepHeader(number = "4", title = "Special Instructions (Optional)")
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { viewModel.setInstructions(it) },
                placeholder = { Text("e.g. Wash white clothes separately, delicate iron...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(18.dp))

            // Step 5: Payment Method
            StepHeader(number = "5", title = "Select Payment Method")
            Spacer(Modifier.height(8.dp))

            val totalAmount = selectedPackage.pricePerCloth * clothesCount

            val paymentOptions = listOf(
                Triple(PaymentMethod.UPI, "UPI (GPay / PhonePe / Paytm)", Icons.Filled.QrCode),
                Triple(PaymentMethod.CASH_ON_DELIVERY, "Pay at Laundry Desk / Cash", Icons.Filled.Payments),
                Triple(PaymentMethod.HOSTEL_WALLET, "Hostel Student Wallet (Prepaid)", Icons.Filled.CreditCard)
            )

            paymentOptions.forEach { (method, label, icon) ->
                val isSelected = method == selectedPayment
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.setPaymentMethod(method) },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bill Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("Package", selectedPackage.name)
                    SummaryRow("Quantity", "$clothesCount clothes (₹${selectedPackage.pricePerCloth.toInt()}/cloth)")
                    SummaryRow("Payment Mode", when (selectedPayment) {
                        PaymentMethod.UPI -> "UPI Online"
                        PaymentMethod.CASH_ON_DELIVERY -> "Pay on Delivery"
                        PaymentMethod.HOSTEL_WALLET -> "Hostel Wallet"
                    })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Book & Pay Button
            Button(
                onClick = {
                    if (fullServiceDate.isBlank()) {
                        return@Button
                    }
                    if (selectedPayment == PaymentMethod.UPI) {
                        showUpiPaymentDialog = true
                    } else {
                        viewModel.createFullServiceBooking(
                            pickupDate = fullServiceDate,
                            timeSlot   = fullServiceSlot
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = if (isLoading) "Processing..." else when (selectedPayment) {
                        PaymentMethod.UPI -> "Pay ₹${totalAmount.toInt()} via UPI"
                        PaymentMethod.CASH_ON_DELIVERY -> "Place Order (Pay ₹${totalAmount.toInt()} Later)"
                        PaymentMethod.HOSTEL_WALLET -> "Pay ₹${totalAmount.toInt()} with Wallet"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

        } else {
            // ═══════════════════════════════════════════════════════════════════
            // SELF-SERVICE MACHINE SLOT BOOKING (ORIGINAL FLOW)
            // ═══════════════════════════════════════════════════════════════════

            // Step 1: Select Machine
            StepHeader(number = "1", title = "Select Washing Machine")
            Spacer(Modifier.height(8.dp))

            val availableMachines = machines.filter { it.status != MachineStatus.MAINTENANCE }
            if (availableMachines.isEmpty()) {
                Text("No machines available.", style = MaterialTheme.typography.bodyMedium)
            } else {
                availableMachines.forEach { machine ->
                    val isSelected = machine.id == selectedMachineId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectMachine(machine.id) },
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.LocalLaundryService, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(machine.name, fontWeight = FontWeight.Bold)
                                Text("${machine.location} • ${machine.capacity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Step 2: Date
            StepHeader(number = "2", title = "Select Wash Date")
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> viewModel.selectDate("%02d/%02d/%04d".format(d, m + 1, y)) },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                    ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (selectedDate.isBlank()) "Tap to pick date" else "Date: $selectedDate")
            }

            Spacer(Modifier.height(18.dp))

            // Step 3: Time Slot
            StepHeader(number = "3", title = "Select Machine Time Slot")
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { slot ->
                    FilterChip(
                        selected = slot == selectedTimeSlot,
                        onClick  = { viewModel.selectTimeSlot(slot) },
                        label    = { Text(slot, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.createBooking() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = if (isLoading) "Booking Slot..." else "Confirm Free Machine Slot",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // ─── Simulated UPI Payment Sheet Dialog ────────────────────────────────────
    if (showUpiPaymentDialog) {
        val total = (selectedPackage.pricePerCloth * clothesCount).toInt()
        AlertDialog(
            onDismissRequest = { showUpiPaymentDialog = false },
            icon = {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Complete UPI Payment", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Amount: ₹$total",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Hostel Laundry Service: Sunrise Desk", style = MaterialTheme.typography.bodySmall)

                    // Mock QR code frame
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F4F8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.QrCode, contentDescription = "QR Code", modifier = Modifier.size(80.dp), tint = Color.Black)
                            Text("UPI QR Code", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }

                    Text("UPI ID: hostel.laundry@okhdfcbank", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("GPay", "PhonePe", "Paytm", "BHIM").forEach { appName ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = appName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpiPaymentDialog = false
                        viewModel.createFullServiceBooking(
                            pickupDate = fullServiceDate,
                            timeSlot   = fullServiceSlot,
                            simulatedPaymentSuccess = true
                        )
                    }
                ) {
                    Text("Simulate Payment Success")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StepHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
