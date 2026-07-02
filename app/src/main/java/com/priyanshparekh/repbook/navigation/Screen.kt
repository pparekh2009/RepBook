package com.priyanshparekh.repbook.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workouts : Screen("workouts")
    object Settings : Screen("settings")

    object WorkoutDetails : Screen("workout_details/{workoutId}") {
        const val ARG_WORKOUT_ID = "workoutId"
        fun routeWith(workoutId: Long) = "workout_details/$workoutId"
    }

    object Exercise : Screen("exercise/{workoutId}/{exerciseId}/{setNo}") {
        const val ARG_WORKOUT_ID = "workoutId"
        const val ARG_EXERCISE_ID = "exerciseId"
        const val ARG_SET_NO = "setNo"
        fun routeWith(workoutId: Long, exerciseId: Long, setNo: Int) =
            "exercise/$workoutId/$exerciseId/$setNo"
    }

    object Rest : Screen("rest/{workoutId}/{exerciseId}/{completedSetNo}") {
        const val ARG_WORKOUT_ID = "workoutId"
        const val ARG_EXERCISE_ID = "exerciseId"
        const val ARG_COMPLETED_SET_NO = "completedSetNo"
        fun routeWith(workoutId: Long, exerciseId: Long, completedSetNo: Int) =
            "rest/$workoutId/$exerciseId/$completedSetNo"
    }

    object Finish : Screen("finish/{workoutId}") {
        const val ARG_WORKOUT_ID = "workoutId"
        fun routeWith(workoutId: Long) = "finish/$workoutId"
    }

    object History : Screen("history")
    object Export : Screen("export")
    object Import : Screen("import")
}
