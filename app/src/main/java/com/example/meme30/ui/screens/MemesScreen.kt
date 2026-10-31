package com.example.meme30.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meme30.data.BackendApi
import com.example.meme30.data.Meme
import com.example.meme30.Routes
import com.example.meme30.ui.components.RemoteImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException

internal class MemesScreenState {
    val memes = mutableStateOf<List<Meme>>(emptyList())
    val visibleMemeCount = mutableIntStateOf(0)
    val loadingInitialPage = mutableStateOf(true)
    val loadingMore = mutableStateOf(false)
    val initialError = mutableStateOf<String?>(null)
    val pageError = mutableStateOf<String?>(null)
    val hasMore = mutableStateOf(true)
    val reloadKey = mutableIntStateOf(0)
    val initialLoadComplete = mutableStateOf(false)
}

@OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
internal fun MemesScreen(
    screenState: MemesScreenState,
    onMemeClick: (Meme) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var memes by screenState.memes
    var visibleMemeCount by screenState.visibleMemeCount
    var loadingInitialPage by screenState.loadingInitialPage
    var loadingMore by screenState.loadingMore
    var initialError by screenState.initialError
    var pageError by screenState.pageError
    var hasMore by screenState.hasMore
    var reloadKey by screenState.reloadKey
    var initialLoadComplete by screenState.initialLoadComplete
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    suspend fun loadPage(offset: Int, initialPage: Boolean) {
        if (initialPage) {
            loadingInitialPage = true
            initialError = null
        } else {
            loadingMore = true
            pageError = null
        }
        try {
            val page = BackendApi.getMemes(offset)
            memes = if (initialPage) page else memes + page
            visibleMemeCount = if (initialPage) {
                minOf(INITIAL_VISIBLE_MEMES, page.size)
            } else {
                minOf(
                    visibleMemeCount + MEMES_PER_BATCH,
                    memes.size
                )
            }
            hasMore = page.size >= BackendApi.MEME_PAGE_SIZE
        } catch (exception: IOException) {
            if (initialPage) {
                initialError = exception.message ?: "Could not load memes."
            } else {
                pageError = exception.message ?: "Could not load more memes."
            }
        } catch (exception: JSONException) {
            if (initialPage) {
                initialError = "The backend returned invalid meme data."
            } else {
                pageError = "The backend returned invalid meme data."
            }
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            if (initialPage) {
                loadingInitialPage = false
            } else {
                loadingMore = false
            }
        }
    }

    LaunchedEffect(reloadKey) {
        if (initialLoadComplete) return@LaunchedEffect
        animatedVisibilityScope.awaitEnterTransition()
        memes = emptyList()
        visibleMemeCount = 0
        hasMore = true
        pageError = null
        initialError = null
        loadPage(offset = 0, initialPage = true)
        initialLoadComplete = true
    }

    fun requestNextPage(scope: CoroutineScope) {
        if (loadingMore || loadingInitialPage || !hasMore) return
        if (visibleMemeCount < memes.size) {
            visibleMemeCount = minOf(
                visibleMemeCount + MEMES_PER_BATCH,
                memes.size
            )
            return
        }
        loadingMore = true
        scope.launch {
            loadPage(offset = memes.size, initialPage = false)
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
                    onClick = {
                        initialLoadComplete = false
                        reloadKey++
                    },
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = visibleMemeCount,
                    key = { index -> memes[index].id }
                ) { index ->
                    val meme = memes[index]
                    MemeCard(
                        meme = meme,
                        onClick = { onMemeClick(meme) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
                if (
                    loadingMore ||
                    pageError != null ||
                    visibleMemeCount < memes.size ||
                    hasMore
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (loadingMore) {
                                Text(
                                    text = "Loading more memes…",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (pageError != null) {
                                Text(
                                    text = pageError.orEmpty(),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(
                                    onClick = {
                                        requestNextPage(coroutineScope)
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Try again")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        requestNextPage(coroutineScope)
                                    }
                                ) {
                                    Text("Load more memes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val INITIAL_VISIBLE_MEMES = 6
private const val MEMES_PER_BATCH = 6

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
                        .clip(RoundedCornerShape(16.dp))
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = Routes.memeImageSharedKey(meme.id)
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp))
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
