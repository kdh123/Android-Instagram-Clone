package com.dhkim.login

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.dhkim.designsystem.InstagramTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val auth: FirebaseAuth = remember { Firebase.auth }

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LoginContent(
        name = currentUser?.displayName,
        email = currentUser?.email,
        errorMessage = errorMessage,
        onGoogleSignInClick = {
            coroutineScope.launch {
                try {
                    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    val user = firebaseAuthWithGoogleToken(auth, googleIdTokenCredential.idToken)
                    currentUser = user
                    errorMessage = if (user == null) "Firebase authentication failed" else null
                } catch (e: GetCredentialException) {
                    errorMessage = "Google sign in failed: ${e.message}"
                    Log.w("LoginScreen", "GetCredentialException", e)
                }
            }
        },
        onSignOutClick = {
            coroutineScope.launch {
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                currentUser = null
            }
        }
    )
}

private suspend fun firebaseAuthWithGoogleToken(auth: FirebaseAuth, idToken: String): FirebaseUser? {
    return try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        authResult.user
    } catch (e: Exception) {
        Log.e("LoginScreen", "Firebase auth failed", e)
        null
    }
}


@Composable
private fun LoginContent(
    name: String?,
    email: String?,
    errorMessage: String?,
    onGoogleSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (name == null) {
            Text(
                text = "LoginScreen",
                style = InstagramTheme.typography.bodyLargeBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onGoogleSignInClick) {
                Text("Sign in with Google")
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Text("Welcome, $name : $email")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSignOutClick) {
                Text("Sign Out")
            }
        }
    }
}

@LoginScreenPreviews
@Composable
private fun LoginScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginContent(
                name = null,
                email = null,
                errorMessage = "Error message example",
                onGoogleSignInClick = {},
                onSignOutClick = {}
            )
        }
    }
}

@LoginScreenPreviews
@Composable
private fun LoggedInScreenPreview() {
    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginContent(
                name = "Tom",
                email = "abc@gmail.com",
                errorMessage = null,
                onGoogleSignInClick = {},
                onSignOutClick = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class LoginScreenPreviews