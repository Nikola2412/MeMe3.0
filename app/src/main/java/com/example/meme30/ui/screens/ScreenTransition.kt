package com.example.meme30.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

internal suspend fun AnimatedVisibilityScope.awaitEnterTransition() {
    snapshotFlow {
        transition.currentState == EnterExitState.Visible &&
            transition.targetState == EnterExitState.Visible
    }.first { it }
}
