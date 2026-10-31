
package com.example.meme30

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.meme30.ui.screens.HomeScreen
import com.example.meme30.ui.screens.MemeDetailScreen
import com.example.meme30.ui.screens.MemesScreen
import com.example.meme30.ui.screens.SearchScreen
import com.example.meme30.ui.screens.VideoDetailScreen

private const val TRANSITION_DURATION = 380

private val NavigationEasing =
    CubicBezierEasing(0.2f, 0f, 0f, 1f)

private fun routeIndex(route: String?): Int? =
    when (route?.substringBefore("/")) {
        Routes.HOME -> 0
        Routes.SEARCH -> 1
        Routes.MEMES -> 2
        Routes.MEME_DETAIL.substringBefore("/") -> 2
        Routes.VIDEO_DETAIL.substringBefore("/") -> 0
        else -> 0
    }

private fun enterPage(
    fromRoute: String?,
    toRoute: String?,
    fallback: EnterTransition
): EnterTransition {
    val from = routeIndex(fromRoute) ?: return fallback
    val to = routeIndex(toRoute) ?: return fallback

    if (from == to) return fallback

    return slideInHorizontally(
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            easing = NavigationEasing
        ),
        initialOffsetX = { width ->
            if (to > from) width / 2 else -width / 2
        }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION / 2
        )
    )
}

private fun exitPage(
    fromRoute: String?,
    toRoute: String?,
    fallback: ExitTransition
): ExitTransition {
    val from = routeIndex(fromRoute) ?: return fallback
    val to = routeIndex(toRoute) ?: return fallback

    if (from == to) return fallback

    return slideOutHorizontally(
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            easing = NavigationEasing
        ),
        targetOffsetX = { width ->
            if (to > from) -width / 2 else width / 2
        }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION / 2
        )
    )
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun AppNavigation(
    navController: NavHostController
) {
    SharedTransitionLayout {
        val sharedTransitionScope = this
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = {
                enterPage(
                    initialState.destination.route,
                    targetState.destination.route,
                    EnterTransition.None
                )
            },
            exitTransition = {
                exitPage(
                    initialState.destination.route,
                    targetState.destination.route,
                    ExitTransition.None
                )
            },
            popEnterTransition = {
                enterPage(
                    initialState.destination.route,
                    targetState.destination.route,
                    EnterTransition.None
                )
            },
            popExitTransition = {
                exitPage(
                    initialState.destination.route,
                    targetState.destination.route,
                    ExitTransition.None
                )
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController)
            }

            composable(Routes.SEARCH) {
                SearchScreen(navController)
            }

            composable(Routes.MEMES) {
                val animatedVisibilityScope = this
                MemesScreen(
                    onMemeClick = { meme ->
                        navController.navigate(Routes.memeDetail(meme))
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            composable(
                route = Routes.MEME_DETAIL,
                arguments = listOf(
                    navArgument("memeId") { type = NavType.StringType },
                    navArgument("creator") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val animatedVisibilityScope = this
                MemeDetailScreen(
                    memeId = entry.arguments?.getString("memeId").orEmpty(),
                    creator = entry.arguments?.getString("creator").orEmpty(),
                    onBack = { navController.popBackStack() },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            composable(
                route = Routes.VIDEO_DETAIL,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("creator") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                VideoDetailScreen(
                    videoId = entry.arguments?.getString("videoId").orEmpty(),
                    title = entry.arguments?.getString("title").orEmpty(),
                    creator = entry.arguments?.getString("creator").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
