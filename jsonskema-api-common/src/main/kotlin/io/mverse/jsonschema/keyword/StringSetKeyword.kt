package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.mergeCombine
import lang.json.JsonPath

data class StringSetKeyword(override val value: Set<String> = setOf()) : KeywordImpl<Set<String>>() {
  constructor(value: String) : this(setOf(value))

  operator fun plus(other: String): StringSetKeyword {
    return StringSetKeyword(value + other)
  }

  fun withAnotherValue(anotherValue: String): StringSetKeyword {
    return this + anotherValue
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Set<String>>, report: MergeReport): Keyword<Set<String>> {
    if (this.value.isNotEmpty() && other.value.isNotEmpty() && this.value != other.value) {
      report += mergeCombine(path, keyword, this, other)
    }
    return StringSetKeyword(this.value + other.value)
  }

  override fun withValue(value: Set<String>): Keyword<Set<String>> = this.copy(value = value)
}
