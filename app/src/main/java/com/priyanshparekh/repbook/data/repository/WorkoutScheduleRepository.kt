package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.WorkoutScheduleDao
import com.priyanshparekh.repbook.data.mapper.toDomain
import com.priyanshparekh.repbook.data.mapper.toEntity
import com.priyanshparekh.repbook.domain.model.WorkoutSchedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkoutScheduleRepository(
    private val scheduleDao: WorkoutScheduleDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getScheduleForDays(days: List<Int>): Flow<List<WorkoutSchedule>> =
        scheduleDao.getScheduleForDays(days).map { list -> list.map { it.toDomain() } }

    fun getScheduleForDay(day: Int): Flow<WorkoutSchedule?> =
        scheduleDao.getScheduleForDay(day).map { it?.toDomain() }

    suspend fun upsert(schedule: WorkoutSchedule) = withContext(ioDispatcher) {
        scheduleDao.upsert(schedule.toEntity())
    }

    suspend fun delete(schedule: WorkoutSchedule) = withContext(ioDispatcher) {
        scheduleDao.delete(schedule.toEntity())
    }

    suspend fun getAllSchedules(): List<WorkoutSchedule> = withContext(ioDispatcher) {
        scheduleDao.getAllSchedules().map { it.toDomain() }
    }
}
