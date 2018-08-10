package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.ElementType
import lang.convert
import lang.runLengths

object JsonSchemaInspections {

}

fun Schema.calculateType(): JsonSchemaType? {
  val schema = this.asDraft6()
  if (schema.types.isNotEmpty()) {
    return schema.types.first()
  }

  val fromEnumValue = schema.enumValues.convert { array ->
    val counts = array
        .map { it.type.toJsonSchemaType() }
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

internal fun ElementType.toJsonSchemaType(): JsonSchemaType {
  return when (this) {
    ElementType.NULL -> JsonSchemaType.NULL
    ElementType.STRING -> JsonSchemaType.STRING
    ElementType.NUMBER -> JsonSchemaType.NUMBER
    ElementType.OBJECT -> JsonSchemaType.OBJECT
    ElementType.ARRAY -> JsonSchemaType.ARRAY
    ElementType.BOOLEAN -> JsonSchemaType.BOOLEAN
  }
}

