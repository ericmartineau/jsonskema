package io.mverse.jsonschema.keyword

data class NumberKeyword(val number: Number) : JsonSchemaKeywordImpl<Number>(number.toDouble()) {
  val double: Double
    get() = number.toDouble()

  val integer: Int
    get() = number.toInt()
}
