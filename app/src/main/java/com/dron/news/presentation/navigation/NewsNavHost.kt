package com.dron.news.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dron.news.presentation.screen.settings.SettingsScreen
import com.dron.news.presentation.screen.subscriptions.SubscriptionsScreen

@Composable
fun NewsNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Subscriptions.route
    ) {
        composable(Screen.Subscriptions.route) {
            SubscriptionsScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    // Простые экраны (без параметров)
    data object Subscriptions : Screen("subscriptions")
    data object Settings : Screen("settings")

    // Экраны с параметрами (когда добавите)
    // data class ArticleDetails(val articleId: Long) : Screen("article/$articleId") {
    //     companion object {
    //         const val ROUTE = "article/{articleId}"
    //         const val ARG_ID = "articleId"
    //     }
    // }
}