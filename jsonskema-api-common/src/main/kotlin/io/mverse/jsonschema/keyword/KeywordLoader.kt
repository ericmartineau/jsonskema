package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.JsonValueWithPath

/**
 * Functional interface for loading keywords (Functional interface).
 *
 * This class assumes that someone else has already determined the correct key and validated that the incoming type
 * is one of the accepted types.
 */
interface KeywordLoader<K : Keyword<*>> {
  fun loadKeyword(jsonValue: JsonValueWithPath): K?
}
