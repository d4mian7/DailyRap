package com.dailyrap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailyrap.app.ui.screen.connections.ConnectionsScreen
import com.dailyrap.app.ui.screen.crossword.CrosswordScreen
import com.dailyrap.app.ui.screen.help.HelpScreen
import com.dailyrap.app.ui.screen.help.tutorial.RapConnectTutorial
import com.dailyrap.app.ui.screen.help.tutorial.RapCrosswordTutorial
import com.dailyrap.app.ui.screen.help.tutorial.RapWordTutorial
import com.dailyrap.app.ui.screen.home.HomeScreen
import com.dailyrap.app.ui.screen.literalnie.LiteralnieScreen
import com.dailyrap.app.ui.screen.settings.SettingsScreen
import com.dailyrap.app.ui.screen.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val CONNECTIONS = "connections"
    const val LITERALNIE = "literalnie"
    const val CROSSWORD = "crossword"
    const val STATS = "stats"
    const val HELP = "help"
    const val HELP_RAPCONNECT = "help/rapconnect"
    const val HELP_RAPWORD = "help/rapword"
    const val HELP_RAPCROSSWORD = "help/rapcrossword"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToConnections = { navController.navigate(Routes.CONNECTIONS) },
                onNavigateToLiteralnie = { navController.navigate(Routes.LITERALNIE) },
                onNavigateToCrossword = { navController.navigate(Routes.CROSSWORD) },
                onNavigateToStats = { navController.navigate(Routes.STATS) },
                onNavigateToHelp = { navController.navigate(Routes.HELP) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.CONNECTIONS) {
            ConnectionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.LITERALNIE) {
            LiteralnieScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CROSSWORD) {
            CrosswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.STATS) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTutorial = { type ->
                    navController.navigate("help/$type")
                }
            )
        }
        composable(Routes.HELP_RAPCONNECT) {
            RapConnectTutorial(onClose = { navController.popBackStack() })
        }
        composable(Routes.HELP_RAPWORD) {
            RapWordTutorial(onClose = { navController.popBackStack() })
        }
        composable(Routes.HELP_RAPCROSSWORD) {
            RapCrosswordTutorial(onClose = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
