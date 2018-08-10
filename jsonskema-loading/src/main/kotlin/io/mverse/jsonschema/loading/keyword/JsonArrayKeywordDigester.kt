package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.serialization.json.JsonElement

class JsonArrayKeywordDigester(keyword: KeywordInfo<JsonArrayKeyword>) : BaseKeywordDigester<JsonArrayKeyword>(keyword) {

  override fun extractKeyword(jsonElement: JsonElement): JsonArrayKeyword {
    return JsonArrayKeyword(jsonElement.jsonArray)
  }
}
