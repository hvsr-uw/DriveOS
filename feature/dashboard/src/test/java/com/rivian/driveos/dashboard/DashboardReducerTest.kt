package com.rivian.driveos.dashboard

import com.rivian.driveos.model.AlertSeverity
import com.rivian.driveos.model.CloudStatus
import com.rivian.driveos.model.ConnectivityAlert
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
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DashboardReducerTest {
    @Test
    fun reduceFormatsCoreDashboardFields() {
        val state = DashboardReducer.reduce(snapshot())

        assertEquals("R1T-DEMO", state.vehicleTitle)
        assertEquals("238 mi", state.rangeLabel)
        assertEquals("All Purpose", state.driveModeLabel)
        assertEquals("Service Center", state.navigationTitle)
        assertEquals("Vehicle connected", state.connectivityBanner)
        assertEquals("Source: Network | Freshness: Fresh | Age: 0s", state.dataQualityLabel)
    }

    private fun snapshot() = VehicleSnapshot(
        vehicleId = "R1T-DEMO",
        status = VehicleStatus(
            batteryPercent = 77,
            estimatedRangeMiles = 238,
            cabinTemperatureFahrenheit = 70,
            tirePressurePsi = TirePressure(34, 34, 35, 35),
            driveMode = DriveMode.ALL_PURPOSE,
            odometerMiles = 18_420
        ),
        media = MediaState(MediaSource.STREAMING, "Electric Horizon", "DriveOS Mix", true, 42),
        navigation = NavigationState("Service Center", 24, 10.4, RouteCondition.CLEAR),
        connectivity = ConnectivityState(CloudStatus.CONNECTED, SignalStrength.GOOD, Instant.EPOCH),
        dataQuality = DataQuality(SnapshotSource.NETWORK, Freshness.FRESH, 0),
        alerts = listOf(
            ConnectivityAlert(AlertSeverity.INFO, "CONNECTIVITY_HEALTHY", "Vehicle connected", "OK")
        ),
        capturedAt = Instant.EPOCH
    )
}
