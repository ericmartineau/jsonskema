package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.mergeException
import lang.json.JsonPath

data class BooleanKeyword(override val value: Boolean) : KeywordImpl<Boolean>() {

  override fun withValue(value: Boolean): Keyword<Boolean> {
    return this.copy(value = value)
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Boolean>, report: MergeReport): Keyword<Boolean> = mergeException()
}
