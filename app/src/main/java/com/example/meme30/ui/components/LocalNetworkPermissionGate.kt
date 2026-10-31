package com.example.meme30.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.meme30.BuildConfig

@Composable
fun LocalNetworkPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val permissionRequired = Build.VERSION.SDK_INT >= 37
    var permissionGranted by remember {
        mutableStateOf(
            !permissionRequired ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        permissionDenied = !granted
    }
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
        permissionDenied = !permissionGranted
    }

    if (permissionGranted) {
        content()
    } else {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Allow local network access") },
            text = {
                Text(
                    "MeMe needs to connect to the meme and video server on your local " +
                        "network at ${BuildConfig.BACKEND_BASE_URL} to load content."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (permissionDenied) {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            settingsLauncher.launch(intent)
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                        }
                    }
                ) {
                    Text(if (permissionDenied) "Open app settings" else "Continue")
                }
            },
            dismissButton = {
                if (permissionDenied) {
                    TextButton(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                        }
                    ) {
                        Text("Try again")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
