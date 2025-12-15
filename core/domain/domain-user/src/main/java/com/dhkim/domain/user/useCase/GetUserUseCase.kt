package com.dhkim.domain.user.useCase

import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    operator fun invoke(): Flow<User?> {
        return userRepository.getUser()
    }
}