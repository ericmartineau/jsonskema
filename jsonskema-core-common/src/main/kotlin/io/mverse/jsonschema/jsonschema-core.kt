package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.URI
import lang.json.toJsonObject

fun JsonSchema.schemaBuilder():JsonSchemaBuilder = JsonSchemaBuilder()
fun JsonSchema.schemaBuilder(id: URI):JsonSchemaBuilder = JsonSchemaBuilder(id)
fun JsonSchema.schemaBuilder(id: String):JsonSchemaBuilder = JsonSchemaBuilder(URI(id))

val JsonElement.int get() = this.primitive.intOrNull
val JsonElement.double get() = this.primitive.doubleOrNull


operator fun JsonObject.minus(key:String): kotlinx.serialization.json.JsonObject = JsonObject(this.content - key)
operator fun JsonObject.plus(pair:Pair<String, JsonElement>): JsonObject {
  return this.toMutableMap().apply {
    this[pair.first] = pair.second
  }.toJsonObject()
}
