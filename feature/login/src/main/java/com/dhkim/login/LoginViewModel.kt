package com.dhkim.login

import androidx.lifecycle.ViewModel
import com.dhkim.domain.login.useCase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

}