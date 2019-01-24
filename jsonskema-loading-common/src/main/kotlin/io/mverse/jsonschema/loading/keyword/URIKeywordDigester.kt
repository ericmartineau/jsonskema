package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.URIKeyword
import lang.json.JsrValue
import lang.json.unbox
import lang.net.URI

class URIKeywordDigester(keyword: KeywordInfo<URIKeyword>) : BaseKeywordDigester<URIKeyword>(keyword) {
  override fun extractKeyword(jsonValue: JsrValue): URIKeyword {
    val uriString:String = jsonValue.unbox()
    return URIKeyword(URI(uriString))
  }
}
