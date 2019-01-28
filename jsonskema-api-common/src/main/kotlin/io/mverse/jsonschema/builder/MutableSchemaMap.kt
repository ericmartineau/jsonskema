package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchema.schemaBuilder
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SchemaMapKeyword

class MutableSchemaMap(val keyword: KeywordInfo<SchemaMapKeyword>,
                       val builder: MutableSchema) {

  operator fun set(key: String, block: MutableSchema.() -> Unit) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    val childLocation = builder.location.child(keyword).child(key)
    val newProperty = schemaBuilder(childLocation, builder.schemaLoader, block)
    builder[keyword] = existing + (key to builder.buildSubSchema(newProperty, keyword, key))
  }

  operator fun set(key: String, schema: MutableSchema) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    val childLocation = builder.location.child(keyword).child(key)
    val newProperty = schema.withLocation(childLocation)
    builder[keyword] = existing + (key to builder.buildSubSchema(newProperty, keyword, key))
  }

  operator fun invoke(block: MutableSchemaMap.() -> Unit) {
    block()
  }

  fun toSchemaMap(): Map<String, Schema> = builder[keyword]?.value ?: emptyMap()
}
