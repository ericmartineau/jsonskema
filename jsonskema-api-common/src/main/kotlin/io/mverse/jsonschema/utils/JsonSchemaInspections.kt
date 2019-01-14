package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.ElementType
import lang.collection.runLengths
import lang.json.JsrType
import lang.json.type
import lang.json.values

fun Schema.calculateType(): JsonSchemaType? {
  val schema = this.asDraft6()
  if (schema.types.isNotEmpty()) {
    return schema.types.first()
  }

  val fromEnumValue = schema.enumValues?.let { array ->
    val counts = array.values
        .map { it.type.toJsonSchemaType() }
        .distinct()
        .toList()
    return@let when (counts.size) {
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

internal fun JsrType.toJsonSchemaType(): JsonSchemaType =
    when (this) {
      JsrType.ARRAY -> JsonSchemaType.ARRAY
      JsrType.OBJECT -> JsonSchemaType.OBJECT
      JsrType.STRING -> JsonSchemaType.STRING
      JsrType.NUMBER -> JsonSchemaType.NUMBER
      JsrType.BOOLEAN -> JsonSchemaType.BOOLEAN
      JsrType.NULL -> JsonSchemaType.NULL
    }

