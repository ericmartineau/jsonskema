package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.StringSetKeyword
import lang.json.JsrArray
import lang.json.JsrValue
import lang.json.unbox
import lang.json.values

class StringSetKeywordDigester(keyword: KeywordInfo<StringSetKeyword>) : BaseKeywordDigester<StringSetKeyword>(keyword) {

  override fun extractKeyword(jsonValue: JsrValue): StringSetKeyword {
    val values = linkedSetOf<String>()
    for (string in (jsonValue as JsrArray).values) {
      values.add(string.unbox())
    }
    return StringSetKeyword(values)
  }
}
