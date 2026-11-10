package com.example.laundryslot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Login      : Screen("login",       "Login",       Icons.Filled.Lock)
    data object Home       : Screen("home",        "Home",        Icons.Filled.Home)
    data object Machines   : Screen("machines",    "Machines",    Icons.Filled.LocalLaundryService)
    data object Booking    : Screen("booking",     "Book",        Icons.Filled.CalendarMonth)
    data object MyBookings : Screen("my_bookings", "My Bookings", Icons.Filled.Receipt)
    data object Profile    : Screen("profile",     "Profile",     Icons.Filled.Person)
    data object Location   : Screen("location",    "Location",    Icons.Filled.LocationOn)
}

// Only screens that appear in the bottom navigation bar
val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Machines,
    Screen.Booking,
    Screen.MyBookings,
    Screen.Profile,
    Screen.Location
)
