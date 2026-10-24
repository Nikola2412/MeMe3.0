
package com.example.meme30.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.meme30.R
import com.example.meme30.Screen

private data class NavItem(
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
    val screen: Screen
)

@Composable
fun BottomNavigation(
    navController: NavHostController
) {
    val items = listOf(
        NavItem(
            label = "Home",
            icon = R.drawable.home_24_rounded_filled,
            selectedIcon = R.drawable.home_24_rounded_filled,
            screen = Screen.Home
        ),
        NavItem(
            label = "Search",
            icon = R.drawable.rounded_search_24,
            selectedIcon = R.drawable.rounded_search_24,
            screen = Screen.Search
        ),
        NavItem(
            label = "Memes",
            icon = R.drawable.rounded_library_music_24,
            selectedIcon = R.drawable.rounded_library_music_24,
            screen = Screen.Memes
        )
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route

            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 400f
                ),
                label = "iconScale"
            )

            val iconSize by animateDpAsState(
                targetValue = if (selected) 27.dp else 24.dp,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = 400f
                ),
                label = "iconSize"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.screen.route) {
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selected) {
                                item.selectedIcon
                            } else {
                                item.icon
                            }
                        ),
                        contentDescription = item.label,
                        modifier = Modifier
                            .scale(iconScale)
                    )
                },
                label = {
                    Text(item.label)
                },
                alwaysShowLabel = true
            )
        }
    }
}
