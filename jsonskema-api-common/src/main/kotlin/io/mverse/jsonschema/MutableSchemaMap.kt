package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaMapKeyword

class MutableSchemaMap(val keyword: KeywordInfo<SchemaMapKeyword>,
                       val builder: SchemaBuilder) {

  operator fun set(key:String, schema:SchemaBuilder) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    builder[keyword] = existing + (key to builder.buildSubSchema(schema, keyword, key))
  }

  operator fun set(key:String, block:SchemaBuilder.()->Unit) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    builder[keyword] = existing + (key to builder.buildSubSchema(JsonSchema.schemaBuilder.apply(block), keyword, key))
  }

  fun toSchemaMap():Map<String, Schema> = builder[keyword]?.value ?: emptyMap()
}
