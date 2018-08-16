package io.mverse.jsonschema.loading.keyword.versions

import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.keyword.BaseKeywordDigester
import kotlinx.serialization.json.JsonElement
import lang.URI

class IdKeywordDigester(keyword: KeywordInfo<IdKeyword>) : BaseKeywordDigester<IdKeyword>(keyword) {
  override fun extractKeyword(jsonElement: JsonElement): IdKeyword {
    return IdKeyword(URI(jsonElement.primitive.content))
  }
}
