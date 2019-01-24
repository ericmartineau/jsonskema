package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.StringKeyword
import lang.json.JsrValue
import lang.json.unbox

class StringKeywordDigester(keyword: KeywordInfo<StringKeyword>) : BaseKeywordDigester<StringKeyword>(keyword) {

  override fun extractKeyword(jsonValue: JsrValue): StringKeyword {
    return StringKeyword(jsonValue.unbox())
  }
}
