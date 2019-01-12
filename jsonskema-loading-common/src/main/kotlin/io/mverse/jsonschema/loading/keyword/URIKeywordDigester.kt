package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.URIKeyword
import kotlinx.serialization.json.JsonElement
import lang.net.URI

class URIKeywordDigester(keyword: KeywordInfo<URIKeyword>) : BaseKeywordDigester<URIKeyword>(keyword) {
  override fun extractKeyword(jsonElement: JsonElement): URIKeyword {
    val uriString = jsonElement.primitive.content
    return URIKeyword(URI(uriString))
  }
}
