package io.mverse.jsonschema.validation.keywords.string.formatValidators

import io.mverse.jsonschema.validation.FormatValidator
import lang.Pattern
import lang.format

class PatternBasedValidator(private val pattern: Pattern, private val format: String) : FormatValidator {

  override fun validate(subject: String): String? {
    return if (!pattern.find(subject)) {
      "[%s] is not a valid".format(format)
    } else null
  }

  override fun formatName(): String {
    return format
  }
}
