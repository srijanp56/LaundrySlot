# P21 – LaundrySlot 🧺

**Hostel Laundry Management & Washing Machine Slot Booking Android Application**

Developed for 7th Semester Android Development Practical Examination.

---

## 📱 Project Overview

**LaundrySlot** is a modern, offline-first Android application designed to help hostel students manage their laundry schedules efficiently. It eliminates hostel queues by allowing students to check washing machine availability in real-time, reserve slots without double-booking conflicts, cancel reservations, and navigate to hostel laundry rooms.

---

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Declarative UI)
- **Design System:** Material 3 (Dynamic Color, Cards, Navigation Bar, Typography)
- **Architecture:** MVVM (Model - View - ViewModel) + Repository Pattern
- **Local Persistence:** Room Database (SQLite abstraction, TypeConverters, Foreign Keys)
- **Asynchronous Programming:** Kotlin Coroutines & Suspending functions
- **Reactive Streams:** Kotlin Flow, StateFlow, SharedFlow
- **Navigation:** Jetpack Navigation Compose
- **Location Services:** Google Play Services Location (`FusedLocationProviderClient`) + Geocoder
- **Unit Testing:** JUnit 4 + Compose UI Testing (`createComposeRule`)

---

## 🚀 Key Features

1. **Student Authentication & Login:**
   - Student credential input: Name, Roll / Student ID, Hostel, Room number, and Password.
   - **⚡ 1-Tap Quick Viva Demo Login:** Instant auto-fill and sign-in for seamless college examiner evaluations.
   - Offline session persistence across app restarts via `SavedStateHandle` and ViewModel.
   - Logout option on Profile Screen with confirmation dialog.

2. **Live Dashboard (Home):**
   - Real-time statistics: Total, Available, and Occupied machines.
   - Quick overview of the upcoming laundry booking.
   - Entry animations using Compose `AnimatedVisibility`.

2. **Washing Machine Directory (Machines):**
   - Status indicators: `Available` (Green), `Occupied` (Orange), `Maintenance` (Red).
   - Staggered entry animation using `LazyColumn`.
   - Detailed dialog modal showing machine capacity and location.

3. **Dual-Mode Laundry Booking (Book):**
   - **Mode A: Self-Service Machine Slot:** Free washing machine queue booking with overlap conflict prevention.
   - **Mode B: All-in-One Full Service (Wash + Iron):**
     - Select package: *Wash & Steam Iron (₹15/cloth)*, *Steam Iron Only (₹8/cloth)*, *Wash & Fold (₹10/cloth)*.
     - Dynamic garment quantity counter with real-time bill calculation.
     - Pickup date and slot scheduling with custom student instructions.
     - **Integrated Payment Gateway Simulation:**
       - **UPI (GPay / PhonePe / Paytm / BHIM)** with interactive UPI QR Code Dialog.
       - **Cash on Delivery / Pay at Laundry Desk**.
       - **Hostel Student ID Wallet**.

4. **Booking History & Order Tracking (My Bookings):**
   - Clear indicators for *Self-Service Wash* vs *Full Service (Wash + Iron)*.
   - Live payment status tags (*₹150 Paid via UPI*, *Pay on Delivery*, *Free Slot*).
   - One-tap cancellation with refund confirmation dialog.

5. **Student Profile (Profile):**
   - View and update student name, hostel name, and room number.
   - Laundry room timings and contact info.

6. **Location & Distance Tracker (Location):**
   - Runtime location permission handling using Accompanist.
   - Current GPS coordinate extraction with `FusedLocationProviderClient`.
   - Reverse Geocoding via Android `Geocoder` (coordinates to address).
   - Real-time Haversine distance computation to hostel laundry facilities.

---

## 🏗️ Architecture & Data Flow

```
[ UI Layer (Jetpack Compose Screens) ]
                ↕  (StateFlow / Events)
      [ LaundryViewModel ]
                ↕  (Suspend / Flow)
     [ LaundryRepository ]
                ↕  (DAO Calls)
   [ Room Database (SQLite) ]
```

- **Single Source of Truth:** Room Database acts as the single source of truth; UI state reacts to reactive DB flows.
- **State Hoisting:** ViewModel manages state and business rules, surviving screen rotations.
- **SavedStateHandle:** Retains form inputs across process recreation.

---

## 📚 Syllabus Concepts Checklist

| Unit | Syllabus Topic | Implementation in LaundrySlot |
|---|---|---|
| **Unit I** | Advanced Jetpack Compose | Material 3, `LazyColumn`, `AnimatedVisibility`, `animateColorAsState`, `LaunchedEffect`, `DisposableEffect`, `FlowRow` |
| **Unit II** | Kotlin Coroutines & Flow | `viewModelScope`, `Dispatchers.IO`, `StateFlow` (`stateIn`), `SharedFlow` (`UiEvent`), `collectAsState()` |
| **Unit III** | Architecture Components | MVVM, `AndroidViewModel`, `SavedStateHandle`, `ViewModelProvider.Factory`, Repository Pattern |
| **Unit IV** | Room Database | `@Entity`, `@PrimaryKey`, `@ForeignKey`, `@TypeConverter`, `@Dao`, Room `Flow`, Pre-population callback |
| **Unit V** | Location-Based Services | Runtime permissions, `FusedLocationProviderClient`, `Geocoder` reverse geocoding, Haversine formula |
| **Unit VI** | Testing & Git | JUnit tests (`BookingLogicTest`), Compose UI tests (`LaundrySlotUiTest`), `.gitignore`, Git versioning |

---

## 🧪 Running Tests

### Unit Tests
```bash
./gradlew test
```

### Android UI Tests
```bash
./gradlew connectedAndroidTest
```

---

## 💻 How to Run the App

1. Open **Android Studio** and choose **Open**.
2. Select the `LaundrySlot` folder.
3. Allow Gradle to sync dependencies.
4. Select an Android emulator (API 26+) or physical device.
5. Click the green **Run (▶)** button.

