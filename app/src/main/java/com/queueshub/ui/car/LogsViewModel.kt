package com.queueshub.ui.car

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.data.api.model.ApiLog
import com.queueshub.domain.usecases.LogDataUseCase
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.CreateOrderViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(val logDataUseCase: LogDataUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(CreateOrderViewState())
    val state: StateFlow<CreateOrderViewState> = _state.asStateFlow()

    fun addLogs(data: ApiLog) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                    try{
                        logDataUseCase(data = data)
                    }catch (e: IOException){
                    }
            }
        }
    }




}