package com.dhkim.login

sealed interface LoginSideEffect {

    data object NavigateToMain : LoginSideEffect
    data class ShowToastMessage(val message: String) : LoginSideEffect
}
