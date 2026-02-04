package com.zenia.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

fun AnimatedContentTransitionScope<*>.slideIn() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

fun AnimatedContentTransitionScope<*>.slideOut() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

fun AnimatedContentTransitionScope<*>.popSlideIn() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))

fun AnimatedContentTransitionScope<*>.popSlideOut() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))

fun NavController.safeNavigate(route: String) {
    val lifecycle = this.currentBackStackEntry?.lifecycle
    if (lifecycle != null && lifecycle.currentState == Lifecycle.State.RESUMED) {
        this.navigate(route) {
            launchSingleTop = true
        }
    }
}