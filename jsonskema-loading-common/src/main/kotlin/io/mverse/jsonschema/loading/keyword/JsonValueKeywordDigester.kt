package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.json.JsrType
import lang.json.JsrValue

class JsonValueKeywordDigester(keyword: KeywordInfo<JsonValueKeyword>, vararg acceptedTypes: JsrType)
  : BaseKeywordDigester<JsonValueKeyword>(keyword, *acceptedTypes) {

  override fun extractKeyword(jsonValue: JsrValue): JsonValueKeyword {
    return JsonValueKeyword(jsonValue)
  }
}
