package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workout_history ORDER BY completed_at DESC")
    fun getAll(): Flow<List<WorkoutHistoryEntity>>

    @Insert
    suspend fun insert(entry: WorkoutHistoryEntity)
}
