package com.dhkim.login

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.domain.user.model.User

@Composable
fun LoginScreen(
    user: User?,
    onAction: (LoginAction) -> Unit
) {
    LoginContent(
        name = user?.name,
        email = user?.email,
        onGoogleSignInClick = { onAction(LoginAction.Login) },
        onSignOutClick = { onAction(LoginAction.Logout) }
    )
}
@Composable
private fun LoginContent(
    name: String?,
    email: String?,
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
                onGoogleSignInClick = {},
                onSignOutClick = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class LoginScreenPreviews