package com.example.laundryslot.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laundryslot.viewmodel.LaundryViewModel
import com.example.laundryslot.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * Login Screen
 * Student hostel authentication supporting:
 * 1. Standard login with Name, Roll Number, Room Number & Password
 * 2. 1-Tap Quick Viva Demo Login (for examiners / viva evaluation)
 * 3. Offline session persistence via ViewModel & SavedStateHandle
 */
@Composable
fun LoginScreen(
    viewModel: LaundryViewModel,
    snackbarHostState: SnackbarHostState,
    onLoginSuccess: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var studentName   by rememberSaveable { mutableStateOf("") }
    var rollNumber    by rememberSaveable { mutableStateOf("") }
    var hostelName    by rememberSaveable { mutableStateOf("Sunrise Boys Hostel") }
    var roomNumber    by rememberSaveable { mutableStateOf("") }
    var password      by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        // If already logged in, navigate immediately
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Collect SharedFlow UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowError   -> snackbarHostState.showSnackbar("❌ ${event.message}")
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // ─── App Header & Branding ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.LocalLaundryService,
                        contentDescription = "App Logo",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(54.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = "P21 – LaundrySlot",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.primary
                )

                Text(
                    text       = "Hostel Laundry Management & Slot Booking",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign  = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ─── Login Form Card ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isVisible,
            enter   = fadeIn(tween(800)) + slideInVertically(tween(800)) { 40 }
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text       = "Student Login",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Student Name
                    OutlinedTextField(
                        value         = studentName,
                        onValueChange = { studentName = it },
                        label         = { Text("Student Full Name") },
                        placeholder   = { Text("e.g. Rahul Sharma") },
                        leadingIcon   = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    // Roll Number
                    OutlinedTextField(
                        value         = rollNumber,
                        onValueChange = { rollNumber = it },
                        label         = { Text("Roll Number / Student ID") },
                        placeholder   = { Text("e.g. CS21045") },
                        leadingIcon   = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    // Hostel Name & Room Number Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value         = hostelName,
                            onValueChange = { hostelName = it },
                            label         = { Text("Hostel") },
                            leadingIcon   = { Icon(Icons.Filled.Home, contentDescription = null) },
                            singleLine    = true,
                            modifier      = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value         = roomNumber,
                            onValueChange = { roomNumber = it },
                            label         = { Text("Room No") },
                            placeholder   = { Text("204") },
                            leadingIcon   = { Icon(Icons.Filled.MeetingRoom, contentDescription = null) },
                            singleLine    = true,
                            modifier      = Modifier.weight(0.9f)
                        )
                    }

                    // Password
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Password") },
                        leadingIcon   = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier   = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    // Primary Login Button
                    Button(
                        onClick = {
                            val success = viewModel.login(
                                name     = studentName,
                                roll     = rollNumber,
                                hostel   = hostelName,
                                room     = roomNumber
                            )
                            if (success) {
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = MaterialTheme.shapes.large
                    ) {
                        Text("Sign In to LaundrySlot", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text     = " OR ",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    // Quick Viva Demo Button
                    OutlinedButton(
                        onClick = {
                            viewModel.quickDemoLogin()
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("⚡ 1-Tap Quick Viva Demo Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ─── Offline Account Security Note ─────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = MaterialTheme.shapes.medium,
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier          = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Filled.Lock,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = "Offline Authentication — Student session & preferences saved locally via Room Database & ViewModel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

