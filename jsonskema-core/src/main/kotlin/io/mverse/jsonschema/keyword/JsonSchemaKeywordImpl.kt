package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import lang.URI
import lang.hashKode
import lang.illegalState
import lang.json.asJsonArray

abstract class JsonSchemaKeywordImpl<T>(val keywordValue: T? = null) : JsonSchemaKeyword<T> {

  override val value: T? = keywordValue

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    val jsonKey = keyword.key
    builder.run {
      when (keywordValue) {
        is String-> jsonKey to keywordValue
        is JsonElement-> jsonKey to keywordValue
        is URI-> jsonKey to keywordValue.toString()
        is Boolean-> jsonKey to keywordValue
        is Set<*>-> jsonKey to keywordValue.map { it.toString() }.asJsonArray()
//        if (DoubleMath.isMathematicalInteger(number.doubleValue())) {
//          generator.write(jsonKey, number.intValue())
//        } else {
//          generator.write(jsonKey, number.doubleValue())
//        }
        is Number-> jsonKey to keywordValue
        else-> illegalState("Dont know how to write keyword value $keywordValue")
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is JsonSchemaKeywordImpl<*> && other.keywordValue == keywordValue
  }

  override fun hashCode(): Int {
    return hashKode(keywordValue)
  }

  override fun toString(): String = keywordValue.toString()
}
