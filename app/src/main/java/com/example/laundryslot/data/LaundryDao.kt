package com.example.laundryslot.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Unit IV: Data Access Object — defines all database operations.
 * Unit II: Returns Flow for reactive/real-time updates.
 */
@Dao
interface LaundryDao {

    // ─── Machine Queries ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: Machine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMachines(machines: List<Machine>)

    @Update
    suspend fun updateMachine(machine: Machine)

    @Query("SELECT * FROM machines ORDER BY id ASC")
    fun getAllMachines(): Flow<List<Machine>>

    @Query("SELECT * FROM machines WHERE id = :machineId")
    suspend fun getMachineById(machineId: Int): Machine?

    @Query("SELECT COUNT(*) FROM machines WHERE status = 'AVAILABLE'")
    fun getAvailableCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM machines WHERE status = 'OCCUPIED'")
    fun getOccupiedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM machines")
    fun getTotalMachineCount(): Flow<Int>

    // ─── Booking Queries ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE status = 'UPCOMING' ORDER BY date ASC, timeSlot ASC")
    fun getUpcomingBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE status != 'UPCOMING' ORDER BY createdAt DESC")
    fun getPastBookings(): Flow<List<Booking>>

    /**
     * Check for overlapping booking — prevents double-booking the same machine + date + slot.
     * Unit VII: Business logic implemented via SQL query.
     */
    @Query("""
        SELECT COUNT(*) FROM bookings 
        WHERE machineId = :machineId 
        AND date = :date 
        AND timeSlot = :timeSlot 
        AND status = 'UPCOMING'
    """)
    suspend fun countOverlappingBookings(machineId: Int, date: String, timeSlot: String): Int

    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :bookingId")
    suspend fun cancelBooking(bookingId: Int)

    @Query("UPDATE bookings SET status = 'COMPLETED' WHERE status = 'UPCOMING' AND date < :today")
    suspend fun markPastBookingsCompleted(today: String)

    @Query("SELECT * FROM bookings WHERE status = 'UPCOMING' LIMIT 1")
    fun getNextUpcomingBooking(): Flow<Booking?>
}

