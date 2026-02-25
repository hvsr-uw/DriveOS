package com.rivian.driveos.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rivian.driveos.data.VehicleRepository
import com.rivian.driveos.model.VehicleCommand
import com.rivian.driveos.service.VehicleSyncCoordinator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Presentation boundary between Android lifecycle and vehicle data services.
 */
class DriveDashboardViewModel(
    private val repository: VehicleRepository,
    private val syncCoordinator: VehicleSyncCoordinator
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(repository.snapshot, repository.events) { snapshot, events ->
        DashboardReducer.reduce(snapshot, events)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        viewModelScope.launch {
            syncCoordinator.refreshOnce()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            syncCoordinator.refreshOnce()
        }
    }

    fun togglePlayback() {
        viewModelScope.launch {
            repository.applyCommand(VehicleCommand.TogglePlayback)
        }
    }

    fun nextTrack() {
        viewModelScope.launch {
            repository.applyCommand(VehicleCommand.NextTrack)
        }
    }

    companion object {
        fun factory(
            repository: VehicleRepository,
            syncCoordinator: VehicleSyncCoordinator
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(DriveDashboardViewModel::class.java)) {
                    "Unsupported ViewModel class: ${modelClass.name}"
                }
                return DriveDashboardViewModel(repository, syncCoordinator) as T
            }
        }
    }
}
