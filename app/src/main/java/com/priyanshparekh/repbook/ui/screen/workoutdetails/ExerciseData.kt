package com.priyanshparekh.repbook.ui.screen.workoutdetails

data class ExerciseDataItem(
    val id: Long = -1L,
    val name: String,
    val isTimeBased: Boolean = false
)



object ExerciseData {

    val chestExercises = listOf(
        ExerciseDataItem(name = "Assisted Dip"),
        ExerciseDataItem(name = "Band-Assisted Bench Press"),
        ExerciseDataItem(name = "Bar Dip"),
        ExerciseDataItem(name = "Bench Press"),
        ExerciseDataItem(name = "Bench Press Against Band"),
        ExerciseDataItem(name = "Board Press"),
        ExerciseDataItem(name = "Cable Chest Press"),
        ExerciseDataItem(name = "Clap Push-Up"),
        ExerciseDataItem(name = "Close-Grip Bench Press"),
        ExerciseDataItem(name = "Close-Grip Feet-Up Bench Press"),
        ExerciseDataItem(name = "Cobra Push-Up"),
        ExerciseDataItem(name = "Decline Push-Up"),
        ExerciseDataItem(name = "Dumbbell Chest Fly"),
        ExerciseDataItem(name = "Dumbbell Chest Press"),
        ExerciseDataItem(name = "Dumbbell Decline Chest Press"),
        ExerciseDataItem(name = "Dumbbell Floor Press"),
        ExerciseDataItem(name = "Dumbbell Pullover"),
        ExerciseDataItem(name = "Feet-Up Bench Press"),
        ExerciseDataItem(name = "Floor Press")
    )

    val shoulderExercises = listOf(
        ExerciseDataItem(name = "Arnold Press"),
        ExerciseDataItem(name = "Band External Shoulder Rotation", isTimeBased = true),
        ExerciseDataItem(name = "Band Internal Shoulder Rotation", isTimeBased = true),
        ExerciseDataItem(name = "Band Pull-Apart"),
        ExerciseDataItem(name = "Banded Face Pull"),
        ExerciseDataItem(name = "Barbell Front Raise"),
        ExerciseDataItem(name = "Barbell Rear Delt Row"),
        ExerciseDataItem(name = "Barbell Upright Row"),
        ExerciseDataItem(name = "Behind the Neck Press"),
        ExerciseDataItem(name = "Cable Internal Shoulder Rotation"),
        ExerciseDataItem(name = "Cable External Shoulder Rotation"),
        ExerciseDataItem(name = "Cable Front Raise"),
        ExerciseDataItem(name = "Cable Lateral Raise"),
        ExerciseDataItem(name = "Cable Rear Delt Row"),
        ExerciseDataItem(name = "Cuban Press"),
        ExerciseDataItem(name = "Devils Press"),
        ExerciseDataItem(name = "Dumbbell Front Raise"),
        ExerciseDataItem(name = "Dumbbell Horizontal Internal Shoulder Rotation"),
        ExerciseDataItem(name = "Dumbbell Horizontal External Shoulder Rotation"),
        ExerciseDataItem(name = "Dumbbell Lateral Raise")
    )

    val exercises = listOf(
        *chestExercises.toTypedArray(),
        *shoulderExercises.toTypedArray()
    ).mapIndexed { index, item -> item.copy(id = (index + 1).toLong()) }

//    val categories = listOf(
//        Category("Chest Exercises", chestExercises),
//        Category("Shoulder Exercises", shoulderExercises)
//
//    )

}