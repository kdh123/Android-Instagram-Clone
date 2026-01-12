package com.dhkim.data.login.dataSource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.dhkim.data.login.model.UserDto
import com.dhkim.domain.login.exception.LoginFailException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRemoteSource @Inject constructor(
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    @param:ApplicationContext private val context: Context
) {

    private val userRef = firebaseDatabase.getReference("users")

    suspend fun login() {
        try {
            val result = credentialManager.getCredential(
                request = credentialRequest,
                context = context,
            )
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val user = firebaseAuthWithGoogleToken(firebaseAuth, googleIdTokenCredential.idToken) ?: throw LoginFailException("Unknown User")
            registerUser(user)
        } catch (e: Exception) {
            throw LoginFailException(e.message ?: "Unknown Error")
        }
    }

    fun logout(): Flow<Unit> {
        return flow {
            firebaseAuth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            emit(Unit)
        }
    }

    private suspend fun firebaseAuthWithGoogleToken(auth: FirebaseAuth, idToken: String): FirebaseUser? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user
        } catch (e: Exception) {
            throw LoginFailException(e.message)
        }
    }

    private suspend fun registerUser(firebaseUser: FirebaseUser) {
        val user = UserDto(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            profileUrl = "${firebaseUser.photoUrl}"
        )

        val userRef = userRef.child(user.id)
        userRef.setValue(user).await()
    }
}