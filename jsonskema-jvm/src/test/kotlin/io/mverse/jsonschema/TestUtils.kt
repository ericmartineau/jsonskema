package io.mverse.jsonschema

import assertk.Assert
import assertk.assert
import assertk.assertThat
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.utils.SchemaPaths
import kotlinx.serialization.json.JsonNull
import lang.BigInteger
import lang.exception.illegalState
import lang.json.JsrValue
import lang.json.jsrArrayOf
import lang.json.jsrNumber
import lang.json.jsrObject
import lang.json.toJsrValue
import lang.net.URI
import lang.uuid.randomUUID
import java.math.BigDecimal

fun schema(block:MutableSchema.()->Unit):Schema {
  return JsonSchemas.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID()), loader = JsonSchemas.schemaLoader).build(block)
}

fun JsonSchemas.schema(block:MutableSchema.()->Unit):Schema {
  return JsonSchemas.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID()), loader = JsonSchemas.schemaLoader).build(block)
}

fun schema(id:URI, block:MutableSchema.()->Unit):Schema {
  return JsonSchemas.schemaBuilder(id, loader = JsonSchemas.schemaLoader).build(block)
}

fun schema(id:String, block:MutableSchema.()->Unit):Schema {
  return JsonSchemas.schemaBuilder(id, loader = JsonSchemas.schemaLoader).build(block)
}

fun JsonSchemas.schema(id:String, block:MutableSchema.()->Unit):Schema {
  return JsonSchemas.schemaBuilder(id, loader = JsonSchemas.schemaLoader).build(block)
}

fun schemaBuilder(block:MutableSchema.()->Unit):MutableSchema {
  return JsonSchemas.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID()), loader = JsonSchemas.schemaLoader).apply(block)
}

fun schemaBuilder(id:String, block:MutableSchema.()->Unit):MutableSchema {
  return JsonSchemas.schemaBuilder(id = id, loader = JsonSchemas.schemaLoader).apply(block)
}
fun schemaBuilder(id:URI, block:MutableSchema.()->Unit):MutableSchema {
  return JsonSchemas.schemaBuilder(id = id, loader = JsonSchemas.schemaLoader).apply(block)
}

object TestUtils {

  fun createValue(value: JsrValue): JsonValueWithPath {
    return JsonValueWithPath.fromJsonValue(value)
  }

  fun createJsonObjectWithLocation(): JsonValueWithPath {
    val json = jsrObject {
      "foo" *= "bar"
      "num" *= 3
    }
    return JsonValueWithPath.fromJsonValue(json)
  }

  fun createJsonNumberWithLocation(number: Number): JsonValueWithPath {
    val jsrValue = when(number) {
      is Double-> jsrNumber(number)
      is Long-> jsrNumber(number)
      is BigDecimal-> jsrNumber(number)
      is BigInteger-> jsrNumber(number)
      is Int -> jsrNumber(number)
      else-> jsrNumber(number.toDouble())
    }
    return JsonValueWithPath.fromJsonValue(jsrValue)

  }

  fun createJsonStringWithLocation(string: String): JsonValueWithPath {
    val jsrValue = toJsrValue(string)
    return JsonValueWithPath.fromJsonValue(jsrValue)
  }

  fun createJsonArrayWithLocation(): JsonValueWithPath {
    val jsrValue = jsrArrayOf("foo", "bar", 3, true, JsonNull)

    return JsonValueWithPath.fromJsonValue(jsrValue)
  }
}

inline fun <reified T : Any?> T.assertThat(message: String? = null): Assert<T> = assertThat(this, message)
inline fun <reified T, reified V> T?.assertThat(block: T.() -> V): Assert<V> = assertThat<V>(this?.block()
    ?: illegalState("Accessor method returned null"))
