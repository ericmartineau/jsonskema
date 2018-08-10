package io.mverse.jsonschema.loading.keyword.versions.flex

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader

/**
 * Class that handles extracting limit keywords (minimum,maximum,exclusiveMaximum,exclusiveMinimum) for all version
 * of the schema.  This class is complicated, but it saved a sort of class explosion for keywords/validators.
 */


data class LimitKeywordDigester(
    val keyword: KeywordInfo<LimitKeyword>,
    val exclusiveKeyword: KeywordInfo<LimitKeyword>,
    override val includedKeywords: List<KeywordInfo<LimitKeyword>> = listOf(keyword, exclusiveKeyword),
    val blankKeywordSupplier: ()->LimitKeyword) : KeywordDigester<LimitKeyword> {

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder<*>,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<LimitKeyword>? {

    val exclusiveValue = jsonObject.path(exclusiveKeyword.key)
    val limitValue = jsonObject.path(keyword.key)

    return KeywordDigest.ofNullable(keyword, LimitKeyword(keyword = keyword,
        exclusiveKeyword = exclusiveKeyword,
        limit = limitValue.number,
        exclusive = exclusiveValue.number))
  }

  class LimitKeywordDigesterBuilder

  companion object {

    private fun builder(): LimitKeywordDigesterBuilder {
      return LimitKeywordDigesterBuilder()
    }

    fun minimumExtractor(): LimitKeywordDigester {
      return LimitKeywordDigester(
          keyword = Keywords.MINIMUM,
          exclusiveKeyword = Keywords.EXCLUSIVE_MINIMUM,
          blankKeywordSupplier = { LimitKeyword.minimumKeyword() })
    }

    fun maximumExtractor(): LimitKeywordDigester {
      return LimitKeywordDigester(
          keyword = Keywords.MAXIMUM,
          exclusiveKeyword = Keywords.EXCLUSIVE_MAXIMUM,
          blankKeywordSupplier = { LimitKeyword.maximumKeyword() })
    }
  }
}
