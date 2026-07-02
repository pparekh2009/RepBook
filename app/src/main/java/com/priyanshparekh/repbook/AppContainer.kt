package com.priyanshparekh.repbook

import android.content.Context
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.export.ExportSerializer
import com.priyanshparekh.repbook.data.export.ImportService
import com.priyanshparekh.repbook.data.export.ImportValidator
import com.priyanshparekh.repbook.data.preferences.AppPreferencesDataStore
import com.priyanshparekh.repbook.data.preferences.WorkoutCompletionRepository
import com.priyanshparekh.repbook.data.preferences.dataStore
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutHistoryRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.repository.WorkoutScheduleRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository

class AppContainer(context: Context) {
    private val db = RepBookDatabase.getInstance(context)

    val workoutRepository = WorkoutRepository(db.workoutDao())
    val exerciseRepository = ExerciseRepository(db.exerciseDao())
    val setRepository = SetRepository(db.setDao())
    val scheduleRepository = WorkoutScheduleRepository(db.workoutScheduleDao())
    val historyRepository = WorkoutHistoryRepository(db.workoutHistoryDao())

    val preferencesDataStore = AppPreferencesDataStore(context.dataStore)
    val completionRepository = WorkoutCompletionRepository(context.dataStore)
    val sessionRepository = WorkoutSessionRepository()

    val exportSerializer = ExportSerializer(workoutRepository, exerciseRepository, setRepository, scheduleRepository)
    val importValidator = ImportValidator()
    val importService = ImportService(db)
}
