# DriveOS Companion Architecture

## Product Goal

DriveOS Companion is a prototype Android infotainment surface for a software-defined vehicle. It is designed to feel credible to an automotive software reviewer because the core behavior is not just a static UI: the dashboard is driven by domain models, refresh orchestration, cached state, service lifecycle boundaries, and degraded connectivity handling.

## User-Facing Capabilities

- Media control panel with source, track, artist, play state, and volume.
- Navigation card with destination, ETA, remaining distance, and route condition.
- Vehicle status panels for range, battery, cabin temperature, tire pressure, drive mode, and odometer.
- Connectivity alerts for offline, weak-signal, stale-telemetry, and cloud-recovered states.
- Cache-first dashboard restore so the head unit can show the last known state before network refresh finishes.
- Background vehicle sync through an Android foreground service.
- Service diagnostics that surface cache hits, refreshes, failures, command actions, and classification events.
- Local media command handling for playback and track changes.

## Design Principles

- **Cache-first, network-refresh second:** infotainment should display known state quickly, then update when telemetry arrives.
- **Explicit degraded states:** offline mode is a first-class domain state, not a UI afterthought.
- **Module boundaries mirror ownership:** model, network, data, service, and feature UI are independently testable.
- **Kotlin primary, Java compatible:** most code is Kotlin, with one Java classifier to demonstrate Android mixed-language integration.
- **Immutable snapshots:** dashboard rendering consumes immutable `VehicleSnapshot` values to avoid UI race conditions.
- **Diagnostics as product behavior:** the UI exposes freshness and sync events because infotainment reliability depends on explaining data quality.
- **Command intent modeling:** media controls emit domain commands so command delivery can later move behind Android service bindings or vehicle APIs.

## Module Responsibilities

### `:core:model`

Owns shared domain contracts. These classes are intentionally Android-free so they can be tested quickly and reused by service, data, and UI modules.

Key models:

- `VehicleSnapshot`
- `VehicleStatus`
- `MediaState`
- `NavigationState`
- `ConnectivityState`
- `ConnectivityAlert`
- `DataQuality`
- `VehicleCommand`
- `TelemetryEvent`

### `:core:network`

Owns the REST API boundary and DTO mapping. `VehicleTelemetryApi` expresses the remote contract; `VehicleTelemetryDto` and nested DTOs isolate wire-format changes from the rest of the app.

Production implementation would use Retrofit against a vehicle-cloud or simulator endpoint. The prototype includes a deterministic `DemoVehicleTelemetryApi` so the UI and tests can run without a real backend.

### `:core:data`

Owns data orchestration. `VehicleRepository` exposes a `StateFlow<VehicleSnapshot?>`, serves cached snapshots first, refreshes from REST, applies Java alert classification, handles local command intents, and records repository health events.

Cache is abstracted behind `VehicleSnapshotCache`. The included `InMemoryVehicleSnapshotCache` is deliberately replaceable with Room or Proto DataStore.

`SnapshotFreshnessPolicy` owns stale-data thresholds. `TelemetryEventLog` owns an in-memory diagnostic timeline for engineering visibility.

### `:core:service`

Owns background sync coordination. `VehicleSyncCoordinator` performs periodic refresh with backoff-friendly boundaries and emits sync health.

### `:feature:dashboard`

Owns the Compose presentation layer. `DriveDashboardViewModel` observes repository state and reduces it into `DashboardUiState`. `DriveDashboardScreen` renders panels without knowing about network or cache details.

### `:app`

Owns Android entry points and manual dependency assembly for the prototype. A production app would replace `DriveOsDependencies` with Hilt or another DI framework.

## Data Flow

```text
Android service / ViewModel
        |
        v
VehicleSyncCoordinator
        |
        v
VehicleRepository.refresh()
        |
        +--> VehicleSnapshotCache.read()
        |
        +--> VehicleTelemetryApi.getSnapshot()
                 |
                 v
              DTO mapper
                 |
                 v
        SnapshotFreshnessPolicy
                 |
                 v
        ConnectivityAlertClassifier.java
                 |
                 v
          TelemetryEventLog
                 |
                 v
          VehicleSnapshotCache.write()
                 |
                 v
          StateFlow<VehicleSnapshot?>
                 |
                 v
          DashboardReducer
                 |
                 v
          Compose Dashboard
```

## Failure Model

| Failure | App Behavior |
| --- | --- |
| Network unavailable | Render cached snapshot with `OFFLINE` connectivity and an offline alert. |
| REST timeout | Preserve last known snapshot, append stale-telemetry alert if data age exceeds threshold. |
| Empty cache on first launch | Render loading dashboard with degraded connectivity message. |
| Service restart | Repository replays cached state, then sync coordinator refreshes again. |
| Weak signal | Alert classifier emits weak-signal warning without blocking dashboard rendering. |
| Local command while online | Repository applies command optimistically and records a command event. |
| Local command without state | Repository returns `null`; UI remains unchanged until telemetry or cache appears. |

## Testing Strategy

- Repository tests validate cache-first emissions and network refresh replacement.
- Reducer tests validate domain-to-UI transformation.
- Java classifier tests validate signal/staleness alert classification.
- Command tests validate optimistic media state updates and event logging.
- Android instrumentation tests can be added around `DriveDashboardScreen` once Android SDK tooling is available.

## Reviewer Walkthrough

1. Start at `VehicleSnapshot` to see the domain contract.
2. Follow `VehicleTelemetryDto.toDomain()` to see wire-format normalization.
3. Read `VehicleRepository.refresh()` to see cache replay, network refresh, freshness classification, alert classification, and event logging.
4. Read `DriveDashboardViewModel` to see how repository state and event history become UI state.
5. Read `DriveDashboardScreen` to see the head-unit surface and diagnostics panels.

## Extension Points

- Replace `DemoVehicleTelemetryApi` with a real Retrofit endpoint.
- Replace `InMemoryVehicleSnapshotCache` with Room for persistent local state.
- Add Android Automotive OS vehicle property integration.
- Move manual dependency assembly to Hilt.
- Add Espresso tests for media controls and offline banners.
- Add bound service APIs for command delivery from other Android components.
- Add a gRPC bridge for zonal telemetry once the second project is introduced.
