package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import lang.exception.illegalState
import lang.isIntegral
import lang.json.MutableJsrObject
import lang.json.jsrObject

data class LimitKeyword(val keyword: KeywordInfo<LimitKeyword>,
                        val exclusiveKeyword: KeywordInfo<LimitKeyword>,
                        val limit: Number? = null,
                        val exclusiveLimit: Number? = null) : Keyword<Number?> {

  override fun withValue(value: Number?): Keyword<Number?> {
    return this.copy(limit = value)
  }

  override val value: Number? = limit

  val isExclusive: Boolean = exclusiveLimit != null

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    when {
      version.isBefore(Draft6) -> writeDraft3And4(builder)
      version.isPublic -> writeDraft6AndUp(builder)
      else -> error("Unknown output type: $version")
    }
  }

  override fun toString(): String {
    val self = this
    return jsrObject {
      self.toJson(keyword, this, JsonSchemaVersion.latest, false)
    }.toString()
  }

  protected fun writeDraft6AndUp(builder: MutableJsrObject) {
    builder.run {
      if (limit != null) {
        keyword.key *= getWithPrecision(limit)
      }

      if (exclusiveLimit != null) {
        exclusiveKeyword.key *= getWithPrecision(exclusiveLimit)
      }
    }
  }

  private fun writeDraft3And4(builder: MutableJsrObject) {
    if (limit != null && exclusiveLimit != null) {
      illegalState("Draft schema version does not support number values for ${keyword.key} " +
          "and ${exclusiveKeyword.key}")
    }
    builder.apply {
      if (exclusiveLimit != null) {
        keyword.key *= getWithPrecision(exclusiveLimit)
        exclusiveKeyword.key *= true
      }

      if (limit != null) {
        keyword.key *= getWithPrecision(limit)
      }
    }
  }

  companion object {

    fun minimumKeyword(): LimitKeyword {
      return LimitKeyword(Keywords.MINIMUM, Keywords.EXCLUSIVE_MINIMUM, null, null)
    }

    fun maximumKeyword(): LimitKeyword {
      return LimitKeyword(Keywords.MAXIMUM, Keywords.EXCLUSIVE_MAXIMUM, null, null)
    }

    private fun getWithPrecision(input: Number): Number {
      return if (input.isIntegral()) {
        input.toInt()
      } else {
        input.toDouble()
      }
    }
  }
}
