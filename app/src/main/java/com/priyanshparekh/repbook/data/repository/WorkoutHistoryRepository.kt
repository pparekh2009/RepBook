package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.WorkoutHistoryDao
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import com.priyanshparekh.repbook.data.mapper.toDomain
import com.priyanshparekh.repbook.domain.model.WorkoutHistoryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkoutHistoryRepository(
    private val dao: WorkoutHistoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun getAll(): Flow<List<WorkoutHistoryEntry>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun insert(entry: WorkoutHistoryEntry) = withContext(ioDispatcher) {
        dao.insert(
            WorkoutHistoryEntity(
                workoutId = entry.workoutId,
                workoutName = entry.workoutName,
                completedAt = entry.completedAt,
                durationSeconds = entry.durationSeconds,
                totalVolume = entry.totalVolume
            )
        )
    }
}
