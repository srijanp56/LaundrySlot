package com.example.laundryslot.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the status of a washing machine.
 * Unit IV: Room Entity with enum type (TypeConverter needed)
 */
enum class MachineStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE
}

/**
 * Room Entity: Washing Machine
 * Unit IV: @Entity, @PrimaryKey
 */
@Entity(tableName = "machines")
data class Machine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,           // e.g. "Machine 1"
    val location: String,       // e.g. "Ground Floor"
    val status: MachineStatus,
    val capacity: String = "7 kg",
    val lastServiced: String = "N/A"
)

