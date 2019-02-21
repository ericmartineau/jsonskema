package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.mergeConflict
import io.mverse.jsonschema.mergeException
import lang.json.JsonPath
import lang.json.JsrArray
import lang.json.JsrValue
import lang.json.values

data class JsonArrayKeyword(override val value: JsrIterable) : KeywordImpl<JsrIterable>() {
  constructor(jsrArray:JsrArray): this(jsrArray.values)

  override fun withValue(value: JsrIterable): Keyword<JsrIterable> = this.copy(value = value)
  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<JsrIterable>, report: MergeReport): Keyword<JsrIterable> {
    report += mergeConflict(path, keyword, this, other)
    return JsonArrayKeyword(other.value)
  }
}


typealias JsrIterable = Iterable<JsrValue>

fun iterableOf(block: () -> JsrIterable): JsrIterable = object : JsrIterable {
  override fun iterator(): Iterator<JsrValue> = block().iterator()
}
