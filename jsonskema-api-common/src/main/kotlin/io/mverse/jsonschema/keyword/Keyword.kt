package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.MutableJsrObject

/**
 * Stores the values of a keyword (or keywords), and can serialize those values to json.
 */
interface Keyword<P> {
  val value: P
  fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean)
  fun withValue(value: P): Keyword<P>
}
