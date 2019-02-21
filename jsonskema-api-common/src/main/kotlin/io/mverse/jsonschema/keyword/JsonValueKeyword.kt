package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath
import lang.json.JsrValue
import lang.json.toJsrValue

data class JsonValueKeyword(override val value: JsrValue) : KeywordImpl<JsrValue>() {
  constructor(any: Any?): this(toJsrValue(any))
  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<JsrValue>, report: MergeReport): Keyword<JsrValue> {
    report += mergeConflict(path, keyword, this, other)
    return other as JsonValueKeyword
  }

  override fun withValue(value: JsrValue): Keyword<JsrValue> = this.copy(value = value)
}
