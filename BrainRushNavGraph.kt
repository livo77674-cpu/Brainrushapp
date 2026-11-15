package com.brainrush.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.brainrush.app.presentation.BrainRushViewModel
import com.brainrush.app.presentation.difficulty.DifficultySelectionScreen
import com.brainrush.app.presentation.game.GameScreen
import com.brainrush.app.presentation.home.HomeScreen
import com.brainrush.app.presentation.modes.ModeSelectionScreen
import com.brainrush.app.presentation.operations.OperationsSelectionScreen
import com.brainrush.app.presentation.records.RecordsScreen
import com.brainrush.app.presentation.results.ResultsScreen
import com.brainrush.app.presentation.settings.SettingsScreen
import com.brainrush.app.presentation.statistics.StatisticsScreen

object BrainRushRoutes {
    const val HOME = "home"
    const val MODES = "modes"
    const val OPERATIONS = "operations"
    const val DIFFICULTY = "difficulty"
    const val GAME = "game"
    const val RESULTS = "results"
    const val STATISTICS = "statistics"
    const val RECORDS = "records"
    const val SETTINGS = "settings"
}

@Composable
fun BrainRushNavGraph(viewModel: BrainRushViewModel, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val userProgress by viewModel.userProgress.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val selectedConfig by viewModel.selectedConfig.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val lastSession by viewModel.lastCompletedSession.collectAsState()

    NavHost(navController = navController, startDestination = BrainRushRoutes.HOME, modifier = modifier) {
        composable(BrainRushRoutes.HOME) {
            HomeScreen(
                progress = userProgress,
                scoringEngine = viewModel.scoringEngine,
                onStartFlow = { navController.navigate(BrainRushRoutes.MODES) },
                onQuickStartMode = { viewModel.startQuickMode(it); navController.navigate(BrainRushRoutes.GAME) },
                onOpenStatistics = { navController.navigate(BrainRushRoutes.STATISTICS) },
                onOpenRecords = { navController.navigate(BrainRushRoutes.RECORDS) },
                onOpenSettings = { navController.navigate(BrainRushRoutes.SETTINGS) }
            )
        }
        composable(BrainRushRoutes.MODES) {
            ModeSelectionScreen(selectedConfig, viewModel::selectMode, viewModel::setTrainingQuestionCount, { navController.navigate(BrainRushRoutes.OPERATIONS) }, { navController.popBackStack() })
        }
        composable(BrainRushRoutes.OPERATIONS) {
            OperationsSelectionScreen(selectedConfig.operations, viewModel::toggleOperation, { navController.navigate(BrainRushRoutes.DIFFICULTY) }, { navController.popBackStack() })
        }
        composable(BrainRushRoutes.DIFFICULTY) {
            DifficultySelectionScreen(selectedConfig.difficulty, viewModel::selectDifficulty, {
                viewModel.startGameSession()
                navController.navigate(BrainRushRoutes.GAME) { popUpTo(BrainRushRoutes.HOME) }
            }, { navController.popBackStack() })
        }
        composable(BrainRushRoutes.GAME) {
            GameScreen(gameState, appSettings, viewModel::onDigitPressed, viewModel::onDeletePressed, viewModel::onSubmitPressed, viewModel::pauseGame, viewModel::resumeGame, { navController.popBackStack(BrainRushRoutes.HOME, false) }, {
                navController.navigate(BrainRushRoutes.RESULTS) { popUpTo(BrainRushRoutes.HOME) }
            })
        }
        composable(BrainRushRoutes.RESULTS) {
            ResultsScreen(lastSession, {
                viewModel.startGameSession()
                navController.navigate(BrainRushRoutes.GAME) { popUpTo(BrainRushRoutes.HOME) }
            }, { navController.popBackStack(BrainRushRoutes.HOME, false) })
        }
        composable(BrainRushRoutes.STATISTICS) { StatisticsScreen(userProgress) { navController.popBackStack() } }
        composable(BrainRushRoutes.RECORDS) { RecordsScreen(userProgress) { navController.popBackStack() } }
        composable(BrainRushRoutes.SETTINGS) { SettingsScreen(appSettings, viewModel::toggleSound, viewModel::toggleVibration, viewModel::setThemeMode, viewModel::resetAllStatistics) { navController.popBackStack() } }
    }
}
