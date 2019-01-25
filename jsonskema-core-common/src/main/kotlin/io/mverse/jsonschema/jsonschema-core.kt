package io.mverse.jsonschema

import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.toMutableJsonObject

//fun JsonSchema.schemaBuilder(loader: SchemaLoader = JsonSchema.schemaReader.loader): MutableJsonSchema = MutableJsonSchema(loader)
//fun JsonSchema.schemaBuilder(id: URI): MutableJsonSchema = MutableJsonSchema(id)
//fun JsonSchema.schemaBuilder(id: String): MutableJsonSchema = MutableJsonSchema(URI(id))

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
