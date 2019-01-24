package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath

data class StringKeyword(override val value: String) : KeywordImpl<String>() {
  override fun withValue(value: String): Keyword<String> = this.copy(value = value)

  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<String>, report: MergeReport): Keyword<String> {
    report += mergeConflict(path, keyword, this, other)
    return StringKeyword(other.value)
  }
}
