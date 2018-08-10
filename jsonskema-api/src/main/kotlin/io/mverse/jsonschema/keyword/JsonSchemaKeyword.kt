package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder

/**
 * Stores the values of a keyword (or keywords), and can serialize those values to json.
 */
interface JsonSchemaKeyword<P> {
  val value: P?
  fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion)
}
