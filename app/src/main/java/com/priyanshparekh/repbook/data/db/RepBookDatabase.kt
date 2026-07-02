package com.priyanshparekh.repbook.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutHistoryDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutScheduleDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity

@Database(
    entities = [WorkoutEntity::class, ExerciseEntity::class, SetEntity::class, WorkoutScheduleEntity::class, WorkoutHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class RepBookDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun workoutScheduleDao(): WorkoutScheduleDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RepBookDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN is_time_based INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sets ADD COLUMN duration_seconds INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS workout_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workout_id INTEGER NOT NULL,
                        workout_name TEXT NOT NULL,
                        completed_at INTEGER NOT NULL,
                        duration_seconds INTEGER NOT NULL,
                        total_volume REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): RepBookDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RepBookDatabase::class.java,
                    "repbook_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
            }
    }
}
