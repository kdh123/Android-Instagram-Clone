package com.dhkim.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.common.handle
import com.dhkim.common.restartableStateIn
import com.dhkim.domain.login.useCase.LoginUseCase
import com.dhkim.domain.login.useCase.LogoutUseCase
import com.dhkim.domain.user.useCase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    val user = getUserUseCase()
        .restartableStateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _sideEffect = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.Login -> login()
            is LoginAction.Logout -> logout()
        }
    }

    private fun login() {
        viewModelScope.handle(
            block = {
                loginUseCase().first()
                user.restart()
                _sideEffect.send(LoginSideEffect.ShowToastMessage("Login Success"))
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(LoginSideEffect.ShowToastMessage(it.message ?: ""))
                }
            }
        )
    }

    private fun logout() {
        viewModelScope.handle(
            block = {
                logoutUseCase().first()
                user.restart()
                _sideEffect.send(LoginSideEffect.ShowToastMessage("Login Fail"))
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(LoginSideEffect.ShowToastMessage(it.message ?: ""))
                }
            }
        )
    }
}