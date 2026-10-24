
package com.example.meme30

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.meme30.ui.screens.HomeScreen
import com.example.meme30.ui.screens.MemesScreen
import com.example.meme30.ui.screens.SearchScreen

private const val TRANSITION_DURATION = 380

private val NavigationEasing =
    CubicBezierEasing(0.2f, 0f, 0f, 1f)

private fun routeIndex(route: String?): Int? =
    when (route?.substringBefore("/")) {
        MemeRoutes.HOME -> 0
        MemeRoutes.SEARCH -> 1
        MemeRoutes.MEMES -> 2
        else -> null
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
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = MemeRoutes.HOME,
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
        composable(MemeRoutes.HOME) {
            HomeScreen()
        }

        composable(MemeRoutes.SEARCH) {
            SearchScreen()
        }

        composable(MemeRoutes.MEMES) {
            MemesScreen()
        }
    }
}
