package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: WorkoutScheduleEntity)

    @Delete
    suspend fun delete(schedule: WorkoutScheduleEntity)

    @Query("SELECT * FROM workout_schedule WHERE day IN (:days)")
    fun getScheduleForDays(days: List<Int>): Flow<List<WorkoutScheduleEntity>>

    @Query("SELECT * FROM workout_schedule WHERE day = :day")
    fun getScheduleForDay(day: Int): Flow<WorkoutScheduleEntity?>

    @Query("SELECT * FROM workout_schedule")
    suspend fun getAllSchedules(): List<WorkoutScheduleEntity>

    @Query("DELETE FROM workout_schedule")
    suspend fun deleteAll()
}
