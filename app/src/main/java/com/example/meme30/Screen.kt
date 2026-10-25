
package com.example.meme30

sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Search : Screen(Routes.SEARCH)
    data object Memes : Screen(Routes.MEMES)
}
