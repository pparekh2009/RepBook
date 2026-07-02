package com.priyanshparekh.repbook.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.ui.screen.exercise.ExerciseScreen
import com.priyanshparekh.repbook.ui.screen.exercise.ExerciseViewModel
import com.priyanshparekh.repbook.ui.screen.finish.FinishScreen
import com.priyanshparekh.repbook.ui.screen.finish.FinishViewModel
import com.priyanshparekh.repbook.ui.screen.rest.RestScreen
import com.priyanshparekh.repbook.ui.screen.rest.RestViewModel
import com.priyanshparekh.repbook.ui.screen.home.HomeScreen
import com.priyanshparekh.repbook.ui.screen.home.HomeViewModel
import com.priyanshparekh.repbook.ui.screen.history.HistoryScreen
import com.priyanshparekh.repbook.ui.screen.history.HistoryViewModel
import com.priyanshparekh.repbook.ui.screen.settings.ExportScreen
import com.priyanshparekh.repbook.ui.screen.settings.ExportViewModel
import com.priyanshparekh.repbook.ui.screen.settings.ImportScreen
import com.priyanshparekh.repbook.ui.screen.settings.ImportViewModel
import com.priyanshparekh.repbook.ui.screen.settings.SettingsScreen
import com.priyanshparekh.repbook.ui.screen.settings.SettingsViewModel
import com.priyanshparekh.repbook.ui.screen.workoutdetails.WorkoutDetailsScreen
import com.priyanshparekh.repbook.ui.screen.workoutdetails.WorkoutDetailsViewModel
import com.priyanshparekh.repbook.ui.screen.workouts.WorkoutsScreen
import com.priyanshparekh.repbook.ui.screen.workouts.WorkoutsViewModel

@Composable
fun RepBookNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(container)
            )
            HomeScreen(
                viewModel = viewModel,
                onStartWorkout = { workoutId, exerciseId ->
                    navController.navigate(Screen.Exercise.routeWith(workoutId, exerciseId, 1))
                }
            )
        }
        composable(Screen.Workouts.route) {
            val viewModel: WorkoutsViewModel = viewModel(
                factory = WorkoutsViewModel.factory(container)
            )
            WorkoutsScreen(
                viewModel = viewModel,
                onNavigateToDetails = { workoutId ->
                    navController.navigate(Screen.WorkoutDetails.routeWith(workoutId))
                }
            )
        }
        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.factory(container)
            )
            HistoryScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(container)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToExport = { navController.navigate(Screen.Export.route) },
                onNavigateToImport = { navController.navigate(Screen.Import.route) }
            )
        }
        composable(Screen.Export.route) {
            val viewModel: ExportViewModel = viewModel(
                factory = ExportViewModel.factory(container)
            )
            ExportScreen(viewModel = viewModel, onNavigateUp = { navController.navigateUp() })
        }
        composable(Screen.Import.route) {
            val viewModel: ImportViewModel = viewModel(
                factory = ImportViewModel.factory(container)
            )
            ImportScreen(viewModel = viewModel, onNavigateUp = { navController.navigateUp() })
        }
        composable(
            route = Screen.WorkoutDetails.route,
            arguments = listOf(
                navArgument(Screen.WorkoutDetails.ARG_WORKOUT_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getLong(Screen.WorkoutDetails.ARG_WORKOUT_ID)
            val viewModel: WorkoutDetailsViewModel = viewModel(
                factory = WorkoutDetailsViewModel.factory(workoutId, container)
            )
            WorkoutDetailsScreen(
                viewModel = viewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = Screen.Exercise.route,
            arguments = listOf(
                navArgument(Screen.Exercise.ARG_WORKOUT_ID) { type = NavType.LongType },
                navArgument(Screen.Exercise.ARG_EXERCISE_ID) { type = NavType.LongType },
                navArgument(Screen.Exercise.ARG_SET_NO) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getLong(Screen.Exercise.ARG_WORKOUT_ID)
            val exerciseId = backStackEntry.arguments!!.getLong(Screen.Exercise.ARG_EXERCISE_ID)
            val setNo = backStackEntry.arguments!!.getInt(Screen.Exercise.ARG_SET_NO)
            val viewModel: ExerciseViewModel = viewModel(
                factory = ExerciseViewModel.factory(workoutId, exerciseId, setNo, container)
            )
            ExerciseScreen(
                viewModel = viewModel,
                onNavigateToRest = { wId, eId, completedSetNo ->
                    navController.navigate(Screen.Rest.routeWith(wId, eId, completedSetNo))
                }
            )
        }
        composable(
            route = Screen.Rest.route,
            arguments = listOf(
                navArgument(Screen.Rest.ARG_WORKOUT_ID) { type = NavType.LongType },
                navArgument(Screen.Rest.ARG_EXERCISE_ID) { type = NavType.LongType },
                navArgument(Screen.Rest.ARG_COMPLETED_SET_NO) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getLong(Screen.Rest.ARG_WORKOUT_ID)
            val exerciseId = backStackEntry.arguments!!.getLong(Screen.Rest.ARG_EXERCISE_ID)
            val completedSetNo = backStackEntry.arguments!!.getInt(Screen.Rest.ARG_COMPLETED_SET_NO)
            val viewModel: RestViewModel = viewModel(
                factory = RestViewModel.factory(workoutId, exerciseId, completedSetNo, container)
            )
            RestScreen(
                viewModel = viewModel,
                onNavigateToExercise = { wId, eId, setNo ->
                    navController.navigate(Screen.Exercise.routeWith(wId, eId, setNo))
                },
                onNavigateToFinish = { wId ->
                    navController.navigate(Screen.Finish.routeWith(wId))
                }
            )
        }
        composable(
            route = Screen.Finish.route,
            arguments = listOf(
                navArgument(Screen.Finish.ARG_WORKOUT_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getLong(Screen.Finish.ARG_WORKOUT_ID)
            val viewModel: FinishViewModel = viewModel(
                factory = FinishViewModel.factory(workoutId, container)
            )
            FinishScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun Placeholder(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}
