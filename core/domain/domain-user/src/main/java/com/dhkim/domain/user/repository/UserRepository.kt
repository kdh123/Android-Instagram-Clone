package com.dhkim.domain.user.repository

import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUser(): Flow<User?>
}