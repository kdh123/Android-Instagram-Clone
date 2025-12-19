package com.dhkim.login

sealed interface LoginSideEffect {

    data object NavigateToHome : LoginSideEffect
    data class ShowToastMessage(val message: String) : LoginSideEffect
}
