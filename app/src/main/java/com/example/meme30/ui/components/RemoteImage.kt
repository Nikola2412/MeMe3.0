package com.example.meme30.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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

@Composable
fun RemoteImage(
    memeId: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxImageDimensionPx: Int = 480,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cachedBitmap = remember(memeId, maxImageDimensionPx) {
        BackendApi.getCachedMemeImage(memeId, maxImageDimensionPx)
    }
    var bitmap by remember(memeId, maxImageDimensionPx) {
        mutableStateOf<Bitmap?>(cachedBitmap)
    }
    var loading by remember(memeId, maxImageDimensionPx) {
        mutableStateOf(cachedBitmap == null)
    }
    var failed by remember(memeId, maxImageDimensionPx) { mutableStateOf(false) }

    LaunchedEffect(memeId, maxImageDimensionPx) {
        try {
            bitmap = BackendApi.getMemeImage(memeId, maxImageDimensionPx)
        } catch (_: IOException) {
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
            if (failed) {
                androidx.compose.material3.Text(
                    text = "Image unavailable",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (loading) {
                androidx.compose.material3.Text(
                    text = "Loading image…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
