package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.mergeConflict
import io.mverse.jsonschema.mergeException
import lang.json.JsonPath
import lang.json.JsrArray

data class JsonArrayKeyword(override val value: JsrArray) : KeywordImpl<JsrArray>() {
  override fun withValue(value: JsrArray): Keyword<JsrArray> = this.copy(value = value)
  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<JsrArray>, report: MergeReport): Keyword<JsrArray> {
    report += mergeConflict(path, keyword, this, other)
    return JsonArrayKeyword(other.value)
  }
}
