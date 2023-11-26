package com.queueshub.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.domain.usecases.GetMaintenances
import com.queueshub.domain.usecases.GetSensors
import com.queueshub.domain.usecases.ValidateImei
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.MaintenanceViewState
import com.queueshub.ui.viewStates.SensorViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(val getSensors: GetSensors,val getMaintenances: GetMaintenances,val validateImeiUseCase: ValidateImei) :
    ViewModel() {

    private val _state = MutableStateFlow(SensorViewState())
    val state: StateFlow<SensorViewState> = _state.asStateFlow()

    private val _maintenanceState = MutableStateFlow(MaintenanceViewState())
    val maintenanceState: StateFlow<MaintenanceViewState> = _maintenanceState.asStateFlow()

    fun fetchSensors() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _state.update {
                    try{
                        it.copy(loading = false, sensors = getSensors())
                    }catch (e: IOException){
                        it.copy(loading = false,failure= Event(e))
                    }
                }
            }
        }
    }
    fun fetchMaintenance() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _maintenanceState.update {
                    try{
                        it.copy(loading = false, maintenances = getMaintenances())
                    }catch (e: IOException){
                        it.copy(loading = false,failure= Event(e))
                    }
                }
            }
        }
    }

}