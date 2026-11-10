package com.example.laundryslot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.laundryslot.ui.HomeScreen
import com.example.laundryslot.ui.theme.LaundrySlotTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit VI: Jetpack Compose UI tests
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class LaundrySlotUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_isDisplayedInitially() {
        composeTestRule.setContent {
            LaundrySlotTheme {
                LaundrySlotApp()
            }
        }

        // Check Login screen elements
        composeTestRule.onNodeWithText("P21 – LaundrySlot").assertIsDisplayed()
        composeTestRule.onNodeWithText("Student Login").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsMachineStatusSection() {
        composeTestRule.setContent {
            LaundrySlotTheme {
                LaundrySlotApp()
            }
        }

        composeTestRule.onNodeWithText("Machine Status").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsUpcomingBookingSection() {
        composeTestRule.setContent {
            LaundrySlotTheme {
                LaundrySlotApp()
            }
        }

        composeTestRule.onNodeWithText("Upcoming Booking").assertIsDisplayed()
    }
}

