package io.mverse.jsonschema.builder

import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.loading.LoadingReport

interface KeywordBuilder<K : JsonSchemaKeyword<*>> {
  fun build(parentLocation: SchemaLocation, keyword: KeywordInfo<K>, report: LoadingReport): K
}
