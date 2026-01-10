package com.dhkim.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.common.handle
import com.dhkim.domain.feed.useCase.SyncHiddenFeedsUseCase
import com.dhkim.domain.feed.useCase.SyncLikeFeedsUseCase
import com.dhkim.domain.login.exception.LoginFailException
import com.dhkim.domain.login.useCase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val syncLikeFeedsUseCase: SyncLikeFeedsUseCase,
    private val syncHiddenFeedsUseCase: SyncHiddenFeedsUseCase
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
                loginUseCase()
                syncLikeFeedsUseCase()
                syncHiddenFeedsUseCase()
                _sideEffect.send(LoginSideEffect.NavigateToHome)
            },
            onError = { e ->
                viewModelScope.launch {
                    if (e is LoginFailException) {
                        _sideEffect.send(LoginSideEffect.ShowToastMessage(e.message ?: ""))
                    } else {
                        _sideEffect.send(LoginSideEffect.NavigateToHome)
                    }
                }
            }
        )
    }
}