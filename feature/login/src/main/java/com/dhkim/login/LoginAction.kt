package com.dhkim.login

sealed interface LoginAction {

    data object Login : LoginAction
    data object Logout : LoginAction
}