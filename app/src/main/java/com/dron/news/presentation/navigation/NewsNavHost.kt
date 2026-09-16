package com.dron.news.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dron.news.presentation.screen.settings.SettingsScreen
import com.dron.news.presentation.screen.subscriptions.SubscriptionsScreen

object Routes {
    const val SUBSCRIPTIONS = "subscriptions"
    const val SETTINGS = "settings"
}

@Composable
fun NewsNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SUBSCRIPTIONS
    ) {
        composable(Routes.SUBSCRIPTIONS) {
            SubscriptionsScreen(
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}