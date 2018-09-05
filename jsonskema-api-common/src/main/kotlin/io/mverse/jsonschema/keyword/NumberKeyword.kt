package io.mverse.jsonschema.keyword

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
}
