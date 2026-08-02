# Resale Scanner

Native Android inventory and sourcing companion for resellers. Sprint 1 establishes a Kotlin, Jetpack Compose, Material 3, MVVM, Room, CameraX, and ML Kit foundation.

## Requirements

- Android Studio with JDK 17
- Android SDK 35
- Android 8.0 (API 26) or newer device/emulator

Open the repository in Android Studio, let Gradle sync, and run the `app` configuration. Camera scanning works best on a physical device.

## Architecture

- `data`: Room persistence, repository implementation, file exporters
- `domain`: inventory model and repository contracts
- `ui`: Compose screens, navigation, and lifecycle-aware ViewModels
- `scanner`: CameraX preview and ML Kit barcode analysis

