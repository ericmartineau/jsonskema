package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import kotlinx.serialization.json.json
import lang.illegalState
import lang.isIntegral

data class LimitKeyword(val keyword: KeywordInfo<LimitKeyword>,
                      val exclusiveKeyword: KeywordInfo<LimitKeyword>,
                      val limit: Number? = null,
                      val exclusiveLimit: Number? = null) : JsonSchemaKeyword<Number?> {

  override fun copy(value: Number?): JsonSchemaKeyword<Number?> {
    return this.copy(limit = value)
  }

  override val value: Number? = limit

  val isExclusive:Boolean = exclusiveLimit != null

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    when {
      version.isBefore(Draft6) -> writeDraft3And4(builder)
      version.isPublic -> writeDraft6AndUp(builder)
      else -> error("Unknown output type: $version")
    }
  }

  override fun toString(): String {
    val self = this
    return json {
      self.toJson(keyword, this, JsonSchemaVersion.latest())
    }.toString()
  }

  protected fun writeDraft6AndUp(builder: kotlinx.serialization.json.JsonBuilder) {
    builder.run {
      if (limit != null) {
        keyword.key to getWithPrecision(limit)
      }

      if (exclusiveLimit != null) {
        exclusiveKeyword.key to getWithPrecision(exclusiveLimit)
      }
    }
  }

  private fun writeDraft3And4(builder: kotlinx.serialization.json.JsonBuilder) {
    if (limit != null && exclusiveLimit != null) {
      illegalState("Draft schema version does not support number values for ${keyword.key} " +
          "and ${exclusiveKeyword.key}")
    }
    builder.apply {
      if (exclusiveLimit != null) {
        keyword.key to getWithPrecision(exclusiveLimit)
        exclusiveKeyword.key to true
      }

      if (limit != null) {
        keyword.key to getWithPrecision(limit)
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
