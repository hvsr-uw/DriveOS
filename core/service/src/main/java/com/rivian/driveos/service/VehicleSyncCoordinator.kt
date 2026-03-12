package com.rivian.driveos.service

import com.rivian.driveos.data.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * Periodic sync loop shared by the Android foreground service and ViewModel.
 *
 * Keeping the loop outside `Service` makes lifecycle behavior testable and
 * prevents Android framework code from owning repository policy.
 */
class VehicleSyncCoordinator(
    private val repository: VehicleRepository
) {
    suspend fun start(intervalMillis: Long = DEFAULT_INTERVAL_MILLIS) {
        repository.loadCached()
        while (coroutineContext.isActive) {
            repository.refresh()
            delay(intervalMillis)
        }
    }

    suspend fun refreshOnce() {
        repository.refresh()
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS = 30_000L
    }
}
