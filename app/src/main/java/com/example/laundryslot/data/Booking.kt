package com.example.laundryslot.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Service options:
 * - SELF_SERVICE: Student books a washing machine slot to wash their own clothes.
 * - FULL_SERVICE: Hostel staff takes clothes, washes, dries, irons, and delivers back.
 */
enum class ServiceType {
    SELF_SERVICE,
    FULL_SERVICE
}

/**
 * Payment options for laundry services.
 */
enum class PaymentMethod {
    UPI,                 // GPay, PhonePe, Paytm
    CASH_ON_DELIVERY,    // Pay at Laundry Desk / Cash
    HOSTEL_WALLET        // Student ID Card / Hostel balance
}

enum class PaymentStatus {
    PAID,
    PENDING
}

enum class BookingStatus {
    UPCOMING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Room Entity: Booking
 * Unit IV: @Entity with support for both self-service machine slots
 * and full-service wash + iron requests with payment tracking.
 */
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serviceType: ServiceType = ServiceType.SELF_SERVICE,
    val machineId: Int = 0,             // 0 if Full-Service (handled by staff)
    val machineName: String = "",       // e.g., "Machine 1" or "Staff Wash & Iron Desk"
    val studentName: String,
    val roomNumber: String,
    val date: String,                   // Format: "dd/MM/yyyy"
    val timeSlot: String,               // Format: "09:00 - 10:00" or "Morning Slot"
    val status: BookingStatus = BookingStatus.UPCOMING,
    // Full service & payment details
    val packageType: String = "Self Wash", // "Wash & Iron", "Wash & Fold", "Iron Only"
    val clothesCount: Int = 0,             // Number of garments
    val totalAmount: Double = 0.0,         // Total price in INR (₹)
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val instructions: String = "",         // Special student notes
    val createdAt: Long = System.currentTimeMillis()
)
