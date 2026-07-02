package com.priyanshparekh.repbook.data.export

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

class ImportValidator {

    fun validate(jsonString: String): ValidationResult {
        val root = try {
            Json.parseToJsonElement(jsonString).jsonObject
        } catch (e: Exception) {
            return ValidationResult.Invalid("Not valid JSON")
        }

        val appId = root["appId"]?.jsonPrimitive?.content
            ?: return ValidationResult.Invalid("Missing field: appId")
        if (appId != ExportSerializer.APP_ID) {
            return ValidationResult.Invalid("Unknown appId: $appId")
        }

        val version = root["version"]?.jsonPrimitive?.intOrNull
            ?: return ValidationResult.Invalid("Missing or non-integer field: version")
        if (version != ExportSerializer.VERSION) {
            return ValidationResult.Invalid("Unsupported version: $version")
        }

        val workoutsArray = root["workouts"] as? JsonArray
            ?: return ValidationResult.Invalid("Missing or non-array field: workouts")
        val exercisesArray = root["exercises"] as? JsonArray
            ?: return ValidationResult.Invalid("Missing or non-array field: exercises")
        val setsArray = root["sets"] as? JsonArray
            ?: return ValidationResult.Invalid("Missing or non-array field: sets")
        val scheduleArray = root["schedule"] as? JsonArray
            ?: return ValidationResult.Invalid("Missing or non-array field: schedule")

        for (element in workoutsArray) {
            val obj = element as? JsonObject
                ?: return ValidationResult.Invalid("Workout entry must be an object")
            obj["id"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Workout entry missing or invalid 'id'")
            obj["name"]?.jsonPrimitive?.content
                ?: return ValidationResult.Invalid("Workout entry missing 'name'")
        }

        for (element in exercisesArray) {
            val obj = element as? JsonObject
                ?: return ValidationResult.Invalid("Exercise entry must be an object")
            obj["id"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Exercise entry missing or invalid 'id'")
            obj["workoutId"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Exercise entry missing or invalid 'workoutId'")
            obj["name"]?.jsonPrimitive?.content
                ?: return ValidationResult.Invalid("Exercise entry missing 'name'")
        }

        for (element in setsArray) {
            val obj = element as? JsonObject
                ?: return ValidationResult.Invalid("Set entry must be an object")
            obj["id"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Set entry missing or invalid 'id'")
            obj["exerciseId"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Set entry missing or invalid 'exerciseId'")
            obj["setNo"]?.jsonPrimitive?.intOrNull
                ?: return ValidationResult.Invalid("Set entry missing or invalid 'setNo'")
            obj["weight"]?.jsonPrimitive?.floatOrNull
                ?: return ValidationResult.Invalid("Set entry missing or invalid 'weight'")
            obj["reps"]?.jsonPrimitive?.intOrNull
                ?: return ValidationResult.Invalid("Set entry missing or invalid 'reps'")
        }

        for (element in scheduleArray) {
            val obj = element as? JsonObject
                ?: return ValidationResult.Invalid("Schedule entry must be an object")
            obj["day"]?.jsonPrimitive?.intOrNull
                ?: return ValidationResult.Invalid("Schedule entry missing or invalid 'day'")
            obj["workoutId"]?.jsonPrimitive?.longOrNull
                ?: return ValidationResult.Invalid("Schedule entry missing or invalid 'workoutId'")
        }

        val workoutIds = workoutsArray.mapNotNull {
            (it as? JsonObject)?.get("id")?.jsonPrimitive?.longOrNull
        }.toSet()
        for (element in exercisesArray) {
            val obj = element as JsonObject
            val refId = obj["workoutId"]!!.jsonPrimitive.longOrNull!!
            if (refId !in workoutIds) {
                return ValidationResult.Invalid("Exercise references unknown workoutId: $refId")
            }
        }

        val exerciseIds = exercisesArray.mapNotNull {
            (it as? JsonObject)?.get("id")?.jsonPrimitive?.longOrNull
        }.toSet()
        for (element in setsArray) {
            val obj = element as JsonObject
            val refId = obj["exerciseId"]!!.jsonPrimitive.longOrNull!!
            if (refId !in exerciseIds) {
                return ValidationResult.Invalid("Set references unknown exerciseId: $refId")
            }
        }

        return ValidationResult.Valid
    }
}
