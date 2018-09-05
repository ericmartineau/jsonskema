package io.mverse.jsonschema.validation.factory

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.keywords.KeywordValidator

/**
 * Extracts any necessary validation keywords from a [Schema] instance.
 * Extracts any necessary keyword validators by inspecting the provided schema.
 */
class KeywordValidatorCreator<K : Keyword<*>, V : KeywordValidator<K>>(val block:(K, Schema, SchemaValidatorFactory)->V?) {
  operator fun invoke(p1: K, p2: Schema, p3: SchemaValidatorFactory): V? {
    return block(p1, p2, p3)
  }

  fun invokeUnsafe(keyword:Keyword<*>, schema:Schema, factory:SchemaValidatorFactory): KeywordValidator<*>? {
    @Suppress("UNCHECKED_CAST")
    return block(keyword as K, schema, factory)
  }
}
