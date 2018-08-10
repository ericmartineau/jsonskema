package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.NumberKeyword
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.double

class NumberKeywordDigester(keyword: KeywordInfo<NumberKeyword>) : BaseKeywordDigester<NumberKeyword>(keyword) {

  override fun extractKeyword(jsonElement: JsonElement): NumberKeyword {
    return NumberKeyword(jsonElement.double)
  }
}
