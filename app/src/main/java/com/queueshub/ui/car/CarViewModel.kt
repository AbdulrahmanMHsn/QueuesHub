package com.queueshub.ui.car

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.domain.usecases.GetCarModels
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.ModelsViewState
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
class CarViewModel @Inject constructor(val getCaModel: GetCarModels) :
    ViewModel() {

    private val _state = MutableStateFlow(ModelsViewState())
    val state: StateFlow<ModelsViewState> = _state.asStateFlow()

    fun fetchModels() {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _state.update {
                    try{
                        it.copy(loading = false, models = getCaModel())
                    }catch (e: IOException){
                        it.copy(loading = false,failure= Event(e))
                    }
                }
            }
        }
    }

}