package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.NumberKeyword
import lang.json.JsrValue
import lang.json.unbox

class NumberKeywordDigester(keyword: KeywordInfo<NumberKeyword>) : BaseKeywordDigester<NumberKeyword>(keyword) {

  override fun extractKeyword(jsonValue: JsrValue): NumberKeyword {
    return NumberKeyword(jsonValue.unbox())
  }
}
