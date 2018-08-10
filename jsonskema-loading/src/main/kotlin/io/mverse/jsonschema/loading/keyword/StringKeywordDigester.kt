package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.StringKeyword
import kotlinx.serialization.json.JsonElement

class StringKeywordDigester(keyword: KeywordInfo<StringKeyword>) : BaseKeywordDigester<StringKeyword>(keyword) {

  override fun extractKeyword(jsonElement: JsonElement): StringKeyword {
    return StringKeyword(jsonElement.primitive.content)
  }
}
