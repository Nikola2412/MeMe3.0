package com.example.meme30.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meme30.data.BackendApi
import com.example.meme30.data.Meme
import com.example.meme30.ui.components.RemoteImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.json.JSONException
import java.io.IOException

@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MemesScreen(
    onMemeClick: (Meme) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var memes by remember { mutableStateOf<List<Meme>>(emptyList()) }
    var loadingInitialPage by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var initialError by remember { mutableStateOf<String?>(null) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(reloadKey) {
        memes = emptyList()
        hasMore = true
        pageError = null
        initialError = null
        loadingInitialPage = true
        try {
            val page = BackendApi.getMemes(offset = 0)
            memes = page
            hasMore = page.size >= BackendApi.MEME_PAGE_SIZE
        } catch (exception: IOException) {
            initialError = exception.message ?: "Could not load memes."
        } catch (exception: JSONException) {
            initialError = "The backend returned invalid meme data."
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            loadingInitialPage = false
        }
    }

    LaunchedEffect(gridState, memes.size, hasMore, pageError, loadingInitialPage) {
        if (loadingInitialPage || memes.isEmpty() || !hasMore || pageError != null) {
            return@LaunchedEffect
        }

        snapshotFlow {
            gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.first { lastVisibleIndex ->
            lastVisibleIndex >= (memes.size - 4).coerceAtLeast(0)
        }

        loadingMore = true
        try {
            val page = BackendApi.getMemes(offset = memes.size)
            memes = memes + page
            hasMore = page.size >= BackendApi.MEME_PAGE_SIZE
        } catch (exception: IOException) {
            pageError = exception.message ?: "Could not load more memes."
        } catch (exception: JSONException) {
            pageError = "The backend returned invalid meme data."
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            loadingMore = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Meme Gallery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A little laugh goes a long way.",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when {
            loadingInitialPage -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
            initialError != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = initialError.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { reloadKey++ },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Try again")
                }
            }
            memes.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No memes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(memes, key = { it.id }) { meme ->
                    MemeCard(
                        meme = meme,
                        onClick = { onMemeClick(meme) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
                if (loadingMore || pageError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (loadingMore) {
                                LoadingIndicator()
                            } else {
                                Text(
                                    text = pageError.orEmpty(),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(
                                    onClick = { pageError = null },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Try again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MemeCard(
    meme: Meme,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    ElevatedCard(onClick = onClick) {
        Column {
            with(sharedTransitionScope) {
                RemoteImage(
                    memeId = meme.id,
                    contentDescription = "Meme by ${meme.creator}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "meme-${meme.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )
            }
            Text(
                text = meme.creator,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
