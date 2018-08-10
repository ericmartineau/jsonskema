package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.ElementType

class JsonValueKeywordDigester(keyword: KeywordInfo<JsonValueKeyword>, vararg acceptedTypes: ElementType)
  : BaseKeywordDigester<JsonValueKeyword>(keyword, *acceptedTypes) {

  override fun extractKeyword(jsonElement: JsonElement): JsonValueKeyword {
    return JsonValueKeyword(jsonElement)
  }
}
