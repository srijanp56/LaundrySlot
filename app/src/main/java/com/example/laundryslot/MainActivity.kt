package com.example.laundryslot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.laundryslot.navigation.Screen
import com.example.laundryslot.navigation.bottomNavScreens
import com.example.laundryslot.ui.BookingScreen
import com.example.laundryslot.ui.HomeScreen
import com.example.laundryslot.ui.LocationScreen
import com.example.laundryslot.ui.LoginScreen
import com.example.laundryslot.ui.MachineScreen
import com.example.laundryslot.ui.MyBookingsScreen
import com.example.laundryslot.ui.ProfileScreen
import com.example.laundryslot.ui.theme.LaundrySlotTheme
import com.example.laundryslot.viewmodel.LaundryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaundrySlotTheme {
                LaundrySlotApp()
            }
        }
    }
}

@Composable
fun LaundrySlotApp() {
    val navController     = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Single ViewModel shared across all screens — Unit III: state hoisting
    val viewModel: LaundryViewModel = viewModel(factory = LaundryViewModel.Factory)
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Determine current destination to hide bottom bar on Login screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Login.route

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar    = {
            if (showBottomBar) {
                LaundryBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            // ─── Login Screen ──────────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel         = viewModel,
                    snackbarHostState = snackbarHostState,
                    onLoginSuccess    = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ─── Main Screens ──────────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Machines.route) {
                MachineScreen(
                    viewModel         = viewModel,
                    onNavigateToBooking = {
                        navController.navigate(Screen.Booking.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Booking.route) {
                BookingScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
            composable(Screen.MyBookings.route) {
                MyBookingsScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel         = viewModel,
                    snackbarHostState = snackbarHostState,
                    onLogout          = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Location.route) {
                LocationScreen()
            }
        }
    }
}

@Composable
fun LaundryBottomNavBar(navController: androidx.navigation.NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavScreens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = {
                    Icon(imageVector = screen.icon, contentDescription = screen.label)
                },
                label = {
                    Text(
                        text       = screen.label,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
