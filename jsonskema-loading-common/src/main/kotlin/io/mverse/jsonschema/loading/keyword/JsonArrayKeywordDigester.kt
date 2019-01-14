package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.json.JsrArray
import lang.json.JsrValue

class JsonArrayKeywordDigester(keyword: KeywordInfo<JsonArrayKeyword>) : BaseKeywordDigester<JsonArrayKeyword>(keyword) {

  override fun extractKeyword(jsonValue: JsrValue): JsonArrayKeyword {
    return JsonArrayKeyword(jsonValue as JsrArray)
  }
}
