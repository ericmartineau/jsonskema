package io.mverse.jsonschema.builder

import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.LoadingReport

interface KeywordBuilder<K : Keyword<*>> {
  fun build(parentLocation: SchemaLocation, keyword: KeywordInfo<K>, report: LoadingReport): K
}
