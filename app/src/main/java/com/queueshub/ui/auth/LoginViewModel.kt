package com.queueshub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrHandle
import com.queueshub.domain.model.NetworkException
import com.queueshub.domain.model.User
import com.queueshub.domain.usecases.GetUserLogged
import com.queueshub.domain.usecases.LoginUser
import com.queueshub.ui.models.Event
import com.queueshub.ui.viewStates.LoginViewState
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
class LoginViewModel @Inject constructor(userLogged: GetUserLogged, val loginUser: LoginUser) :
    ViewModel() {

    private val _state = MutableStateFlow(LoginViewState())
    val state: StateFlow<LoginViewState> = _state.asStateFlow()

    private val _userLogged: MutableStateFlow<Pair<Boolean, Long?>> =
        MutableStateFlow(Pair(false, null))
    val userLogged: StateFlow<Pair<Boolean, Long?>> = _userLogged.asStateFlow()

    init {
        viewModelScope.launch {
            _userLogged.update {
                userLogged()
            }
        }
    }

    fun startLogin(phone: String, pin: String) {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _state.update {
                    try{
                    it.copy(loading = false, user = loginUser(phone, pin))
                    }catch (e: IOException){
                        it.copy(loading = false,failure= Event(e))
                    }
                }
            }
        }
    }
}