package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath

data class NumberKeyword(override val value: Number) : KeywordImpl<Number>() {
  override fun withValue(value: Number): Keyword<Number> = this.copy(value = value)

  val double: Double
    get() = value.toDouble()

  val integer: Int
    get() = value.toInt()

  override fun equals(other: Any?): Boolean = when {
    this === other -> true
    other !is NumberKeyword -> false
    else -> double == other.double
  }

  override fun hashCode(): Int {
    return double.hashCode()
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Number>, report: MergeReport): Keyword<Number> {
    report += mergeConflict(path, keyword, this, other)
    return NumberKeyword(other.value)
  }
}
