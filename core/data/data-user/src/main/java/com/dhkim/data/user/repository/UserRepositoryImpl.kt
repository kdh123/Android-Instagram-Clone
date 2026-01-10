package com.dhkim.data.user.repository

import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return flow {
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                emit(null)
                return@flow
            }

            val cachedUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                profileUrl = "${firebaseUser.photoUrl}"
            )
            emit(cachedUser)

            try {
                firebaseUser.getIdToken(false).await()
            } catch (_: Exception) {
            }
        }
    }
}