package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<SetEntity>)

    @Update
    suspend fun update(set: SetEntity)

    @Delete
    suspend fun delete(set: SetEntity)

    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId ORDER BY set_no ASC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE exercise_id = :exerciseId ORDER BY set_no ASC")
    suspend fun getSetsForExerciseOnce(exerciseId: Long): List<SetEntity>

    @Query("SELECT * FROM sets ORDER BY exercise_id ASC, set_no ASC")
    suspend fun getAllSets(): List<SetEntity>
}
