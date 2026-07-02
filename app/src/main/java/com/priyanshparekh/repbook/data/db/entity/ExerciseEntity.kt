package com.priyanshparekh.repbook.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workout_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workout_id")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    val name: String,
    @ColumnInfo(name = "is_time_based") val isTimeBased: Boolean = false
)
