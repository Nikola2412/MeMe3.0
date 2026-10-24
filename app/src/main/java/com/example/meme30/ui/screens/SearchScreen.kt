
package com.example.meme30.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.meme30.R

private val searchCategories =
    listOf("All") + feedVideos.map { it.category }.distinct()

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val focusManager = LocalFocusManager.current

    val listState = rememberLazyListState()

    val isHeaderCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 0
        }
    }

    val searchFieldVerticalPadding by animateDpAsState(
        targetValue = if (isHeaderCollapsed) 4.dp else 12.dp,
        label = "searchFieldPadding"
    )

    val normalizedQuery = query.trim()

    val visibleVideos = remember(normalizedQuery, selectedCategory) {
        feedVideos.filter { video ->
            val matchesQuery = normalizedQuery.isBlank() ||
                    video.title.contains(normalizedQuery, ignoreCase = true) ||
                    video.creator.contains(normalizedQuery, ignoreCase = true) ||
                    video.category.contains(normalizedQuery, ignoreCase = true) ||
                    video.previewText.contains(normalizedQuery, ignoreCase = true)

            val matchesCategory =
                selectedCategory == "All" ||
                        video.category == selectedCategory

            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Collapsible heading
        AnimatedVisibility(
            visible = !isHeaderCollapsed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp)
                    .padding(bottom = 4.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = "Find your next laugh",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Search videos, creators, or categories",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = searchFieldVerticalPadding),
            placeholder = {
                Text("Try \"gaming\" or \"dog\"")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = R.drawable.rounded_search_24
                    ),
                    contentDescription = "Search videos"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    /*
                    IconButton (
                        onClick = {
                            query = ""
                            selectedCategory = "All"
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.rounded_search_24
                            ),
                            contentDescription = "Clear search"
                        )
                    }
                     */
                    TextButton(
                        onClick = {
                            query = ""
                            selectedCategory = "All"
                        }
                    ) {
                        Text("Clear filters")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        // Horizontally scrollable categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            searchCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    label = {
                        Text(category)
                    }
                )
            }
        }

        // Results or empty state
        if (visibleVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = if (feedVideos.isEmpty()) {
                            "No videos yet"
                        } else {
                            "No videos found"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (feedVideos.isEmpty()) {
                            "Check back later for new videos."
                        } else {
                            "Try a different search term or category."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (normalizedQuery.isNotBlank() ||
                        selectedCategory != "All"
                    ) {
                        TextButton(
                            onClick = {
                                query = ""
                                selectedCategory = "All"
                            }
                        ) {
                            Text("Clear filters")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = visibleVideos,
                    key = { video ->
                        "${video.creator}:${video.title}"
                    }
                ) { video ->
                    VideoCard(video)
                }
            }
        }
    }
}
