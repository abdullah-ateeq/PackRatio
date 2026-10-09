# PackRatio 📦⚡

**PackRatio** is a modern, offline Android application designed for inventory management, packaging logistics, and bulk distribution calculations. Built with **Jetpack Compose** and **Material 3**, PackRatio converts raw bulk inventory into formatted, complete packages with high mathematical precision—calculating package counts, leftovers, utilization percentages, and mathematical formulas step-by-step.

---

## 🌟 Key Features

- 🎯 **High-Precision Packaging Calculations**:
  - Computes complete packages, packaged quantities, remaining leftovers, and utilization percentages using exact `BigDecimal` arithmetic.
  - Detects status automatically (`Fully Packed`, `Leftover`, `Package Too Large`).
  
- ⚖️ **Multi-Unit & Compatible Conversions**:
  - Supports **Items** (Pieces), **Weight** (mg, g, kg, oz, lb), and **Volume** (ml, L, fl oz, gal).
  - Mix & match compatible units seamlessly (e.g., Bulk quantity in **kg** and Package size in **g**).

- 🧮 **Step-by-Step Formula Visualizer**:
  - Interactive mathematical breakdown showing base unit normalizations, integer division, remaining modulo, and utilization formulas.

- 📜 **Calculation History & Search**:
  - Saves past calculation runs locally with custom package names and notes.
  - Search, filter by packaging status, and sort by date, package name, package count, or leftover amount.
  - Quickly reload historical entries directly back into the calculator.

- ⚙️ **Customization & Preferences**:
  - Support for Light Mode, Dark Mode, and System Theme with **Material 3 Dynamic Color** (Monet).
  - Configurable default quantity types, bulk amounts, and units.
  - Toggles for displaying formula cards, notes, utilization stats, and haptic feedback.

- 🔒 **100% Offline & Private**:
  - No internet connection required. All preferences and history remain securely on-device using Jetpack DataStore.

---

## 📊 Supported Units & Quantity Types

| Quantity Type | Base Unit | Supported Units |
| :--- | :--- | :--- |
| **Items** 🧩 | Pieces | `pieces` |
| **Weight** ⚖️ | Milligrams | `mg`, `g`, `kg`, `oz`, `lb` |
| **Volume** 🧪 | Milliliters | `ml`, `L`, `fl oz`, `gal` (US) |

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture Principles
- **State Management**: Kotlin Coroutines, `StateFlow`, `SharedFlow`
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) + JSON Serialization
- **Design System**: Material Design 3, Dynamic Colors, Haptic Feedback integration

---

## 📁 Project Structure

```
com.example.packratio
├── calculator       # Pure, high-precision BigDecimal calculation engine
├── data             # DataStore preferences & JSON history persistence
├── model            # Domain models (PackRatioInput, Result, Units, Settings)
├── ui
│   ├── navigation   # Bottom bar navigation & graph definitions
│   ├── screens      # Compose screens (Calculator, History, Settings, About, Splash)
│   └── theme        # Material 3 Color palette, Typography, and Themes
├── util             # Haptic feedback & UI utility helpers
└── viewmodel        # ViewModel handling state flows & UI interactions
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / 2024.2.1 or newer (recommended: Android Studio Meerkat / Jellyfish or latest)
- **JDK**: Java 11 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 37

### Building from Source

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/PackRatio.git
   cd PackRatio
   ```

2. **Open in Android Studio**:
   Open Android Studio and select `Open an Existing Project`, targeting the root directory.

3. **Build & Run**:
   Run the application on an emulator or a connected physical Android device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Testing

The codebase includes unit test coverage for calculation logic and data serialization:

```bash
# Run unit tests
./gradlew test
```

Unit test suites:
- `PackRatioCalculatorTest`: Verifies exact multi-unit conversions, edge cases, zero handling, and utilization math.
- `HistoryJsonSerializerTest`: Validates serialization & deserialization of history records.

---

## 📄 License

```text
Copyright (c) 2026 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the opacity for the specific language governing permissions and limitations under the License.
```
