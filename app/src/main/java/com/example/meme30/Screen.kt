
package com.example.meme30

sealed class Screen(val route: String) {
    data object Home : Screen(Routes.HOME)
    data object Memes : Screen(Routes.MEMES)
    data object User : Screen(Routes.USER)
}
