package com.rivian.driveos.dashboard

/**
 * Display-ready state for the infotainment dashboard.
 *
 * The UI receives formatted strings and rows instead of raw telemetry so Compose
 * stays focused on layout while reducers own presentation rules.
 */
data class DashboardUiState(
    val isLoading: Boolean,
    val vehicleTitle: String,
    val rangeLabel: String,
    val batteryLabel: String,
    val cabinLabel: String,
    val tirePressureLabel: String,
    val driveModeLabel: String,
    val mediaTitle: String,
    val mediaSubtitle: String,
    val mediaControlLabel: String,
    val navigationTitle: String,
    val navigationSubtitle: String,
    val connectivityBanner: String,
    val dataQualityLabel: String,
    val lastUpdatedLabel: String,
    val alerts: List<AlertRow>,
    val eventRows: List<EventRow>
) {
    companion object {
        val Loading = DashboardUiState(
            isLoading = true,
            vehicleTitle = "DriveOS Companion",
            rangeLabel = "-- mi",
            batteryLabel = "--%",
            cabinLabel = "-- F",
            tirePressureLabel = "-- / -- / -- / -- psi",
            driveModeLabel = "Starting",
            mediaTitle = "Media unavailable",
            mediaSubtitle = "Waiting for vehicle state",
            mediaControlLabel = "Paused",
            navigationTitle = "Navigation loading",
            navigationSubtitle = "Cached route will appear here",
            connectivityBanner = "Connecting to vehicle services",
            dataQualityLabel = "Source: starting",
            lastUpdatedLabel = "Last update: pending",
            alerts = emptyList(),
            eventRows = emptyList()
        )
    }
}

data class AlertRow(
    val code: String,
    val title: String,
    val message: String,
    val severity: String
)

data class EventRow(
    val type: String,
    val message: String
)
