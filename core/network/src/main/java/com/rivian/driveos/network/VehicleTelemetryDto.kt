package com.rivian.driveos.network

import com.rivian.driveos.model.CloudStatus
import com.rivian.driveos.model.ConnectivityState
import com.rivian.driveos.model.DataQuality
import com.rivian.driveos.model.DriveMode
import com.rivian.driveos.model.Freshness
import com.rivian.driveos.model.MediaSource
import com.rivian.driveos.model.MediaState
import com.rivian.driveos.model.NavigationState
import com.rivian.driveos.model.RouteCondition
import com.rivian.driveos.model.SignalStrength
import com.rivian.driveos.model.SnapshotSource
import com.rivian.driveos.model.TirePressure
import com.rivian.driveos.model.VehicleSnapshot
import com.rivian.driveos.model.VehicleStatus
import java.time.Duration
import java.time.Instant

data class VehicleTelemetryDto(
    val vehicleId: String,
    val batteryPercent: Int,
    val estimatedRangeMiles: Int,
    val cabinTemperatureFahrenheit: Int,
    val tirePressure: TirePressureDto,
    val driveMode: String,
    val media: MediaDto,
    val navigation: NavigationDto,
    val connectivity: ConnectivityDto,
    val capturedAtEpochMillis: Long
)

data class TirePressureDto(
    val frontLeft: Int,
    val frontRight: Int,
    val rearLeft: Int,
    val rearRight: Int
)

data class MediaDto(
    val source: String,
    val trackTitle: String,
    val artist: String,
    val isPlaying: Boolean,
    val volumePercent: Int
)

data class NavigationDto(
    val destination: String,
    val etaMinutes: Int,
    val remainingMiles: Double,
    val routeCondition: String
)

data class ConnectivityDto(
    val cloudStatus: String,
    val signalStrength: String,
    val lastUpdatedEpochMillis: Long
)

/**
 * Maps the REST wire contract into a domain snapshot while normalizing
 * out-of-range simulator values before they reach the UI.
 */
fun VehicleTelemetryDto.toDomain(now: Instant = Instant.now()): VehicleSnapshot {
    val capturedAt = Instant.ofEpochMilli(capturedAtEpochMillis)
    val ageSeconds = Duration.between(capturedAt, now).seconds.coerceAtLeast(0)

    return VehicleSnapshot(
        vehicleId = vehicleId,
        status = VehicleStatus(
            batteryPercent = batteryPercent.coerceIn(0, 100),
            estimatedRangeMiles = estimatedRangeMiles.coerceAtLeast(0),
            cabinTemperatureFahrenheit = cabinTemperatureFahrenheit,
            tirePressurePsi = TirePressure(
                frontLeft = tirePressure.frontLeft,
                frontRight = tirePressure.frontRight,
                rearLeft = tirePressure.rearLeft,
                rearRight = tirePressure.rearRight
            ),
            driveMode = driveMode.enumValueOrDefault(DriveMode.ALL_PURPOSE),
            odometerMiles = 18_420
        ),
        media = MediaState(
            source = media.source.enumValueOrDefault(MediaSource.STREAMING),
            trackTitle = media.trackTitle,
            artist = media.artist,
            isPlaying = media.isPlaying,
            volumePercent = media.volumePercent.coerceIn(0, 100)
        ),
        navigation = NavigationState(
            destination = navigation.destination,
            etaMinutes = navigation.etaMinutes.coerceAtLeast(0),
            remainingMiles = navigation.remainingMiles.coerceAtLeast(0.0),
            routeCondition = navigation.routeCondition.enumValueOrDefault(RouteCondition.CLEAR)
        ),
        connectivity = ConnectivityState(
            cloudStatus = connectivity.cloudStatus.enumValueOrDefault(CloudStatus.CONNECTED),
            signalStrength = connectivity.signalStrength.enumValueOrDefault(SignalStrength.GOOD),
            lastUpdatedAt = Instant.ofEpochMilli(connectivity.lastUpdatedEpochMillis)
        ),
        dataQuality = DataQuality(
            source = SnapshotSource.NETWORK,
            freshness = ageSeconds.toFreshness(),
            ageSeconds = ageSeconds
        ),
        alerts = emptyList(),
        capturedAt = capturedAt
    )
}

private fun Long.toFreshness(): Freshness = when {
    this < 30 -> Freshness.FRESH
    this < 180 -> Freshness.AGING
    else -> Freshness.STALE
}

private inline fun <reified T : Enum<T>> String.enumValueOrDefault(default: T): T =
    enumValues<T>().firstOrNull { it.name == uppercase() } ?: default
