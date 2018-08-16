package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.keywords.KeywordValidator

object NumberLimitValidators {

  fun getMaxValidator(keyword: LimitKeyword, schema: Schema): KeywordValidator<LimitKeyword>? {
    return when {
      keyword.isExclusive -> NumberExclusiveMaximumValidator(
          schema = schema,
          exclusiveMaximum = keyword.exclusiveLimit!!.toDouble())
      keyword.limit != null -> NumberMaximumValidator(
          schema = schema,
          maximum = keyword.limit!!.toDouble()
      )
      else -> null
    }
  }

  fun getMinValidator(keyword: LimitKeyword, schema: Schema): KeywordValidator<LimitKeyword>? {
    return when {
      keyword.isExclusive -> NumberExclusiveMinimumValidator(
          schema = schema,
          exclusiveMinimum = keyword.exclusiveLimit!!.toDouble())
      keyword.limit != null -> NumberMinimumValidator(
          schema = schema,
          minimum = keyword.limit!!.toDouble()
      )
      else -> null
    }
  }
}
