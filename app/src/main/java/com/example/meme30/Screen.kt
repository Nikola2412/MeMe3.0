
package com.example.meme30

sealed class Screen(val route: String) {
    data object Home : Screen(MemeRoutes.HOME)
    data object Search : Screen(MemeRoutes.SEARCH)
    data object Memes : Screen(MemeRoutes.MEMES)
}
