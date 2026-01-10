package com.dhkim.domain.login.repository

import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    suspend fun login()
    fun logout(): Flow<Unit>
}