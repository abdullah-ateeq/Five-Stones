# Five Stones ⚪⚫

A modern, beautifully designed Android implementation of the classic **Five in a Row** (Gomoku / 五子棋) strategy board game, built with **Jetpack Compose**, **Material 3**, and modern Android architectural patterns.

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-green)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-brightgreen)

---

## 🌟 Features

* **Classic Strategy Gameplay**: Two players alternate placing black and white stones on a grid. The first to align five consecutive stones horizontally, vertically, or diagonally wins!
* **Customizable Board Configurations**: Choose between classic board dimensions including **11x11**, **13x13**, **15x15** (standard Gomoku), and **19x19** (Go board size).
* **Multiple Visual Board Styles**:
  * 🪵 **Classic Wood**
  * 🪨 **Modern Slate**
  * 🎋 **Bamboo**
  * ⚪ **Minimalist**
  * 🪵 **Dark Walnut**
* **Theme Customization**: Support for Light Mode, Dark Mode, and System Default themes.
* **Seamless State & Game Persistence**: Automatically saves game state and preferences using **Jetpack DataStore**. Resume ongoing matches seamlessly anytime.
* **Undo & Move History**: Replay moves or undo accidental placements with immediate UI and engine synchronization.
* **Statistics & Score Tracking**: Built-in player statistics tracking total games, Player 1 wins, Player 2 wins, and draws.
* **Sound Effects & Audio Feedback**: Tactile sound effects for stone placement, victories, and draws, with toggleable settings.
* **Interactive Guide**: Built-in "How to Play" screen with rules and diagrams showing winning patterns.

---

## 📱 Screenshots & Previews

The app features an adaptive Jetpack Compose layout supporting smooth canvas rendering for custom board grid geometry, winning line animations, and custom stone drawing.

---

## 🛠️ Tech Stack & Architecture

Built following Android best practices and clean code principles:

* **UI Layer**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 guidelines.
* **Architecture**: Unidirectional Data Flow (UDF) powered by `ViewModel` and `StateFlow`.
* **Pure Game Logic**: Immutable state handling in `GameRules.kt` for zero side-effect move validation, win checking, and history replaying.
* **Data Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) for persistent settings and game statistics.
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
* **Audio Engine**: Low-latency `SoundPool` sound playback for game interaction feedback.

### Project Directory Structure

```text
com.example.fivestones
├── data/              # DataStore Preferences, stats repository, and audio manager
├── game/              # Pure game rule engine, board geometry, and immutable models
└── ui/                # Jetpack Compose UI screens and components
    ├── about/         # About app info & links
    ├── board/         # Custom Canvas board drawing & touch handling
    ├── components/    # Reusable Compose buttons, bars, surfaces
    ├── game/          # Game board screen & ViewModel
    ├── home/          # Home screen with score stats & navigation
    ├── howto/         # Interactive rules & diagrams
    ├── result/        # Match outcome dialog / screen
    ├── settings/      # Theme, board style, grid size & sound settings
    ├── splash/        # Splash screen
    └── theme/         # Material 3 color palettes and typography
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug / Meerkat or newer recommended.
* **JDK**: Java 11 or higher.
* **Android SDK**: Minimum API Level 24 (Android 7.0), Target API Level 37.

### Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Five-Stones.git
   cd Five-Stones
   ```

2. **Open in Android Studio**:
   Open the root directory in Android Studio and let Gradle sync dependencies.

3. **Build & Install**:
   Run the app on an Android device or emulator via Android Studio, or build from CLI:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Testing

Unit tests for game rule evaluation, winning line checks, and geometry calculations are located under `app/src/test`. To run unit tests:

```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
