package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.mapper.toDomain
import com.priyanshparekh.repbook.data.mapper.toEntity
import com.priyanshparekh.repbook.domain.model.WorkoutSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SetRepository(
    private val setDao: SetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getSetsForExercise(exerciseId: Long): Flow<List<WorkoutSet>> =
        setDao.getSetsForExercise(exerciseId).map { list -> list.map { it.toDomain() } }

    suspend fun getSetsForExerciseOnce(exerciseId: Long): List<WorkoutSet> = withContext(ioDispatcher) {
        setDao.getSetsForExerciseOnce(exerciseId).map { it.toDomain() }
    }

    suspend fun insert(set: WorkoutSet): Long = withContext(ioDispatcher) {
        setDao.insert(set.toEntity())
    }

    suspend fun insertAll(sets: List<WorkoutSet>) = withContext(ioDispatcher) {
        setDao.insertAll(sets.map { it.toEntity() })
    }

    suspend fun update(set: WorkoutSet) = withContext(ioDispatcher) {
        setDao.update(set.toEntity())
    }

    suspend fun delete(set: WorkoutSet) = withContext(ioDispatcher) {
        setDao.delete(set.toEntity())
    }

    suspend fun getAllSets(): List<WorkoutSet> = withContext(ioDispatcher) {
        setDao.getAllSets().map { it.toDomain() }
    }

    suspend fun insertDefaultSets(
        exerciseId: Long,
        count: Int = 3,
        durationSeconds: Int? = null
    ) = withContext(ioDispatcher) {
        val sets = (1..count).map { i ->
            SetEntity(exerciseId = exerciseId, setNo = i, weight = 0f, reps = 0, durationSeconds = durationSeconds)
        }
        setDao.insertAll(sets)
    }
}
