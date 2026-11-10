package com.example.laundryslot.data

import androidx.room.TypeConverter

/**
 * Unit IV: TypeConverters — Room can't store enums directly.
 * We convert them to/from String for storage.
 */
class Converters {

    @TypeConverter
    fun fromMachineStatus(status: MachineStatus): String = status.name

    @TypeConverter
    fun toMachineStatus(value: String): MachineStatus = MachineStatus.valueOf(value)

    @TypeConverter
    fun fromBookingStatus(status: BookingStatus): String = status.name

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus = BookingStatus.valueOf(value)

    @TypeConverter
    fun fromServiceType(type: ServiceType): String = type.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType = ServiceType.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)
}
