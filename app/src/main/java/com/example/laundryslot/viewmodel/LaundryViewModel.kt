package com.example.laundryslot.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.laundryslot.data.Booking
import com.example.laundryslot.data.BookingStatus
import com.example.laundryslot.data.LaundryDatabase
import com.example.laundryslot.data.LaundryRepository
import com.example.laundryslot.data.Machine
import com.example.laundryslot.data.MachineStatus
import com.example.laundryslot.data.PaymentMethod
import com.example.laundryslot.data.PaymentStatus
import com.example.laundryslot.data.ServiceType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "LaundryViewModel"

/**
 * Laundry Package for Full-Service bookings
 */
data class LaundryPackage(
    val id: String,
    val name: String,
    val description: String,
    val pricePerCloth: Double,
    val estimatedTime: String
)

/**
 * Unit II & III: ViewModel using AndroidViewModel (needs Application context for Room),
 * SavedStateHandle, viewModelScope, StateFlow, SharedFlow.
 */
class LaundryViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository: LaundryRepository

    // ─── Init ──────────────────────────────────────────────────────────────────
    init {
        val dao = LaundryDatabase.getDatabase(application).laundryDao()
        repository = LaundryRepository(dao)

        // On startup, mark past bookings as completed
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                repository.markPastBookingsCompleted(today)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking past bookings: ${e.message}")
            }
        }
    }

    // ─── Machine State ─────────────────────────────────────────────────────────

    /** All machines — observed as StateFlow for Compose */
    val machines: StateFlow<List<Machine>> = repository.allMachines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCount: StateFlow<Int> = repository.availableCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val occupiedCount: StateFlow<Int> = repository.occupiedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMachineCount: StateFlow<Int> = repository.totalMachineCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ─── Booking State ─────────────────────────────────────────────────────────

    val upcomingBookings: StateFlow<List<Booking>> = repository.upcomingBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastBookings: StateFlow<List<Booking>> = repository.pastBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextUpcomingBooking: StateFlow<Booking?> = repository.nextUpcomingBooking
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ─── Booking Form State (SavedStateHandle survives process death) ──────────

    private val _selectedMachineId = MutableStateFlow<Int?>(
        savedStateHandle.get("selectedMachineId")
    )
    val selectedMachineId: StateFlow<Int?> = _selectedMachineId.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        savedStateHandle.get("selectedDate") ?: ""
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow(
        savedStateHandle.get("selectedTimeSlot") ?: ""
    )
    val selectedTimeSlot: StateFlow<String> = _selectedTimeSlot.asStateFlow()

    // ─── Full Service Form State ───────────────────────────────────────────────

    val availablePackages = listOf(
        LaundryPackage(
            id = "wash_iron",
            name = "Wash & Steam Iron",
            description = "Complete wash, tumble dry & crisp steam iron",
            pricePerCloth = 15.0,
            estimatedTime = "Next day delivery"
        ),
        LaundryPackage(
            id = "iron_only",
            name = "Steam Iron Only",
            description = "Heavy steam press & hanger fold",
            pricePerCloth = 8.0,
            estimatedTime = "Same day delivery (4 hrs)"
        ),
        LaundryPackage(
            id = "wash_fold",
            name = "Wash & Fold Only",
            description = "Gentle wash, fragrance softener & neatly folded",
            pricePerCloth = 10.0,
            estimatedTime = "Next day delivery"
        )
    )

    private val _selectedPackage = MutableStateFlow(availablePackages[0])
    val selectedPackage: StateFlow<LaundryPackage> = _selectedPackage.asStateFlow()

    private val _clothesCount = MutableStateFlow(10)
    val clothesCount: StateFlow<Int> = _clothesCount.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _instructions = MutableStateFlow("")
    val instructions: StateFlow<String> = _instructions.asStateFlow()

    // ─── UI Event State ────────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // ─── Authentication State ──────────────────────────────────────────────────

    private val _isLoggedIn = MutableStateFlow(
        savedStateHandle.get("isLoggedIn") ?: false
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _studentRollNumber = MutableStateFlow(
        savedStateHandle.get("rollNumber") ?: "CS21045"
    )
    val studentRollNumber: StateFlow<String> = _studentRollNumber.asStateFlow()

    // ─── Profile State ─────────────────────────────────────────────────────────

    private val _studentName = MutableStateFlow(
        savedStateHandle.get("studentName") ?: "Rahul Sharma"
    )
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _hostelName = MutableStateFlow(
        savedStateHandle.get("hostelName") ?: "Sunrise Boys Hostel"
    )
    val hostelName: StateFlow<String> = _hostelName.asStateFlow()

    private val _roomNumber = MutableStateFlow(
        savedStateHandle.get("roomNumber") ?: "204"
    )
    val roomNumber: StateFlow<String> = _roomNumber.asStateFlow()

    // ─── Authentication Actions ────────────────────────────────────────────────

    fun login(name: String, roll: String, hostel: String, room: String): Boolean {
        if (name.isBlank() || roll.isBlank() || room.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Please fill all required student details."))
            }
            return false
        }
        _studentName.update { name.trim() }
        _studentRollNumber.update { roll.trim() }
        _hostelName.update { if (hostel.isBlank()) "Sunrise Boys Hostel" else hostel.trim() }
        _roomNumber.update { room.trim() }
        _isLoggedIn.update { true }

        savedStateHandle["studentName"] = name.trim()
        savedStateHandle["rollNumber"] = roll.trim()
        savedStateHandle["hostelName"] = if (hostel.isBlank()) "Sunrise Boys Hostel" else hostel.trim()
        savedStateHandle["roomNumber"] = room.trim()
        savedStateHandle["isLoggedIn"] = true

        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowMessage("Welcome back, ${name.trim().split(" ").first()}!"))
        }
        return true
    }

    fun quickDemoLogin() {
        login(
            name   = "Rahul Sharma",
            roll   = "CS21045",
            hostel = "Sunrise Boys Hostel",
            room   = "204"
        )
    }

    fun logout() {
        _isLoggedIn.update { false }
        savedStateHandle["isLoggedIn"] = false
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowMessage("Logged out successfully."))
        }
    }

    // ─── Actions ───────────────────────────────────────────────────────────────

    fun selectMachine(machineId: Int) {
        _selectedMachineId.update { machineId }
        savedStateHandle["selectedMachineId"] = machineId
        Log.d(TAG, "Machine selected: $machineId")
    }

    fun selectDate(date: String) {
        _selectedDate.update { date }
        savedStateHandle["selectedDate"] = date
    }

    fun selectTimeSlot(slot: String) {
        _selectedTimeSlot.update { slot }
        savedStateHandle["selectedTimeSlot"] = slot
    }

    fun selectPackage(pkg: LaundryPackage) {
        _selectedPackage.update { pkg }
    }

    fun setClothesCount(count: Int) {
        if (count in 1..50) {
            _clothesCount.update { count }
        }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.update { method }
    }

    fun setInstructions(notes: String) {
        _instructions.update { notes }
    }

    // ─── Self-Service Slot Booking ─────────────────────────────────────────────

    fun createBooking() {
        val machineId = _selectedMachineId.value
        val date      = _selectedDate.value
        val timeSlot  = _selectedTimeSlot.value

        if (machineId == null || date.isBlank() || timeSlot.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Please select a machine, date and time slot."))
            }
            return
        }

        val machine = machines.value.find { it.id == machineId }
        if (machine == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Selected machine not found."))
            }
            return
        }

        if (machine.status == MachineStatus.MAINTENANCE) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("This machine is under maintenance."))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.update { true }
            try {
                val available = repository.isSlotAvailable(machineId, date, timeSlot)
                if (!available) {
                    _uiEvent.emit(UiEvent.ShowError("This slot is already booked. Please choose another."))
                    return@launch
                }

                val booking = Booking(
                    serviceType = ServiceType.SELF_SERVICE,
                    machineId   = machineId,
                    machineName = machine.name,
                    studentName = _studentName.value,
                    roomNumber  = _roomNumber.value,
                    date        = date,
                    timeSlot    = timeSlot,
                    status      = BookingStatus.UPCOMING,
                    packageType = "Self Wash Slot",
                    totalAmount = 0.0,
                    paymentStatus = PaymentStatus.PAID
                )

                repository.insertBooking(booking)
                clearBookingForm()
                _uiEvent.emit(UiEvent.BookingSuccess("Slot confirmed for $date, $timeSlot"))
                Log.d(TAG, "Self-service booking created: $date $timeSlot for machine $machineId")

            } catch (e: Exception) {
                Log.e(TAG, "Error creating booking: ${e.message}")
                _uiEvent.emit(UiEvent.ShowError("Failed to create booking. Try again."))
            } finally {
                _isLoading.update { false }
            }
        }
    }

    // ─── Full-Service (Wash + Iron) Booking with Payment ────────────────────────

    fun createFullServiceBooking(
        pickupDate: String,
        timeSlot: String,
        simulatedPaymentSuccess: Boolean = true
    ) {
        if (pickupDate.isBlank() || timeSlot.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Please select pickup date and time."))
            }
            return
        }

        val pkg = _selectedPackage.value
        val count = _clothesCount.value
        val total = pkg.pricePerCloth * count
        val method = _selectedPaymentMethod.value
        val paymentStatus = if (method == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.PENDING else PaymentStatus.PAID

        viewModelScope.launch {
            _isLoading.update { true }
            try {
                val booking = Booking(
                    serviceType   = ServiceType.FULL_SERVICE,
                    machineId     = 0,
                    machineName   = "Hostel Laundry Staff",
                    studentName   = _studentName.value,
                    roomNumber    = _roomNumber.value,
                    date          = pickupDate,
                    timeSlot      = timeSlot,
                    status        = BookingStatus.UPCOMING,
                    packageType   = pkg.name,
                    clothesCount  = count,
                    totalAmount   = total,
                    paymentMethod = method,
                    paymentStatus = paymentStatus,
                    instructions  = _instructions.value.trim()
                )

                repository.insertBooking(booking)
                clearBookingForm()
                val paymentMsg = if (method == PaymentMethod.UPI) "Paid ₹${total.toInt()} via UPI"
                                 else if (method == PaymentMethod.HOSTEL_WALLET) "Deducted ₹${total.toInt()} from Hostel Wallet"
                                 else "Pay ₹${total.toInt()} on Pickup/Delivery"

                _uiEvent.emit(UiEvent.BookingSuccess("Order Confirmed! ($paymentMsg)"))
                Log.d(TAG, "Full-service booking created: ${pkg.name}, $count clothes, total ₹$total")

            } catch (e: Exception) {
                Log.e(TAG, "Error creating full service booking: ${e.message}")
                _uiEvent.emit(UiEvent.ShowError("Failed to place order. Try again."))
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                repository.cancelBooking(bookingId)
                _uiEvent.emit(UiEvent.ShowMessage("Booking cancelled successfully."))
                Log.d(TAG, "Booking $bookingId cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling booking: ${e.message}")
                _uiEvent.emit(UiEvent.ShowError("Failed to cancel booking."))
            }
        }
    }

    fun clearBookingForm() {
        _selectedMachineId.update { null }
        _selectedDate.update { "" }
        _selectedTimeSlot.update { "" }
        _instructions.update { "" }
        savedStateHandle.remove<Int>("selectedMachineId")
        savedStateHandle.remove<String>("selectedDate")
        savedStateHandle.remove<String>("selectedTimeSlot")
    }

    // ─── Profile Actions ───────────────────────────────────────────────────────

    fun updateProfile(name: String, hostel: String, room: String) {
        _studentName.update { name }
        _hostelName.update { hostel }
        _roomNumber.update { room }
        savedStateHandle["studentName"] = name
        savedStateHandle["hostelName"]  = hostel
        savedStateHandle["roomNumber"]  = room
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowMessage("Profile updated successfully."))
        }
    }

    fun getTimeSlots(): List<String> = listOf(
        "06:00 - 07:00",
        "07:00 - 08:00",
        "08:00 - 09:00",
        "09:00 - 10:00",
        "10:00 - 11:00",
        "11:00 - 12:00",
        "14:00 - 15:00",
        "15:00 - 16:00",
        "16:00 - 17:00",
        "17:00 - 18:00",
        "19:00 - 20:00",
        "20:00 - 21:00"
    )

    fun getFullServiceSlots(): List<String> = listOf(
        "Morning Pickup (07:00 - 09:00)",
        "Afternoon Pickup (12:00 - 14:00)",
        "Evening Pickup (17:00 - 19:00)",
        "Night Drop (20:00 - 21:30)"
    )

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application    = checkNotNull(extras[APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                return LaundryViewModel(application, savedStateHandle) as T
            }
        }
    }
}

/** Sealed class for one-time UI events sent via SharedFlow */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class ShowError(val message: String)   : UiEvent()
    data class BookingSuccess(val message: String) : UiEvent()
}
