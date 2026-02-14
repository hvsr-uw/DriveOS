# DriveOS Companion

DriveOS Companion is an Android infotainment project for a software-defined vehicle experience. It shows the kind of information a driver might see on a vehicle screen: range, battery, tire pressure, cabin temperature, media, navigation, connection status, and background sync state.

The app uses a local telemetry simulator so the full dashboard flow can run from the repo without external setup. The rest of the app is structured like a real Android project: separate modules, repository layer, cache behavior, a foreground sync service, Compose UI, and unit tests.

## What It Shows

- Battery percentage and estimated range
- Cabin temperature
- Tire pressure for all four wheels
- Drive mode
- Current media track, source, volume, play/pause, and next track
- Navigation destination, ETA, distance, and route condition
- Connectivity alerts for healthy, degraded, offline, or stale data
- Whether the latest vehicle state came from network or cache
- A diagnostics panel showing refresh/cache/command events

The values change over time because `DemoVehicleTelemetryApi` generates new demo telemetry on refresh.

## Design Goal

The goal is to model the shape of an Android vehicle app where the UI is driven by data flow instead of hardcoded screen state.

The main pieces are:

- `VehicleTelemetryApi` acts like the REST API boundary.
- `VehicleRepository` coordinates cache, refresh, commands, alerts, and diagnostics.
- `SnapshotFreshnessPolicy` decides whether telemetry is fresh, aging, or stale.
- `ConnectivityAlertClassifier.java` classifies connection health in Java to show Kotlin/Java interop.
- `VehicleSyncService` keeps the demo vehicle state refreshing in the background.
- `DashboardReducer` converts domain state into display-ready UI state.

## Tech Used

- Kotlin
- Java
- Android SDK
- Jetpack Compose
- AndroidX ViewModel / Lifecycle
- Kotlin Coroutines and Flow
- Retrofit-style API interface
- JUnit
- Gradle Kotlin DSL

## Project Layout

| Module | What it does |
| --- | --- |
| `:app` | Android app entry point, manual dependency wiring, activity, manifest, foreground service. |
| `:core:model` | Shared models for vehicle state, media, navigation, connectivity, commands, and events. |
| `:core:network` | API interface, telemetry DTOs, mapping code, and demo telemetry generator. |
| `:core:data` | Repository, cache, freshness policy, alert classifier, diagnostics, and tests. |
| `:core:service` | Sync coordinator used by the foreground service and manual refresh. |
| `:feature:dashboard` | Compose UI, ViewModel, reducer, and dashboard state. |

## Documentation

Additional architecture and review guidance is available in the `docs/` folder, including:

- `docs/ARCHITECTURE.md`
- `docs/REVIEWER_GUIDE.md`
- `docs/ARCHITECTURE_OVERVIEW.md`
- `docs/HOW_IT_WORKS.md`
- `docs/USE_CASES.md`

## Data Flow

```text
VehicleSyncService / Dashboard refresh
        |
        v
VehicleSyncCoordinator
        |
        v
VehicleRepository
        |
        +--> read cached state
        +--> fetch demo telemetry
        +--> map DTOs to domain models
        +--> classify freshness and connectivity
        +--> write cache
        +--> publish StateFlow
        |
        v
DriveDashboardViewModel
        |
        v
DashboardReducer
        |
        v
Compose dashboard
```

More detail:

- [Use cases](docs/USE_CASES.md)
- [How it works](docs/HOW_IT_WORKS.md)
- [Architecture overview](docs/ARCHITECTURE_OVERVIEW.md)
- [Detailed architecture](docs/ARCHITECTURE.md)
- [Reviewer guide](docs/REVIEWER_GUIDE.md)

## Running It

You need Android Studio with JDK 17 and the Android SDK installed. API 35 is used by the project.

### Android Studio

1. Open Android Studio.
2. Choose **File > Open**.
3. Open this folder:

```text
C:\Users\Patron\OneDrive - UW-Madison\Desktop\github project\Rivian 2\DriveOS
```

4. Let Gradle sync finish.
5. Install any missing SDK packages if Android Studio asks.
6. Select the `app` run configuration.
7. Start an emulator or connect a device.
8. Press **Run**.

The app should launch into the DriveOS dashboard and start showing vehicle dashboard data.

### PowerShell

From the project root:

```powershell
.\gradlew.bat test
.\gradlew.bat :app:assembleDebug
```

To install on a connected emulator or device:

```powershell
.\gradlew.bat :app:installDebug
```

The debug APK is generated here:

```text
app\build\outputs\apk\debug\
```

## Setup Troubleshooting

If you see `JAVA_HOME is not set`, install Android Studio or JDK 17 and make sure Java is available:

```powershell
java -version
```

If the Android SDK is missing, open Android Studio and install the SDK from the SDK Manager. Android Studio usually creates `local.properties` automatically. It should point to your SDK, for example:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

If Gradle sync gets stuck, try:

```powershell
.\gradlew.bat --stop
.\gradlew.bat clean
```

Then reopen Android Studio and sync again.

## Demo Data Source

The project includes a local telemetry source for repeatable development and review. It drives:

- Vehicle telemetry
- Cloud connection status
- Navigation route summary
- Media metadata

The app architecture around that source includes:

- Android app structure
- Compose dashboard
- Foreground service lifecycle
- Repository and cache flow
- State updates through Kotlin Flow
- Media command handling
- Fresh/stale/offline state logic
- Java/Kotlin interop
- Unit-testable architecture

## Useful Files To Read

- `app/src/main/java/com/rivian/driveos/MainActivity.kt`
- `app/src/main/java/com/rivian/driveos/service/VehicleSyncService.kt`
- `core/model/src/main/java/com/rivian/driveos/model/VehicleModels.kt`
- `core/network/src/main/java/com/rivian/driveos/network/DemoVehicleTelemetryApi.kt`
- `core/data/src/main/java/com/rivian/driveos/data/VehicleRepository.kt`
- `core/data/src/main/java/com/rivian/driveos/data/ConnectivityAlertClassifier.java`
- `feature/dashboard/src/main/java/com/rivian/driveos/dashboard/DriveDashboardScreen.kt`
- `feature/dashboard/src/main/java/com/rivian/driveos/dashboard/DashboardReducer.kt`

## Tests

Run:

```powershell
.\gradlew.bat test
```

The current tests cover alert classification, repository refresh behavior, media command updates, freshness policy, and dashboard state formatting.

## Note

This is a personal portfolio project using a local telemetry simulator and public Android tooling.
