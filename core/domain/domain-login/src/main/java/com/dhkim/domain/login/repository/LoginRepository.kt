package com.dhkim.domain.login.repository

import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    fun login(): Flow<Unit>
    fun logout(): Flow<Unit>
}