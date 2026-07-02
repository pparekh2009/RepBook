package com.priyanshparekh.repbook.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportValidatorTest {

    private lateinit var validator: ImportValidator

    @Before
    fun setup() {
        validator = ImportValidator()
    }

    private fun validJson(
        appId: String = ExportSerializer.APP_ID,
        version: Int = ExportSerializer.VERSION,
        workouts: String = """[{"id":1,"name":"Push Day"}]""",
        exercises: String = """[{"id":10,"workoutId":1,"name":"Bench Press"}]""",
        sets: String = """[{"id":100,"exerciseId":10,"setNo":1,"weight":80.0,"reps":8}]""",
        schedule: String = """[{"day":1,"workoutId":1}]"""
    ): String = """
        {
          "appId": "$appId",
          "version": $version,
          "exportedAt": "2024-01-01T00:00:00Z",
          "workouts": $workouts,
          "exercises": $exercises,
          "sets": $sets,
          "schedule": $schedule
        }
    """.trimIndent()

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    fun validate_returnsValid_forWellFormedExport() {
        val result = validator.validate(validJson())
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun validate_returnsValid_withEmptyArrays() {
        val result = validator.validate(
            validJson(workouts = "[]", exercises = "[]", sets = "[]", schedule = "[]")
        )
        assertEquals(ValidationResult.Valid, result)
    }

    // ── JSON structure ─────────────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_forMalformedJson() {
        val result = validator.validate("not json {{{")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_forJsonArray() {
        val result = validator.validate("[1, 2, 3]")
        assertTrue(result is ValidationResult.Invalid)
    }

    // ── App identity ───────────────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_whenAppIdMissing() {
        val json = """{"version":1,"exportedAt":"2024-01-01T00:00:00Z","workouts":[],"exercises":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenAppIdWrong() {
        val result = validator.validate(validJson(appId = "com.other.app"))
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("appId"))
    }

    // ── Version ────────────────────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_whenVersionMissing() {
        val json = """{"appId":"com.priyanshparekh.repbook","exportedAt":"2024-01-01T00:00:00Z","workouts":[],"exercises":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenVersionWrong() {
        val result = validator.validate(validJson(version = 99))
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("version"))
    }

    @Test
    fun validate_returnsInvalid_whenVersionIsString() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":"1","exportedAt":"","workouts":[],"exercises":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    // ── Required array keys ────────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_whenWorkoutsMissing() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":1,"exportedAt":"","exercises":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenWorkoutsIsNotArray() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":1,"exportedAt":"","workouts":"bad","exercises":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenExercisesMissing() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":1,"exportedAt":"","workouts":[],"sets":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenSetsMissing() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":1,"exportedAt":"","workouts":[],"exercises":[],"schedule":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenScheduleMissing() {
        val json = """{"appId":"com.priyanshparekh.repbook","version":1,"exportedAt":"","workouts":[],"exercises":[],"sets":[]}"""
        assertTrue(validator.validate(json) is ValidationResult.Invalid)
    }

    // ── Field validation ───────────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_whenWorkoutMissingName() {
        val result = validator.validate(validJson(workouts = """[{"id":1}]"""))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenExerciseMissingWorkoutId() {
        val result = validator.validate(
            validJson(
                workouts = """[{"id":1,"name":"Push"}]""",
                exercises = """[{"id":10,"name":"Bench"}]"""
            )
        )
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenSetMissingWeight() {
        val result = validator.validate(
            validJson(sets = """[{"id":100,"exerciseId":10,"setNo":1,"reps":8}]""")
        )
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validate_returnsInvalid_whenScheduleMissingDay() {
        val result = validator.validate(validJson(schedule = """[{"workoutId":1}]"""))
        assertTrue(result is ValidationResult.Invalid)
    }

    // ── Referential integrity ──────────────────────────────────────────────────

    @Test
    fun validate_returnsInvalid_whenExerciseReferencesUnknownWorkout() {
        val result = validator.validate(
            validJson(
                workouts = """[{"id":1,"name":"Push"}]""",
                exercises = """[{"id":10,"workoutId":999,"name":"Bench"}]""",
                sets = "[]",
                schedule = "[]"
            )
        )
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("workoutId"))
    }

    @Test
    fun validate_returnsInvalid_whenSetReferencesUnknownExercise() {
        val result = validator.validate(
            validJson(
                workouts = """[{"id":1,"name":"Push"}]""",
                exercises = """[{"id":10,"workoutId":1,"name":"Bench"}]""",
                sets = """[{"id":100,"exerciseId":999,"setNo":1,"weight":80.0,"reps":8}]""",
                schedule = "[]"
            )
        )
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("exerciseId"))
    }
}
