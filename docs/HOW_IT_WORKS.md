# How It Works

This document walks through the app from launch to dashboard rendering.

## App Startup

`MainActivity` is the entry point. It gets the app dependency graph from `DriveOsApplication`, starts `VehicleSyncService`, and renders `DriveDashboardScreen`.

The dashboard ViewModel is created through a factory so it can receive:

- `VehicleRepository`
- `VehicleSyncCoordinator`

## Dependency Setup

`DriveOsDependencies` wires the prototype manually:

```text
DemoVehicleTelemetryApi
        |
        v
InMemoryVehicleSnapshotCache
        |
        v
VehicleRepository
        |
        v
VehicleSyncCoordinator
```

Manual dependency wiring keeps the project easy to inspect. A production app could move this to Hilt.

## Telemetry Refresh

`VehicleSyncCoordinator` owns refresh timing. It can:

- load cached state;
- refresh once from the ViewModel;
- run periodic refresh from the foreground service.

The coordinator delegates the actual data work to `VehicleRepository`.

## Repository Flow

`VehicleRepository.refresh()` does the main orchestration:

1. Mark refresh start in repository health.
2. Try to load cached vehicle state.
3. Fetch telemetry from `VehicleTelemetryApi`.
4. Convert DTOs into domain models.
5. Apply freshness classification.
6. Apply connectivity alert classification.
7. Write the snapshot to cache.
8. Publish the latest snapshot through `StateFlow`.
9. Record diagnostic events.

The repository is the main boundary between raw telemetry and dashboard-ready state.

## Telemetry Mapping

`VehicleTelemetryDto.toDomain()` converts API-style telemetry into `VehicleSnapshot`.

This mapping step also normalizes values before they reach the rest of the app. For example, battery percentage and volume are clamped into valid ranges.

## Freshness Policy

`SnapshotFreshnessPolicy` marks snapshots as:

- `FRESH`
- `AGING`
- `STALE`

That state is stored inside `DataQuality`, along with the source and age in seconds.

## Connectivity Alerts

`ConnectivityAlertClassifier.java` creates user-visible alerts from connectivity state.

It is intentionally written in Java because Android projects often contain a mix of Kotlin app code and Java service/framework code.

## Dashboard State

`DriveDashboardViewModel` combines:

- the latest `VehicleSnapshot`;
- recent telemetry events.

It passes both into `DashboardReducer`.

`DashboardReducer` formats domain state into `DashboardUiState`, which contains the strings and rows used by the Compose screen.

## Compose UI

`DriveDashboardScreen` renders:

- header and connectivity banner;
- vehicle status panel;
- media panel;
- navigation panel;
- connectivity alerts;
- service diagnostics.

The UI does not directly fetch telemetry or mutate cache state. It calls ViewModel actions, and the ViewModel delegates to the repository or sync coordinator.

## Media Commands

Media controls use `VehicleCommand`:

- `TogglePlayback`
- `NextTrack`
- `SetVolume`

The repository applies these commands to the current media state and records a diagnostic event.
