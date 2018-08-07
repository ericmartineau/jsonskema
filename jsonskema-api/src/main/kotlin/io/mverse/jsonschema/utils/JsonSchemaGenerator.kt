//package io.mverse.jsonschema.utils
//
//import io.mverse.jsonschema.keyword.KeywordInfo
//import lang.isIntegral
//import lang.json.JsonGenerator
//import lang.json.JsonValue
//
//class JsonSchemaGenerator(private val wrapped: JsonGenerator) : JsonGenerator by wrapped {
//
//  fun write(key: String, number: Number?): JsonSchemaGenerator {
//    if (number == null) {
//      write(key, JsonValue.NULL)
//    } else {
//      if (number.isIntegral()) {
//        write(key, number.toInt())
//      } else {
//        write(key, number.toDouble())
//      }
//    }
//    return this
//  }
//
//  fun writeKey(keyword: KeywordInfo<*>): JsonSchemaGenerator {
//    wrapped.writeKey(keyword.key())
//    return this
//  }
//}
