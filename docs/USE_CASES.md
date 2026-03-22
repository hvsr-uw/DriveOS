# Use Cases

DriveOS Companion is built around a realistic infotainment dashboard flow for a software-defined vehicle. The app focuses on the kind of state a driver expects to see quickly and reliably while the head unit is running.

## 1. Driver Opens The Vehicle Dashboard

When the app starts, the dashboard loads vehicle state and displays the latest available information:

- Battery percentage
- Estimated range
- Cabin temperature
- Tire pressure
- Drive mode
- Media playback
- Navigation summary
- Connectivity health

The goal is fast, readable vehicle status without forcing the UI to wait for every background update.

## 2. Vehicle State Updates In The Background

The foreground sync service keeps telemetry moving while the app is active. Each refresh updates the repository, cache, alerts, and UI state.

This models a common infotainment requirement: the driver-facing screen should stay current while other parts of the system continue to update in the background.

## 3. Dashboard Uses Cached State

The app supports cache-first loading. If cached vehicle state exists, the dashboard can display it while the next refresh is happening.

This matters because vehicle screens should avoid feeling blank or broken just because the newest telemetry has not arrived yet.

## 4. Connectivity Becomes Degraded

The app classifies connectivity states such as healthy, degraded, offline, and stale. These states become visible through alerts and dashboard copy.

This gives the UI a clear way to explain data quality instead of quietly showing old or uncertain state.

## 5. Driver Uses Media Controls

The media panel supports playback actions:

- Play/pause
- Next track
- Volume state in the displayed media model

The commands go through the repository as domain actions, which keeps media control behavior separate from Compose UI code.

## 6. Reviewer Checks System Diagnostics

The diagnostics panel shows recent events such as:

- Cache hit
- Cache miss
- Network refresh started
- Network refresh completed
- Command applied
- Snapshot classified

This makes the data flow visible during review and helps show that the screen is driven by application state, not static sample values.

## 7. Developer Extends The Project

The current structure leaves clear paths for production-style upgrades:

- Replace the local telemetry source with a real Retrofit endpoint.
- Replace the in-memory cache with Room or Proto DataStore.
- Add Android Automotive vehicle property adapters.
- Add bound service APIs for other app components.
- Add Compose UI tests and instrumentation tests.
