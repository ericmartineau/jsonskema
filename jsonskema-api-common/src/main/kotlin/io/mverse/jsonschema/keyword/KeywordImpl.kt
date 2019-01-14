package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonElement
import lang.exception.illegalState
import lang.hashKode
import lang.isIntegral
import lang.json.JsrFalse
import lang.json.JsrNull
import lang.json.JsrTrue
import lang.json.JsrValue
import lang.json.MutableJsrObject
import lang.json.createJsrArray
import lang.json.jsrNumber
import lang.json.jsrString
import lang.json.toJsrValue
import lang.net.URI

abstract class KeywordImpl<T> : Keyword<T> {
  abstract override fun withValue(value: T): Keyword<T>

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    val jsonKey = keyword.key
    val keywordValue = value
    val keywordAsJsr: JsrValue = when (keywordValue) {
      null -> JsrNull
      is String -> jsrString(keywordValue)
      is JsrValue -> keywordValue
      is JsonElement -> keywordValue.toJsrValue()
      is URI -> jsrString(keywordValue.toString())
      is Boolean -> if (keywordValue) JsrTrue else JsrFalse
      is Iterable<*> -> createJsrArray(keywordValue.map { toJsrValue(it) })
      is Number -> {
        val number = keywordValue as Number
        if (number.isIntegral()) {
          jsrNumber(number.toLong())
        } else {
          jsrNumber(number.toDouble())
        }
      }
      else -> illegalState("Dont know how to write keyword value $keywordValue")
    }

    builder.run {
      jsonKey *= keywordAsJsr
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
