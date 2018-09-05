package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import lang.URI
import lang.hashKode
import lang.illegalState
import lang.isIntegral
import lang.json.toJsonArray
import lang.json.toJsonLiteral

abstract class KeywordImpl<T> : Keyword<T> {
  abstract override fun withValue(value: T): Keyword<T>

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    val jsonKey = keyword.key
    val keywordValue = value
    builder.run {
      when (keywordValue) {
        is String-> jsonKey to keywordValue.toJsonLiteral()
        is JsonElement-> jsonKey to keywordValue
        is URI-> jsonKey to keywordValue.toString().toJsonLiteral()
        is Boolean-> jsonKey to keywordValue.toJsonLiteral()
        is Iterable<*>-> jsonKey to keywordValue.map { it.toString() }.toJsonArray()
        is Number-> {
          val number = keywordValue as Number
          if (number.isIntegral()) {
            jsonKey to number.toInt().toJsonLiteral()
          } else {
            jsonKey to number.toDouble().toJsonLiteral()
          }
        }
        else-> illegalState("Dont know how to write keyword value $keywordValue")
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is KeywordImpl<*> && other.value == value
  }

  override fun hashCode(): Int {
    return hashKode(value)
  }

  override fun toString(): String = value.toString()
}
