# 📱 HealthTrack: Advanced BMI & Metabolic Calculator

**HealthTrack** is a modern, privacy-focused Android application designed to help users monitor their health metrics with precision. Built entirely with **Kotlin** and **Jetpack Compose**, it goes beyond simple BMI calculation by offering advanced metabolic insights (BMR, TDEE, Ideal Weight) and historical tracking.

The app emphasizes a clean, Material 3 user experience and ensures all data is persisted locally for maximum privacy.

## 🌟 Key Features

### ⚡ Real-Time Health Analytics
* **Instant BMI Calculation:** Automatic World Health Organization (WHO) classification.
* **Advanced Metrics:** Calculates **Basal Metabolic Rate (BMR)** using the Mifflin-St Jeor equation, **Daily Calorie Needs (TDEE)**, and **Ideal Weight** (Devine Formula).
* **Validation:** Robust input validation prevents calculation errors (e.g., age limits, realistic weight/height ranges).

### 📊 Interactive Visual Analytics
* **Custom Charts:** Features a BMI Evolution Chart rendered using the native **Compose Canvas API** (no external heavy libraries).
* **Visual Feedback:** Visualizes progress with color-coded data points (Blue/Green/Orange/Red) and dynamic threshold lines.

### 📅 Smart History Tracking
* **Local Persistence:** All records are securely stored in a local **Room Database**.
* **Detailed View:** Users can view a chronological list and deep-dive into specific past records.

### 🔔 Intelligent Reminders
* **Weekly Notifications:** Integrated **WorkManager** logic to schedule reliable weekly reminders to log weight.
* **Permission Handling:** Fully compatible with Android 13+ notification standards.

---

## 📸 Screenshots

|               Home Screen               | History & Chart | Details |
|:---------------------------------------:|:---------------:|:-------:|
| <img src="images/home.png" width="250"> | <img src="images/history.png" width="250"> | <img src="images/details.png" width="250"> |
---

## 🛠️ Technical Architecture & Decisions

This project follows modern Android development standards, prioritizing scalability, testability, and clean code principles.

### 1. Architecture Pattern: MVVM & Unidirectional Data Flow
The application implements the **Model-View-ViewModel (MVVM)** architecture:
* **View (Composables):** Pure UI functions that are "dumb" regarding logic. They strictly observe state and dispatch events.
* **ViewModel:** Acts as the state holder, surviving configuration changes and exposing a single source of truth (`StateFlow`).
* **UDF:** Events flow UP, Data flows DOWN.

### 2. Domain Layer & Separation of Concerns
Business logic is decoupled from the UI:
* **Use Case (`CalculateBMIUseCase.kt`):** Encapsulates core algorithms (Mifflin-St Jeor, Devine). This makes the math pure and independent of the Android framework.

### 3. Data Persistence (Room)
* **Entity:** The `BMIRecord` stores raw inputs (age, gender, activity) alongside calculated metrics, allowing for future re-calculations or detailed analysis.
* **Reactive UI:** The DAO returns `Flow<List<BMIRecord>>`, ensuring the History screen updates instantly upon saving.

### 4. Precision Logic
* **Decimal Handling:** We implemented **Cascading Comparison Operators** (e.g., `bmi < 25.0`) instead of standard ranges (`..`) to strictly handle floating-point precision (avoiding "gaps" like 24.95).

---

## 🚀 Tech Stack

* **Language:** Kotlin 2.0
* **UI:** Jetpack Compose (Material Design 3)
* **Navigation:** Jetpack Navigation Compose
* **Database:** Room (SQLite) with KSP
* **Background Tasks:** WorkManager
* **Graphics:** Native Compose Canvas

---

## 🧠 Project Learnings

This project was developed to consolidate advanced Android concepts, including:
* Migrating legacy imperative logic to **Reactive MVVM**.
* Implementing **Custom Graphics** with Canvas instead of relying on third-party libraries.
* Managing modern dependency versions (Kotlin 2.0 / KSP) and resolving build conflicts.
* Implementing strict **Input Validation** and **Unit Testing** logic principles.

---

## ▶️ How to Run

1.  Clone this repository.
2.  Open in **Android Studio Ladybug** (or newer).
3.  Sync Gradle (Ensure JDK 17+ is selected).
4.  Run on an Emulator or Device (Min SDK 24 recommended).