package com.example.meme30.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.meme30.data.BackendApi
import android.graphics.Bitmap
import kotlinx.coroutines.CancellationException
import java.io.IOException

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RemoteImage(
    memeId: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(memeId) { mutableStateOf<Bitmap?>(null) }
    var loading by remember(memeId) { mutableStateOf(true) }
    var failed by remember(memeId) { mutableStateOf(false) }

    LaunchedEffect(memeId) {
        try {
            bitmap = BackendApi.getMemeImage(memeId)
        } catch (exception: IOException) {
            failed = true
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            loading = false
        }
    }

    when {
        bitmap != null -> Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
        else -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                LoadingIndicator()
            } else if (failed) {
                androidx.compose.material3.Text(
                    text = "Image unavailable",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
