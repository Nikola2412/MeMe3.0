package com.example.meme30.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class FeedVideo(
    val title: String,
    val creator: String,
    val viewsAndAge: String,
    val duration: String,
    val category: String,
    val emoji: String,
    val previewText: String,
    val colors: List<Color>,
    val avatarColor: Color
)

private val feedCategories = listOf("All", "Comedy", "Gaming", "Music", "Viral")

internal val feedVideos = listOf(
    FeedVideo(
        title = "When you say “one more episode” at 2 AM",
        creator = "Daily Dose of Memes",
        viewsAndAge = "1.2M views · 2 days ago",
        duration = "0:38",
        category = "Comedy",
        emoji = "🥱",
        previewText = "JUST ONE MORE",
        colors = listOf(Color(0xFF512DA8), Color(0xFFEC407A)),
        avatarColor = Color(0xFF7E57C2)
    ),
    FeedVideo(
        title = "POV: the squad finally wins a game",
        creator = "Pixel Party",
        viewsAndAge = "842K views · 5 hours ago",
        duration = "0:24",
        category = "Gaming",
        emoji = "🎮",
        previewText = "WE DID IT!",
        colors = listOf(Color(0xFF00695C), Color(0xFF26A69A)),
        avatarColor = Color(0xFF00897B)
    ),
    FeedVideo(
        title = "This beat drop has no business being this good",
        creator = "Loop Lab",
        viewsAndAge = "3.4M views · 1 week ago",
        duration = "1:12",
        category = "Music",
        emoji = "🎧",
        previewText = "WAIT FOR IT...",
        colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5)),
        avatarColor = Color(0xFF1E88E5)
    ),
    FeedVideo(
        title = "The tiny dog that thinks it runs the house",
        creator = "Very Good Clips",
        viewsAndAge = "567K views · 3 days ago",
        duration = "0:51",
        category = "Viral",
        emoji = "🐶",
        previewText = "THE BOSS",
        colors = listOf(Color(0xFFEF6C00), Color(0xFFFFCA28)),
        avatarColor = Color(0xFFF57C00)
    ),
    FeedVideo(
        title = "Expectation vs reality: trying to be productive",
        creator = "Daily Dose of Memes",
        viewsAndAge = "290K views · 1 day ago",
        duration = "0:46",
        category = "Comedy",
        emoji = "📱",
        previewText = "FOCUS MODE",
        colors = listOf(Color(0xFFAD1457), Color(0xFFEC407A)),
        avatarColor = Color(0xFFD81B60)
    ),
    FeedVideo(
        title = "The most satisfying game finish of the week",
        creator = "Pixel Party",
        viewsAndAge = "1.8M views · 6 days ago",
        duration = "0:33",
        category = "Gaming",
        emoji = "🏆",
        previewText = "CLUTCH!",
        colors = listOf(Color(0xFF283593), Color(0xFF5C6BC0)),
        avatarColor = Color(0xFF3949AB)
    )
)



@Composable
fun HomeScreen() {
    var selectedCategory by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()

    val isHeaderCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val categoryHeight by animateDpAsState(
        targetValue = if (isHeaderCollapsed) 0.dp else 40.dp,
        label = "categoryHeight"
    )

    val visibleVideos = if (selectedCategory == "All") {
        feedVideos
    } else {
        feedVideos.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader()
        CategoryFilters(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            categoryHeight = categoryHeight,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            items(visibleVideos, key = { it.title }) { video ->
                VideoCard(video)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MeMe 3.0",
                modifier = Modifier.padding(start = 9.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
private fun CategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categoryHeight: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .height(categoryHeight),

        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        feedCategories.forEach { category ->
            val selected = category == selectedCategory
            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
internal fun VideoCard(video: FeedVideo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(video.colors))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, end = 120.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = video.previewText,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = video.emoji,
                    modifier = Modifier.padding(top = 2.dp),
                    fontSize = 62.sp
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(9.dp),
                shape = RoundedCornerShape(5.dp),
                color = Color.Black.copy(alpha = 0.78f)
            ) {
                Text(
                    text = video.duration,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(video.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = video.creator.first().toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp, end = 8.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.creator} · ${video.viewsAndAge}",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "⋮",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 22.sp,
                lineHeight = 20.sp
            )
        }
    }
}
