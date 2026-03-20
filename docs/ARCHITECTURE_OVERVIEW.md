# Architecture Overview

DriveOS Companion uses a layered Android architecture. The main idea is to keep UI rendering, domain state, data orchestration, service lifecycle, and telemetry mapping separate.

## Layer Map

```text
:app
  Android entry points and dependency wiring

:feature:dashboard
  Compose UI, ViewModel, reducer, display state

:core:service
  Sync coordinator used by app/service flows

:core:data
  Repository, cache, freshness policy, diagnostics, commands

:core:network
  API contract, DTOs, demo telemetry source

:core:model
  Shared domain models
```

## Why The App Is Split This Way

The project is split around responsibility:

- UI code should not know how telemetry is fetched.
- Network DTOs should not leak into the dashboard.
- Cache policy should live below the ViewModel.
- Service lifecycle should not own repository rules.
- Domain models should be Android-free where possible.

This makes the code easier to test and easier to extend.

## Main Data Model

`VehicleSnapshot` is the central domain object. It contains:

- vehicle status;
- media state;
- navigation state;
- connectivity state;
- data quality;
- alerts;
- capture timestamp.

The dashboard renders from this snapshot instead of many unrelated fields.

## State Management

The repository exposes state through Kotlin `StateFlow`.

```text
VehicleRepository.snapshot: StateFlow<VehicleSnapshot?>
VehicleRepository.health: StateFlow<RepositoryHealth>
VehicleRepository.events: StateFlow<List<TelemetryEvent>>
```

The ViewModel observes these flows and converts them into UI state.

## Cache Strategy

The current cache implementation is `InMemoryVehicleSnapshotCache`.

The cache is behind `VehicleSnapshotCache`, so it can be replaced later with:

- Room;
- Proto DataStore;
- encrypted local storage;
- a vehicle-specific state store.

## Network Boundary

`VehicleTelemetryApi` represents the telemetry API contract. `DemoVehicleTelemetryApi` implements that contract with local demo values.

This lets the rest of the app behave as if it is consuming REST telemetry while keeping the project runnable from the repo.

## Service Boundary

`VehicleSyncService` is the Android foreground service. It starts a sync loop through `VehicleSyncCoordinator`.

`VehicleSyncCoordinator` is kept in `:core:service` so the timing behavior is separate from Android framework code.

## UI Boundary

The Compose UI receives `DashboardUiState`, not raw domain objects.

`DashboardReducer` owns formatting decisions such as:

- range labels;
- tire pressure labels;
- freshness labels;
- alert rows;
- event rows.

That makes the UI code easier to read and the formatting behavior easier to test.

## Extension Points

The architecture is ready for:

- real Retrofit networking;
- Hilt dependency injection;
- persistent caching;
- Android Automotive OS vehicle property integration;
- gRPC zonal telemetry;
- Compose UI tests;
- instrumentation tests;
- structured logging.
