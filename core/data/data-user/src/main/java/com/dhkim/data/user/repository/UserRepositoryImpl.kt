package com.dhkim.data.user.repository

import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return flow {
            firebaseAuth.currentUser?.run {
                emit(
                    User(
                        id = uid,
                        name = displayName ?: "",
                        email = email ?: "",
                        profileUrl = "$photoUrl"
                    )
                )
            } ?: emit(null)
        }
    }
}