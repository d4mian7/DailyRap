package com.dailyrap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
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

/**
 * Akcje nawigacji wykonujemy tylko z ekranu, który jest aktualnie aktywny (RESUMED).
 * Bez tego szybkie podwójne kliknięcie „wstecz" zdejmowało też ekran główny
 * (zostawało puste fioletowe tło), a podwójne kliknięcie kafelka otwierało grę dwa razy.
 */
private fun NavBackStackEntry.isActive(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.popBackFrom(entry: NavBackStackEntry) {
    if (entry.isActive()) popBackStack()
}

private fun NavHostController.navigateFrom(entry: NavBackStackEntry, route: String) {
    if (entry.isActive()) navigate(route)
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) { entry ->
            HomeScreen(
                onNavigateToConnections = { navController.navigateFrom(entry, Routes.CONNECTIONS) },
                onNavigateToLiteralnie = { navController.navigateFrom(entry, Routes.LITERALNIE) },
                onNavigateToCrossword = { navController.navigateFrom(entry, Routes.CROSSWORD) },
                onNavigateToStats = { navController.navigateFrom(entry, Routes.STATS) },
                onNavigateToHelp = { navController.navigateFrom(entry, Routes.HELP) },
                onNavigateToSettings = { navController.navigateFrom(entry, Routes.SETTINGS) }
            )
        }
        composable(Routes.CONNECTIONS) { entry ->
            ConnectionsScreen(
                onNavigateBack = { navController.popBackFrom(entry) }
            )
        }
        composable(Routes.LITERALNIE) { entry ->
            LiteralnieScreen(
                onNavigateBack = { navController.popBackFrom(entry) }
            )
        }
        composable(Routes.CROSSWORD) { entry ->
            CrosswordScreen(
                onNavigateBack = { navController.popBackFrom(entry) }
            )
        }
        composable(Routes.STATS) { entry ->
            StatsScreen(
                onNavigateBack = { navController.popBackFrom(entry) }
            )
        }
        composable(Routes.HELP) { entry ->
            HelpScreen(
                onNavigateBack = { navController.popBackFrom(entry) },
                onNavigateToTutorial = { type ->
                    navController.navigateFrom(entry, "help/$type")
                }
            )
        }
        composable(Routes.HELP_RAPCONNECT) { entry ->
            RapConnectTutorial(onClose = { navController.popBackFrom(entry) })
        }
        composable(Routes.HELP_RAPWORD) { entry ->
            RapWordTutorial(onClose = { navController.popBackFrom(entry) })
        }
        composable(Routes.HELP_RAPCROSSWORD) { entry ->
            RapCrosswordTutorial(onClose = { navController.popBackFrom(entry) })
        }
        composable(Routes.SETTINGS) { entry ->
            SettingsScreen(
                onNavigateBack = { navController.popBackFrom(entry) }
            )
        }
    }
}
