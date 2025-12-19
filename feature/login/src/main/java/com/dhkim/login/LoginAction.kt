package com.dhkim.login

sealed interface LoginAction {

    data object Login : LoginAction
}