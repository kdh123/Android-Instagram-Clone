package com.dhkim.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.common.handle
import com.dhkim.domain.login.useCase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _sideEffect = Channel<LoginSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.Login -> login()
        }
    }

    private fun login() {
        viewModelScope.handle(
            block = {
                loginUseCase().first()
                _sideEffect.send(LoginSideEffect.ShowToastMessage("로그인 성공"))
            },
            onError = {
                viewModelScope.launch {
                    _sideEffect.send(LoginSideEffect.ShowToastMessage(it.message ?: ""))
                }
            }
        )
    }
}