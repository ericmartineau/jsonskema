package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import lang.convert
import lang.illegalState
import lang.json.ValueType
import lang.runLengths

object JsonSchemaInspections {

  /**
   * Examines a schema to determine what type of data it represents.  It takes the following
   * approaches:
   *
   * 1. Looks at the "type" keyword for an unambiguous answer 2. If the "enum" keyword is present,
   * checks to see if all enum values match one type 3. Examines all keywords to see if one type is
   * preferred based on keywords.
   *
   * @param input The schema to check
   *
   * @return The type of schema, or NULL if no determination could be made
   */
  fun getType(input: Schema): JsonSchemaType? {
    val schema = input.asDraft6()
    if (schema.types.isNotEmpty()) {
      return schema.types.first()
    }

    val fromEnumValue = schema.enumValues.convert { array ->
      val counts = array
          .map { it.valueType.toJsonSchemaType() }
          .distinct()
          .toList()
      return@convert when (counts.size) {
        1 -> counts.first()
        else -> null
      }
    }

    return when (fromEnumValue) {
      null -> {
        val highestCounts = schema.keywords.keys
            .flatMap { it.applicableTypes }
            .map { it.toJsonSchemaType() }
            .sorted()
            .runLengths()
            .sortedByDescending { it.second }

        val distinct = highestCounts
            .map { it.second }
            .distinct()
            .count()

        return when {
          highestCounts.size == 1 -> highestCounts.first().first
          highestCounts.size > 1 && distinct > 1 -> highestCounts.first().first
          else -> JsonSchemaType.NULL
        }
      }
      else -> fromEnumValue
    }
  }
}

internal fun ValueType.toJsonSchemaType(): JsonSchemaType {
  return when (this) {
    ValueType.NULL -> JsonSchemaType.NULL
    ValueType.STRING -> JsonSchemaType.STRING
    ValueType.NUMBER -> JsonSchemaType.NUMBER
    ValueType.INTEGER -> JsonSchemaType.INTEGER
    ValueType.OBJECT -> JsonSchemaType.OBJECT
    ValueType.ARRAY -> JsonSchemaType.ARRAY
    ValueType.BOOLEAN -> JsonSchemaType.BOOLEAN
  }
}

val JsonElement.valueType: ValueType
  get() {
    return when {
      this.isNull -> ValueType.NULL
      this is JsonObject -> ValueType.OBJECT
      this is JsonArray -> ValueType.ARRAY
      this is JsonLiteral -> when {
        this.booleanOrNull != null -> ValueType.BOOLEAN
        this.longOrNull != null -> ValueType.INTEGER
        this.doubleOrNull != null -> ValueType.NUMBER
        else -> ValueType.STRING
      }
      else -> illegalState("Unknown json type")
    }
  }
