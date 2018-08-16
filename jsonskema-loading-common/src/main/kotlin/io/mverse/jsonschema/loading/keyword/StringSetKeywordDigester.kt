package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.StringSetKeyword
import kotlinx.serialization.json.JsonElement

class StringSetKeywordDigester(keyword: KeywordInfo<StringSetKeyword>) : BaseKeywordDigester<StringSetKeyword>(keyword) {

  override fun extractKeyword(jsonElement: JsonElement): StringSetKeyword {
    val values = linkedSetOf<String>()
    for (string in jsonElement.jsonArray) {
      values.add(string.primitive.content)
    }
    return StringSetKeyword(values)
  }
}
