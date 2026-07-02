package com.priyanshparekh.repbook.data.repository

import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetRepositoryTest {

    private val fakeDao = FakeSetDao()
    private val repository = SetRepository(fakeDao)

    @Test
    fun insertDefaultSets_createsThreeSetsWithSetNumbers1To3() = runTest {
        repository.insertDefaultSets(exerciseId = 1L)
        assertEquals(3, fakeDao.insertedSets.size)
        assertEquals(1, fakeDao.insertedSets[0].setNo)
        assertEquals(2, fakeDao.insertedSets[1].setNo)
        assertEquals(3, fakeDao.insertedSets[2].setNo)
    }

    @Test
    fun insertDefaultSets_allSetsHaveCorrectExerciseId() = runTest {
        repository.insertDefaultSets(exerciseId = 42L)
        fakeDao.insertedSets.forEach { assertEquals(42L, it.exerciseId) }
    }

    @Test
    fun insertDefaultSets_allSetsHaveZeroWeightAndReps() = runTest {
        repository.insertDefaultSets(exerciseId = 1L)
        fakeDao.insertedSets.forEach {
            assertEquals(0f, it.weight)
            assertEquals(0, it.reps)
        }
    }

    @Test
    fun insertDefaultSets_customCount_createsCorrectNumberOfSets() = runTest {
        repository.insertDefaultSets(exerciseId = 1L, count = 5)
        assertEquals(5, fakeDao.insertedSets.size)
        assertEquals(listOf(1, 2, 3, 4, 5), fakeDao.insertedSets.map { it.setNo })
    }
}

private class FakeSetDao : SetDao {
    val insertedSets = mutableListOf<SetEntity>()

    override suspend fun insert(set: SetEntity): Long {
        insertedSets.add(set)
        return insertedSets.size.toLong()
    }

    override suspend fun insertAll(sets: List<SetEntity>) {
        insertedSets.addAll(sets)
    }

    override suspend fun update(set: SetEntity) {
        val index = insertedSets.indexOfFirst { it.id == set.id }
        if (index != -1) insertedSets[index] = set
    }

    override suspend fun delete(set: SetEntity) {
        insertedSets.removeIf { it.id == set.id }
    }

    override fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntity>> =
        flowOf(insertedSets.filter { it.exerciseId == exerciseId })

    override suspend fun getSetsForExerciseOnce(exerciseId: Long): List<SetEntity> =
        insertedSets.filter { it.exerciseId == exerciseId }

    override suspend fun getAllSets(): List<SetEntity> = insertedSets.toList()
}
