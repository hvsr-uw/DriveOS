# Reviewer Guide

This guide explains how to inspect DriveOS Companion quickly from an engineering perspective.

## What To Look For

### Architecture

The project is split by ownership rather than by file type:

- Domain state lives in `:core:model`.
- REST boundary and DTO mapping live in `:core:network`.
- Cache, repository policy, command handling, and event logging live in `:core:data`.
- Periodic sync lives in `:core:service`.
- Compose presentation and reduction live in `:feature:dashboard`.
- Android entry points live in `:app`.

### Reliability Behavior

The dashboard does not assume network telemetry is always fresh. `DataQuality` marks each snapshot as network or cache sourced, and as fresh, aging, or stale. Connectivity alerts are calculated separately from freshness because a connected cloud link can still return old telemetry.

### Debuggability

`TelemetryEventLog` records a small timeline:

- cache hit or miss;
- refresh started;
- refresh completed;
- refresh failed;
- snapshot classified;
- local command applied.

The dashboard displays the latest diagnostic events so behavior is visible during manual review.

### Command Model

Media actions use `VehicleCommand` domain intents. The repository applies them optimistically today, but the same shape can be routed through Android service bindings, gRPC, or vehicle APIs later.

## High-Signal Files

- `core/model/src/main/java/com/rivian/driveos/model/VehicleModels.kt`
- `core/network/src/main/java/com/rivian/driveos/network/VehicleTelemetryDto.kt`
- `core/data/src/main/java/com/rivian/driveos/data/VehicleRepository.kt`
- `core/data/src/main/java/com/rivian/driveos/data/SnapshotFreshnessPolicy.kt`
- `core/data/src/main/java/com/rivian/driveos/data/TelemetryEventLog.kt`
- `feature/dashboard/src/main/java/com/rivian/driveos/dashboard/DashboardReducer.kt`
- `feature/dashboard/src/main/java/com/rivian/driveos/dashboard/DriveDashboardScreen.kt`

## Build Note

Run these on a machine with JDK 17 and Android SDK installed:

```powershell
./gradlew test
./gradlew :app:assembleDebug
```
