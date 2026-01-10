package com.dhkim.data.login.dataSource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.dhkim.domain.login.exception.LoginFailException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRemoteSource @Inject constructor(
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context
) {

    suspend fun login() {
        try {
            val result = credentialManager.getCredential(
                request = credentialRequest,
                context = context,
            )
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val user = firebaseAuthWithGoogleToken(auth, googleIdTokenCredential.idToken)
        } catch (e: Exception) {
            throw LoginFailException(e.message ?: "Unknown Error")
        }
    }

    fun logout(): Flow<Unit> {
        return flow {
            auth.signOut()
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
}