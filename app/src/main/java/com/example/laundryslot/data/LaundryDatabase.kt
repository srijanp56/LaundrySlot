package com.example.laundryslot.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Unit IV: Room Database class.
 * - Singleton pattern to prevent multiple instances.
 * - Callback pre-populates with sample machine data.
 * - version = 2 supports full-service laundry and payment tracking.
 */
@Database(
    entities  = [Machine::class, Booking::class],
    version   = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LaundryDatabase : RoomDatabase() {

    abstract fun laundryDao(): LaundryDao

    companion object {
        @Volatile
        private var INSTANCE: LaundryDatabase? = null

        fun getDatabase(context: Context): LaundryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LaundryDatabase::class.java,
                    "laundry_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback that runs when the database is created for the first time.
     * Pre-populates it with 5 washing machines.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateMachines(database.laundryDao())
                }
            }
        }

        suspend fun populateMachines(dao: LaundryDao) {
            val machines = listOf(
                Machine(id = 1, name = "Machine 1", location = "Ground Floor - Room A", status = MachineStatus.AVAILABLE,   capacity = "7 kg", lastServiced = "01/11/2026"),
                Machine(id = 2, name = "Machine 2", location = "Ground Floor - Room A", status = MachineStatus.AVAILABLE,   capacity = "7 kg", lastServiced = "01/11/2026"),
                Machine(id = 3, name = "Machine 3", location = "Ground Floor - Room B", status = MachineStatus.OCCUPIED,    capacity = "9 kg", lastServiced = "15/10/2026"),
                Machine(id = 4, name = "Machine 4", location = "First Floor - Room C",  status = MachineStatus.AVAILABLE,   capacity = "7 kg", lastServiced = "28/10/2026"),
                Machine(id = 5, name = "Machine 5", location = "First Floor - Room C",  status = MachineStatus.MAINTENANCE, capacity = "9 kg", lastServiced = "10/09/2026")
            )
            dao.insertAllMachines(machines)
        }
    }
}
