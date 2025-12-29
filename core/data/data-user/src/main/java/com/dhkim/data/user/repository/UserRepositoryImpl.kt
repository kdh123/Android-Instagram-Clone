package com.dhkim.data.user.repository

import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return flow {
            val firebaseUser = firebaseAuth.currentUser ?: kotlin.run {
                emit(null)
                return@flow
            }

            val user = try {
                firebaseUser.getIdToken(true).await()
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profileUrl = "${firebaseUser.photoUrl}"
                )
            } catch (_: Exception) {
                null
            }

            emit(user)
        }.catch {
            emit(null)
        }
    }
}