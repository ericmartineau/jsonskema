package io.mverse.jsonschema.validation.factory

import io.mverse.jsonschema.keyword.KeywordInfo
import lang.collection.SetMultimap

/**
 * Extracts any necessary validation keywords from a [io.mverse.jsonschema.Schema] instance.
 */

data class KeywordValidatorCreators(private val factories: SetMultimap<KeywordInfo<*>, KeywordValidatorCreator<*, *>>) {
  operator fun get(key: KeywordInfo<*>): Set<KeywordValidatorCreator<*, *>> = factories[key]
}
