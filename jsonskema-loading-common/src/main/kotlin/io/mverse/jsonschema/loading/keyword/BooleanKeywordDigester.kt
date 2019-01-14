package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.json.JsrValue
import lang.json.unbox

class BooleanKeywordDigester(keyword: KeywordInfo<BooleanKeyword>) : BaseKeywordDigester<BooleanKeyword>(keyword) {

  override fun extractKeyword(jsonValue: JsrValue): BooleanKeyword {
    return BooleanKeyword(jsonValue.unbox())
  }
}
