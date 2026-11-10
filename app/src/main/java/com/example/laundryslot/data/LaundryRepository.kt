package com.example.laundryslot.data

import kotlinx.coroutines.flow.Flow

/**
 * Unit III: Repository Pattern — single source of truth.
 * The ViewModel only talks to the Repository, never directly to the DAO.
 * This isolates data access logic and makes it easy to swap data sources.
 */
class LaundryRepository(private val dao: LaundryDao) {

    // ─── Machine Operations ────────────────────────────────────────────────────

    val allMachines: Flow<List<Machine>>   = dao.getAllMachines()
    val availableCount: Flow<Int>          = dao.getAvailableCount()
    val occupiedCount: Flow<Int>           = dao.getOccupiedCount()
    val totalMachineCount: Flow<Int>       = dao.getTotalMachineCount()

    suspend fun updateMachine(machine: Machine) = dao.updateMachine(machine)

    // ─── Booking Operations ────────────────────────────────────────────────────

    val allBookings: Flow<List<Booking>>      = dao.getAllBookings()
    val upcomingBookings: Flow<List<Booking>> = dao.getUpcomingBookings()
    val pastBookings: Flow<List<Booking>>     = dao.getPastBookings()
    val nextUpcomingBooking: Flow<Booking?>   = dao.getNextUpcomingBooking()

    suspend fun insertBooking(booking: Booking): Long = dao.insertBooking(booking)

    suspend fun cancelBooking(bookingId: Int) = dao.cancelBooking(bookingId)

    suspend fun markPastBookingsCompleted(today: String) =
        dao.markPastBookingsCompleted(today)

    /**
     * Check if a slot is already taken. Returns true if the slot is free.
     */
    suspend fun isSlotAvailable(machineId: Int, date: String, timeSlot: String): Boolean {
        val count = dao.countOverlappingBookings(machineId, date, timeSlot)
        return count == 0
    }
}

