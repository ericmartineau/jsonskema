package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaMapKeyword

class MutableSchemaMap(val keyword: KeywordInfo<SchemaMapKeyword>,
                       val builder: MutableSchema) {

  operator fun set(key: String, schema: MutableSchema) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    builder[keyword] = existing + (key to builder.buildSubSchema(schema, keyword, key))
  }

  operator fun set(key: String, block: MutableSchema.() -> Unit) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    builder[keyword] = existing + (key to builder.buildSubSchema(JsonSchema.schemaBuilder {
      schemaLoader = builder.schemaLoader
      block()
    }, keyword, key))
  }

  fun toSchemaMap(): Map<String, Schema> = builder[keyword]?.value ?: emptyMap()
}
