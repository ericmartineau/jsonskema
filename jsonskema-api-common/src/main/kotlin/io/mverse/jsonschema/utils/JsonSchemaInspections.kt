package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import lang.collection.runLengths
import lang.json.JsrArray
import lang.json.JsrType
import lang.json.type
import lang.json.values

fun Schema.calculateJsonSchemaType(): JsonSchemaType? {
  val schema = this.asDraft7()
  if (schema.types.isNotEmpty()) {
    return schema.types.first()
  }

  val fromEnumValue = schema.enumValues?.jsonSchemaType

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

val JsrArray.jsonSchemaType: JsonSchemaType?
  get() {
    val counts = this.values
        .map { it.type.toJsonSchemaType() }
        .distinct()
        .toList()
    return when (counts.size) {
      1 -> counts.first()
      else -> null
    }
  }

//internal fun ElementType.toJsonSchemaType(): JsonSchemaType {
//  return when (this) {
//    ElementType.NULL -> JsonSchemaType.NULL
//    ElementType.STRING -> JsonSchemaType.STRING
//    ElementType.NUMBER -> JsonSchemaType.NUMBER
//    ElementType.OBJECT -> JsonSchemaType.OBJECT
//    ElementType.ARRAY -> JsonSchemaType.ARRAY
//    ElementType.BOOLEAN -> JsonSchemaType.BOOLEAN
//  }
//}

internal fun JsrType.toJsonSchemaType(): JsonSchemaType =
    when (this) {
      JsrType.ARRAY -> JsonSchemaType.ARRAY
      JsrType.OBJECT -> JsonSchemaType.OBJECT
      JsrType.STRING -> JsonSchemaType.STRING
      JsrType.NUMBER -> JsonSchemaType.NUMBER
      JsrType.BOOLEAN -> JsonSchemaType.BOOLEAN
      JsrType.NULL -> JsonSchemaType.NULL
    }

