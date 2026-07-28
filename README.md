# Photo Translate - Android App

A production-ready photo translation app using CameraX + ML Kit Text Recognition v2 + ML Kit Translate, built with Clean Architecture, Hilt, and Coroutines.

## Project Structure

```
photoTranslate/
├── app/                  # Android module
├── .github/              # CI/CD configurations
├── build.gradle          # Project-level build config
├── gradle.properties     # Gradle project settings
├── gradle-wrapper.properties # Gradle wrapper distribution
├── gradlew               # Gradle wrapper script
├── local.properties.template # SDK path template
└── README.md             # This file
```

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (or command-line Android SDK)
- [JDK 17](https://adoptium.net/)
- Git

### Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd photoTranslate
```

2. **Configure Android SDK path**
```bash
cp local.properties.template local.properties
# Edit local.properties and set sdk.dir
echo "sdk.dir=/path/to/your/android/sdk" > local.properties
```

3. **Open in Android Studio** and wait for Gradle sync to complete

## Building the APK

### Option 1: Android Studio (Recommended)
- Open the project in Android Studio
- Select **Build > Build Bundle(s)/APK(s) > Build APK**
- Find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Command Line
```bash
# Build debug APK
./gradlew :app:assembleDebug

# Build release APK (with signing config)
./gradlew :app:assembleRelease
```

### Option 3: GitHub Actions (Auto-build)
Push code to GitHub, and the CI workflow will automatically:
- Build the APK on every push to `master` or `main`
- Run lint checks
- Upload the APK as a Build Artifact

## Features

- 📸 **CameraX** with real-time preview and image capture
- 📝 **ML Kit Text Recognition v2** for OCR
- 🌍 **ML Kit Translate** for instant translation
- 🔄 **Live mode** with text change detection (5-char threshold, 300ms throttle)
- 📜 **History** with Room persistence
- 🎨 **Material 3 DayNight** theme with dark mode support
- 🔐 **Runtime permission handling** (camera, storage)
- 📱 **Offline-aware** translation with model management
- 🔌 **Pluggable translation engine** interface

## Architecture

```
Presentation Layer (UI)
    ↓
ViewModel → Use Cases (Business Logic)
    ↓
Repositories → Data Sources (ML Kit, Room, Remote)
    ↓
Domain Layer (Models, Interfaces)
    ↓
Data Layer (Room DAOs, ML Kit Wrappers)
```

Dependency Injection: [Hilt](https://dagger.dev/hilt/)

## Dependencies

| Component | Version |
|-----------|---------|
| AGP | 8.2.2 |
| Kotlin | 1.9.0 |
| Hilt | 2.52 |
| CameraX | 1.4.0-alpha06 |
| ML Kit OCR | 16.0.0 |
| ML Kit Translate | 16.0.0 |
| Room | 2.6.1 |
| Navigation | 2.7.7 |
| Coil | 2.5.0 |

## License

MIT License - See LICENSE file for details.
