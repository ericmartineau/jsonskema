package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean

class BooleanKeywordDigester(keyword: KeywordInfo<BooleanKeyword>) : BaseKeywordDigester<BooleanKeyword>(keyword) {

  override fun extractKeyword(jsonElement: JsonElement): BooleanKeyword {
    return BooleanKeyword(jsonElement.boolean)
  }
}
