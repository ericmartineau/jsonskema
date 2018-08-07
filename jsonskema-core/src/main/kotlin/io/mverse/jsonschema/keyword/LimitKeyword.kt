package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.json
import lang.illegalState
import lang.isIntegral

data class LimitKeyword(val keyword: KeywordInfo<LimitKeyword>,
                      val exclusiveKeyword: KeywordInfo<LimitKeyword>,
                      val limit: Number?,
                      val exclusive: Number?,
                      val isExclusive: Boolean = exclusive != null) : JsonSchemaKeyword<Number> {

  override val value: Number? = limit

  private val exclusiveLimit: Number?
  get() {
    return when {
      isExclusive && exclusive == null && limit != null-> limit.toDouble()
      else-> exclusive?.toDouble()
    }
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    if (version.isBefore(Draft6)) {
      writeDraft3And4(builder)
    } else if (version.isPublic) {
      writeDraft6AndUp(builder)
    } else {
      throw IllegalArgumentException("Unknown output type: $version")
    }
  }

  override fun toString(): String {
    val self = this
    return json {
      self.toJson(keyword, this, JsonSchemaVersion.latest())
    }.toString()
  }

  protected fun writeDraft6AndUp(builder: JsonBuilder) {
    builder.run {
      if (limit != null) {
        keyword.key to getWithPrecision(limit)
      }

      if (exclusiveLimit != null) {
        exclusiveKeyword.key to getWithPrecision(exclusiveLimit!!)
      }
    }

  }

  private fun writeDraft3And4(builder: JsonBuilder) {
    if (limit != null && exclusive != null) {
      illegalState("Draft schema version does not support number values for ${keyword.key} " +
          "and ${exclusiveKeyword.key}")
    }
    builder.apply {
      when (isExclusive) {
        true-> {
          keyword.key to getWithPrecision(exclusiveLimit!!)
          exclusiveKeyword.key to true
        }
        false-> keyword.key to getWithPrecision(limit!!)
      }
    }
  }

  companion object {

    fun minimumKeyword(): LimitKeyword {
      return LimitKeyword(Keywords.MINIMUM, Keywords.EXCLUSIVE_MINIMUM, null, null, false)
    }

    fun maximumKeyword(): LimitKeyword {
      return LimitKeyword(Keywords.MAXIMUM, Keywords.EXCLUSIVE_MAXIMUM, null, null, false)
    }

//    fun builder(keyword: KeywordInfo<LimitKeyword>, exclusiveKeyword: KeywordInfo<LimitKeyword>): LimitKeywordBuilder {
//      return LimitKeywordBuilder().keyword(keyword).exclusiveKeyword(exclusiveKeyword)
//    }

    private fun getWithPrecision(input: Number): Number {
      return if (input.isIntegral()) {
        input.toInt()
      } else {
        input.toDouble()
      }
    }
  }
}
