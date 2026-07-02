package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.mapper.toDomain
import com.priyanshparekh.repbook.data.mapper.toEntity
import com.priyanshparekh.repbook.domain.model.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getAllWorkouts(): Flow<List<Workout>> =
        workoutDao.getAllWorkouts().map { list -> list.map { it.toDomain() } }

    suspend fun insert(workout: Workout): Long = withContext(ioDispatcher) {
        workoutDao.insert(workout.toEntity())
    }

    suspend fun update(workout: Workout) = withContext(ioDispatcher) {
        workoutDao.update(workout.toEntity())
    }

    suspend fun delete(workout: Workout) = withContext(ioDispatcher) {
        workoutDao.delete(workout.toEntity())
    }

    suspend fun getById(id: Long): Workout? = withContext(ioDispatcher) {
        workoutDao.getById(id)?.toDomain()
    }
}
