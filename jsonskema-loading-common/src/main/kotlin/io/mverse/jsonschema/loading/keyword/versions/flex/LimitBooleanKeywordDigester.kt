package io.mverse.jsonschema.loading.keyword.versions.flex

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.EXCLUSIVE_MINIMUM
import io.mverse.jsonschema.keyword.Keywords.MINIMUM
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest

/**
 * Class that handles extracting limit keywords (minimum,maximum,exclusiveMaximum,exclusiveMinimum) for all version
 * of the schema.  This class is complicated, but it saved a sort of class explosion for keywords/validators.
 */
data class LimitBooleanKeywordDigester(
    val keyword: KeywordInfo<LimitKeyword>,
    val exclusiveKeyword: KeywordInfo<LimitKeyword>,
    val blankKeywordSupplier: () -> LimitKeyword,
    override val includedKeywords: List<KeywordInfo<LimitKeyword>> = listOf(keyword, exclusiveKeyword))
  : KeywordDigester<LimitKeyword> {

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<LimitKeyword>? {

    val exclusiveValue = jsonObject.path(exclusiveKeyword.key)
    val limitValue = jsonObject.path(keyword.key)

    val exclusiveLimit = when {
      limitValue.number != null && exclusiveValue.boolean ?: false -> limitValue.number
      else -> exclusiveValue.number
    }

    return keyword.digest(LimitKeyword(keyword, exclusiveKeyword, limitValue.number, exclusiveLimit))
  }

  class LimitBooleanKeywordDigesterBuilder

  companion object {
    private fun builder(): LimitBooleanKeywordDigesterBuilder {
      return LimitBooleanKeywordDigesterBuilder()
    }

    fun minimumExtractor(): LimitBooleanKeywordDigester {
      return LimitBooleanKeywordDigester(
          keyword = MINIMUM,
          exclusiveKeyword = EXCLUSIVE_MINIMUM,
          blankKeywordSupplier = LimitKeyword.Companion::minimumKeyword)
    }

    fun maximumExtractor(): LimitBooleanKeywordDigester {
      return LimitBooleanKeywordDigester(
          keyword = MINIMUM,
          exclusiveKeyword = EXCLUSIVE_MINIMUM,
          blankKeywordSupplier = LimitKeyword.Companion::maximumKeyword)
    }
  }
}
