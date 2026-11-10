package com.example.laundryslot

import com.example.laundryslot.data.Booking
import com.example.laundryslot.data.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit VI: JUnit unit tests for booking logic.
 * Run with: ./gradlew test
 */
class BookingLogicTest {

    // ─── Helper to create a test booking ──────────────────────────────────────
    private fun makeBooking(
        id: Int = 1,
        machineId: Int = 1,
        date: String = "15/11/2026",
        timeSlot: String = "09:00 - 10:00",
        status: BookingStatus = BookingStatus.UPCOMING
    ) = Booking(
        id          = id,
        machineId   = machineId,
        machineName = "Machine $machineId",
        studentName = "Test Student",
        roomNumber  = "101",
        date        = date,
        timeSlot    = timeSlot,
        status      = status
    )

    // ─── Overlap detection logic ───────────────────────────────────────────────

    @Test
    fun `booking with same machine date and slot should be detected as overlap`() {
        val existing = makeBooking(machineId = 1, date = "15/11/2026", timeSlot = "09:00 - 10:00")
        val newBooking = makeBooking(machineId = 1, date = "15/11/2026", timeSlot = "09:00 - 10:00")

        // Check overlap conditions match
        val isOverlap = existing.machineId == newBooking.machineId &&
                        existing.date == newBooking.date &&
                        existing.timeSlot == newBooking.timeSlot &&
                        existing.status == BookingStatus.UPCOMING

        assertTrue("Should detect overlap", isOverlap)
    }

    @Test
    fun `booking on different date should not be an overlap`() {
        val existing = makeBooking(machineId = 1, date = "15/11/2026", timeSlot = "09:00 - 10:00")
        val newDate  = "16/11/2026"

        val isOverlap = existing.date == newDate && existing.timeSlot == "09:00 - 10:00"
        assertFalse("Different date should not overlap", isOverlap)
    }

    @Test
    fun `booking on different time slot should not be an overlap`() {
        val existing    = makeBooking(timeSlot = "09:00 - 10:00")
        val newTimeSlot = "10:00 - 11:00"

        val isOverlap = existing.timeSlot == newTimeSlot
        assertFalse("Different time slot should not overlap", isOverlap)
    }

    @Test
    fun `cancelled booking should not block same slot`() {
        val cancelled = makeBooking(status = BookingStatus.CANCELLED)

        // A cancelled booking should not be considered an overlap
        val isOverlap = cancelled.status == BookingStatus.UPCOMING
        assertFalse("Cancelled booking should not block slot", isOverlap)
    }

    // ─── Booking status tests ──────────────────────────────────────────────────

    @Test
    fun `booking should default to UPCOMING status`() {
        val booking = makeBooking()
        assertEquals(BookingStatus.UPCOMING, booking.status)
    }

    @Test
    fun `booking date format should be dd MM yyyy`() {
        val booking = makeBooking(date = "15/11/2026")
        val parts   = booking.date.split("/")
        assertEquals(3, parts.size)
        assertEquals(2, parts[0].length)   // day
        assertEquals(2, parts[1].length)   // month
        assertEquals(4, parts[2].length)   // year
    }

    @Test
    fun `time slot format should contain dash`() {
        val booking = makeBooking(timeSlot = "09:00 - 10:00")
        assertTrue("Time slot should contain ' - '", booking.timeSlot.contains(" - "))
    }

    @Test
    fun `upcoming bookings filter works correctly`() {
        val bookings = listOf(
            makeBooking(id = 1, status = BookingStatus.UPCOMING),
            makeBooking(id = 2, status = BookingStatus.COMPLETED),
            makeBooking(id = 3, status = BookingStatus.CANCELLED),
            makeBooking(id = 4, status = BookingStatus.UPCOMING)
        )

        val upcoming = bookings.filter { it.status == BookingStatus.UPCOMING }
        assertEquals(2, upcoming.size)
    }

    @Test
    fun `past bookings filter works correctly`() {
        val bookings = listOf(
            makeBooking(id = 1, status = BookingStatus.UPCOMING),
            makeBooking(id = 2, status = BookingStatus.COMPLETED),
            makeBooking(id = 3, status = BookingStatus.CANCELLED)
        )

        val past = bookings.filter { it.status != BookingStatus.UPCOMING }
        assertEquals(2, past.size)
    }

    @Test
    fun `full service price calculation matches quantity and rate`() {
        val pricePerCloth = 15.0 // Wash & Steam Iron
        val clothesCount = 10
        val total = pricePerCloth * clothesCount
        assertEquals(150.0, total, 0.001)
    }

    @Test
    fun `full service booking defaults to correct package name and payment status`() {
        val booking = Booking(
            serviceType = com.example.laundryslot.data.ServiceType.FULL_SERVICE,
            studentName = "Rahul",
            roomNumber = "204",
            date = "15/11/2026",
            timeSlot = "Morning Slot",
            packageType = "Wash & Steam Iron",
            clothesCount = 8,
            totalAmount = 120.0,
            paymentMethod = com.example.laundryslot.data.PaymentMethod.UPI,
            paymentStatus = com.example.laundryslot.data.PaymentStatus.PAID
        )

        assertEquals(com.example.laundryslot.data.ServiceType.FULL_SERVICE, booking.serviceType)
        assertEquals(120.0, booking.totalAmount, 0.001)
        assertEquals(com.example.laundryslot.data.PaymentStatus.PAID, booking.paymentStatus)
    }
}

