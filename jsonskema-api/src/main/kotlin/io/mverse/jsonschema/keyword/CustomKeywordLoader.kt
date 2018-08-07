package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.JsonValueWithPath

/**
 * This class can be used to quickly create a keyword loader that takes in a JsonValue and outputs the appropriate
 * keyword.  It assumes that someone else has already determined the correct key and validated that the incoming type
 * is one of the accepted types.
 */
interface CustomKeywordLoader<K : JsonSchemaKeyword<*>> {
  fun loadKeywordFromJsonValue(jsonValue: JsonValueWithPath): K?
}
