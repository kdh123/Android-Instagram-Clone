package com.dhkim.domain.login.useCase

import com.dhkim.domain.login.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    operator fun invoke(): Flow<Unit> {
        return loginRepository.logout()
    }
}