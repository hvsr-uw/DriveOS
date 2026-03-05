package com.rivian.driveos.network

import kotlinx.coroutines.delay
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class DemoVehicleTelemetryApi : VehicleTelemetryApi {
    private val tick = AtomicInteger(0)

    override suspend fun getSnapshot(vehicleId: String): VehicleTelemetryDto {
        delay(250)
        val sequence = tick.incrementAndGet()
        val now = Instant.now()
        val weakSignal = sequence % 5 == 0
        val traffic = sequence % 3 == 0

        return VehicleTelemetryDto(
            vehicleId = vehicleId,
            batteryPercent = 78 - (sequence % 6),
            estimatedRangeMiles = 242 - sequence,
            cabinTemperatureFahrenheit = 70,
            tirePressure = TirePressureDto(
                frontLeft = 34,
                frontRight = 34,
                rearLeft = 35,
                rearRight = 35
            ),
            driveMode = if (sequence % 4 == 0) "CONSERVE" else "ALL_PURPOSE",
            media = MediaDto(
                source = "STREAMING",
                trackTitle = if (sequence % 2 == 0) "Northern Route" else "Electric Horizon",
                artist = "DriveOS Mix",
                isPlaying = true,
                volumePercent = 42
            ),
            navigation = NavigationDto(
                destination = "Rivian Service Center",
                etaMinutes = if (traffic) 31 else 24,
                remainingMiles = if (traffic) 12.8 else 10.4,
                routeCondition = if (traffic) "TRAFFIC" else "CLEAR"
            ),
            connectivity = ConnectivityDto(
                cloudStatus = if (weakSignal) "DEGRADED" else "CONNECTED",
                signalStrength = if (weakSignal) "WEAK" else "GOOD",
                lastUpdatedEpochMillis = now.toEpochMilli()
            ),
            capturedAtEpochMillis = now.toEpochMilli()
        )
    }
}
