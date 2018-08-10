package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.URI
import lang.json.toJsonObject

fun JsonSchema.schemaBuilder():JsonSchemaBuilder = JsonSchemaBuilder()
fun JsonSchema.schemaBuilder(id: URI):JsonSchemaBuilder = JsonSchemaBuilder(id)
fun JsonSchema.schemaBuilder(id: String):JsonSchemaBuilder = JsonSchemaBuilder(URI(id))

fun jsonschema(id: String? = null, init: SchemaBuilder<*>.() -> Unit): SchemaBuilder<*> {
  val builder = when(id) {
    null-> JsonSchemaBuilder()
    else-> JsonSchemaBuilder(URI(id))
  }
  builder.init()
  return builder
}

val JsonElement.string get() = this.primitive.contentOrNull
val JsonElement.int get() = this.primitive.intOrNull
val JsonElement.double get() = this.primitive.doubleOrNull
val JsonElement.number get() = this.primitive.doubleOrNull
val JsonElement.boolean get() = this.primitive.booleanOrNull


operator fun kotlinx.serialization.json.JsonObject.minus(key:String): kotlinx.serialization.json.JsonObject = kotlinx.serialization.json.JsonObject(this.content - key)
operator fun kotlinx.serialization.json.JsonObject.plus(pair:Pair<String, JsonElement>): kotlinx.serialization.json.JsonObject {
  return this.toMutableMap().apply {
    this[pair.first] = pair.second
  }.toJsonObject()
}
