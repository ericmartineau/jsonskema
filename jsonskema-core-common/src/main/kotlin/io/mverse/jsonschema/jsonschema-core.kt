package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableJsonSchema
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.toMutableJsonObject
import lang.json.unbox
import lang.net.URI

fun JsonSchema.schemaBuilder(): MutableJsonSchema = MutableJsonSchema()
fun JsonSchema.schemaBuilder(id: URI): MutableJsonSchema = MutableJsonSchema(id)
fun JsonSchema.schemaBuilder(id: String): MutableJsonSchema = MutableJsonSchema(URI(id))

val JsrValue.int get() = this.unbox<Int>()
val JsrValue.double get() = this.unbox<Double>()

operator fun JsrObject.minus(key: String): JsrObject = this.toMutableJsonObject().run {
  this - key
  build()
}

operator fun JsrObject.plus(pair: Pair<String, JsrValue>): JsrObject {
  return this.toMutableJsonObject().run {
    pair.first *= pair.second
    build()
  }
}
