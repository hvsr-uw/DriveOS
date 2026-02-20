package com.rivian.driveos.dashboard

import com.rivian.driveos.model.AlertSeverity
import com.rivian.driveos.model.MediaSource
import com.rivian.driveos.model.TelemetryEvent
import com.rivian.driveos.model.VehicleSnapshot
import java.time.Duration
import java.time.Instant

/**
 * Converts domain snapshots and diagnostic events into stable UI state.
 */
object DashboardReducer {
    fun reduce(
        snapshot: VehicleSnapshot?,
        events: List<TelemetryEvent> = emptyList(),
        now: Instant = Instant.now()
    ): DashboardUiState {
        if (snapshot == null) return DashboardUiState.Loading

        val tirePressure = snapshot.status.tirePressurePsi
        val mediaAction = if (snapshot.media.isPlaying) "Pause" else "Play"
        val strongestAlert = snapshot.alerts.maxByOrNull { it.severity.ordinal }
        val lastUpdatedSeconds = Duration.between(snapshot.connectivity.lastUpdatedAt, now).seconds.coerceAtLeast(0)

        return DashboardUiState(
            isLoading = false,
            vehicleTitle = snapshot.vehicleId,
            rangeLabel = "${snapshot.status.estimatedRangeMiles} mi",
            batteryLabel = "${snapshot.status.batteryPercent}%",
            cabinLabel = "${snapshot.status.cabinTemperatureFahrenheit} F",
            tirePressureLabel = "${tirePressure.frontLeft} / ${tirePressure.frontRight} / ${tirePressure.rearLeft} / ${tirePressure.rearRight} psi",
            driveModeLabel = snapshot.status.driveMode.name.toTitleCase(),
            mediaTitle = snapshot.media.trackTitle,
            mediaSubtitle = "${snapshot.media.artist} | ${snapshot.media.source.displayName()} | ${snapshot.media.volumePercent}%",
            mediaControlLabel = mediaAction,
            navigationTitle = snapshot.navigation.destination,
            navigationSubtitle = "${snapshot.navigation.etaMinutes} min | ${"%.1f".format(snapshot.navigation.remainingMiles)} mi | ${snapshot.navigation.routeCondition.name.toTitleCase()}",
            connectivityBanner = strongestAlert?.title ?: "Vehicle connected",
            dataQualityLabel = "Source: ${snapshot.dataQuality.source.name.toTitleCase()} | Freshness: ${snapshot.dataQuality.freshness.name.toTitleCase()} | Age: ${snapshot.dataQuality.ageSeconds}s",
            lastUpdatedLabel = "Last cloud update: ${lastUpdatedSeconds}s ago",
            alerts = snapshot.alerts.map {
                AlertRow(
                    code = it.code,
                    title = it.title,
                    message = it.message,
                    severity = it.severity.displayName()
                )
            },
            eventRows = events.takeLast(5).reversed().map {
                EventRow(type = it.type.name.toTitleCase(), message = it.message)
            }
        )
    }

    private fun String.toTitleCase(): String =
        lowercase().split("_").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }

    private fun MediaSource.displayName(): String = name.toTitleCase()

    private fun AlertSeverity.displayName(): String = name.toTitleCase()
}
