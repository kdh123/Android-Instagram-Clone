package com.dhkim.data.login.repository

import com.dhkim.data.login.dataSource.LoginRemoteSource
import com.dhkim.domain.login.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val remoteSource: LoginRemoteSource,
): LoginRepository {

    override suspend fun login() {
        remoteSource.login()
    }

    override fun logout(): Flow<Unit> {
        return remoteSource.logout()
    }
}