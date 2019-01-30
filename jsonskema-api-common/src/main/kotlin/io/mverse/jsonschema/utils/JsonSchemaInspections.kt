package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import lang.collection.runLengths
import lang.exception.illegalState
import lang.exception.nullPointer
import lang.json.JsrArray
import lang.json.JsrType
import lang.json.type
import lang.json.values
import lang.net.URI
import lang.net.authority
import lang.net.compareURI
import lang.net.mutate
import lang.net.path
import lang.string.join

/**
 * Given two schema URIs, attempts to build a calculated merge URI for the two.
 */
fun URI.calculateMergeURI(override: URI?): URI {
  if (override == null) return this
  val diffURI = this.compareURI(override)
  if (diffURI.authority != null) {
    illegalState("Unable to automatically calculate merge schema URI.  The schemas must " +
        "contain the same base URI")
  }
  val path = diffURI.path ?: nullPointer("Invalid null path.  Either the target schema has no " +
      "path, or the schemas have identical paths")
  val qualifier = path.removeSuffix(".json").replace('/', '-')
  if (qualifier.isEmpty()) {
    illegalState("The merge source and target have the same base URI")
  }
  return this.mutate {
    val segments = this.path.orEmpty().split('/').toMutableList()
    val lastSegment = segments.last { it.isNotEmpty() }
    val replaced = if (lastSegment.endsWith(".json")) {
      lastSegment.removeSuffix(".json") + "-$qualifier.json"
    } else {
      "$lastSegment-$qualifier"
    }
    segments[segments.lastIndex] = replaced
    this.path = segments.join("/")
  }
}

fun Schema.calculateJsonSchemaType(): JsonSchemaType? {
  val schema = this.draft7()
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

