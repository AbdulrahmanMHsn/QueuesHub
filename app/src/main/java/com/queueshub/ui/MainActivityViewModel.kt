package com.queueshub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queueshub.domain.model.User
import com.queueshub.domain.usecases.GetUserLogged
import com.queueshub.domain.usecases.LoginUser
import com.queueshub.domain.usecases.LogoutUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userLogged: GetUserLogged,
    val logoutUser: LogoutUserUseCase
) : ViewModel() {
    fun logout() {
        viewModelScope.launch {
            logoutUser()
        }
    }

    val uiState: StateFlow<MainActivityUiState> = MutableStateFlow(
        userLogged().let {
            val user = it.second
            return@let MainActivityUiState.Success(it.second, it.first)
        })
}

sealed interface MainActivityUiState {
    object Loading : MainActivityUiState
    data class Success(val userId: Long?, val logged: Boolean) : MainActivityUiState
}

