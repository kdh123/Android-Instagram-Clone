package com.dhkim.domain.login.useCase

import com.dhkim.domain.login.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {

    suspend operator fun invoke() {
        loginRepository.login()
    }
}