package io.mverse.jsonschema.loading.keyword.versions

import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.keyword.BaseKeywordDigester
import lang.json.JsrValue
import lang.json.unbox
import lang.net.URI

class IdKeywordDigester(keyword: KeywordInfo<IdKeyword>) : BaseKeywordDigester<IdKeyword>(keyword) {
  override fun extractKeyword(jsonValue: JsrValue): IdKeyword {
    return IdKeyword(URI(jsonValue.unbox()))
  }
}
