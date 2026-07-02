package com.priyanshparekh.repbook.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_schedule",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workout_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workout_id")]
)
data class WorkoutScheduleEntity(
    @PrimaryKey val day: Int,
    @ColumnInfo(name = "workout_id") val workoutId: Long
)
