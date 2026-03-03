package com.rivian.driveos.network

import retrofit2.http.GET
import retrofit2.http.Path

interface VehicleTelemetryApi {
    @GET("vehicles/{vehicleId}/snapshot")
    suspend fun getSnapshot(@Path("vehicleId") vehicleId: String): VehicleTelemetryDto
}
