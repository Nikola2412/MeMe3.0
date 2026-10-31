package com.example.meme30.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.meme30.data.BackendApi

@Composable
fun VideoDetailScreen(
    videoId: String,
    title: String,
    creator: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var playbackError by remember(videoId) { mutableStateOf<String?>(null) }
    var videoView by remember(videoId) { mutableStateOf<VideoView?>(null) }

    DisposableEffect(videoView) {
        onDispose { videoView?.stopPlayback() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
        ) {
            Text("← Back")
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(androidx.compose.ui.graphics.Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    VideoView(context).also { view ->
                        videoView = view
                        view.setMediaController(MediaController(context).apply {
                            setAnchorView(view)
                        })
                        view.setOnErrorListener { _, _, _ ->
                            playbackError = "Could not play this video."
                            true
                        }
                        view.setVideoURI(Uri.parse(BackendApi.videoStreamUrl(videoId)))
                        view.setOnPreparedListener { it.start() }
                    }
                }
            )
            if (playbackError != null) {
                Text(
                    text = playbackError.orEmpty(),
                    modifier = Modifier.padding(16.dp),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                creator,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
