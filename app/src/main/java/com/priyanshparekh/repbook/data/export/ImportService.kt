package com.priyanshparekh.repbook.data.export

import androidx.room.withTransaction
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity

class ImportService(private val db: RepBookDatabase) {

    suspend fun import(data: ExportData) {
        db.withTransaction {
            db.workoutScheduleDao().deleteAll()
            db.workoutDao().deleteAll()

            data.workouts.forEach { dto ->
                db.workoutDao().insert(WorkoutEntity(id = dto.id, name = dto.name))
            }
            data.exercises.forEach { dto ->
                db.exerciseDao().insert(ExerciseEntity(id = dto.id, workoutId = dto.workoutId, name = dto.name))
            }
            data.sets.forEach { dto ->
                db.setDao().insert(SetEntity(id = dto.id, exerciseId = dto.exerciseId, setNo = dto.setNo, weight = dto.weight, reps = dto.reps))
            }
            data.schedule.forEach { dto ->
                db.workoutScheduleDao().upsert(WorkoutScheduleEntity(day = dto.day, workoutId = dto.workoutId))
            }
        }
    }
}
