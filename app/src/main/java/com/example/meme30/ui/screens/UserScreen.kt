package com.example.meme30.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.meme30.R
import com.example.meme30.Routes
import com.example.meme30.data.BackendApi
import kotlinx.coroutines.launch

private const val ACCOUNT_PREFS = "me_me_account"
private const val ACCOUNT_USERNAME = "username"
private const val ACCOUNT_ID = "channel_id"

@Composable
fun UserScreen(navController: NavHostController) {
    val context = LocalContext.current
    val accountPrefs = remember {
        context.getSharedPreferences(ACCOUNT_PREFS, android.content.Context.MODE_PRIVATE)
    }
    var accountUsername by remember {
        mutableStateOf(accountPrefs.getString(ACCOUNT_USERNAME, null))
    }
    var accountId by remember {
        mutableStateOf(accountPrefs.getString(ACCOUNT_ID, null))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { navController.navigate(Routes.SETTINGS) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_settings_24),
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item {
            if (accountUsername != null && accountId != null) {
                AccountCard(
                    username = accountUsername.orEmpty(),
                    onSignOut = {
                        accountPrefs.edit()
                            .remove(ACCOUNT_USERNAME)
                            .remove(ACCOUNT_ID)
                            .apply()
                        accountUsername = null
                        accountId = null
                    }
                )
            } else {
                LoginCard(
                    onSignedIn = { username, channelId ->
                        accountPrefs.edit()
                            .putString(ACCOUNT_USERNAME, username)
                            .putString(ACCOUNT_ID, channelId)
                            .apply()
                        accountUsername = username
                        accountId = channelId
                    }
                )
            }
        }


    }
}

@Composable
private fun AccountCard(
    username: String,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.firstOrNull()?.uppercase().orEmpty(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Signed in",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun LoginCard(
    onSignedIn: (username: String, channelId: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty() || password.isEmpty()) {
            errorMessage = "Enter a username and password."
            return
        }

        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                if (isRegistering) {
                    BackendApi.createAccount(trimmedUsername, password)
                }

                val channelId = BackendApi.authenticate(trimmedUsername, password)
                if (channelId != null) {
                    onSignedIn(trimmedUsername, channelId)
                } else {
                    errorMessage = if (isRegistering) {
                        "Account could not be created."
                    } else {
                        "Incorrect username or password."
                    }
                }
            } catch (_: Exception) {
                errorMessage = "Could not connect to the server."
            } finally {
                isLoading = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isRegistering) "Create account" else "Sign in",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = 
                if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() })
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = ::submit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(
                    if (isLoading) {
                        "Please wait..."
                    } else if (isRegistering) {
                        "Create account"
                    } else {
                        "Sign in"
                    }
                )
            }
            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    errorMessage = null
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    if (isRegistering) {
                        "Already have an account? Sign in"
                    } else {
                        "New here? Create an account"
                    }
                )
            }
        }
    }
}

@Composable
private fun UserMenuCard(
    title: String,
    subtitle: String,
    icon: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
