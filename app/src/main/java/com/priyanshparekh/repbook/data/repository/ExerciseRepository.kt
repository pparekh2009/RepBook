package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.mapper.toDomain
import com.priyanshparekh.repbook.data.mapper.toEntity
import com.priyanshparekh.repbook.domain.model.Exercise
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> =
        exerciseDao.getExercisesForWorkout(workoutId).map { list -> list.map { it.toDomain() } }

    fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAllExercises().map { list -> list.map { it.toDomain() } }

    suspend fun insert(exercise: Exercise): Long = withContext(ioDispatcher) {
        exerciseDao.insert(exercise.toEntity())
    }

    suspend fun update(exercise: Exercise) = withContext(ioDispatcher) {
        exerciseDao.update(exercise.toEntity())
    }

    suspend fun delete(exercise: Exercise) = withContext(ioDispatcher) {
        exerciseDao.delete(exercise.toEntity())
    }

    suspend fun getById(id: Long): Exercise? = withContext(ioDispatcher) {
        exerciseDao.getById(id)?.toDomain()
    }
}
