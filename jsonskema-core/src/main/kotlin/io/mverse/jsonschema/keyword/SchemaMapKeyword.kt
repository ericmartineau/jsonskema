package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.json

data class SchemaMapKeyword(val schemas: Map<String, Schema> = emptyMap()): SubschemaKeyword,
    JsonSchemaKeywordImpl<Map<String, Schema>>(schemas){

  override val subschemas: List<Schema>
    get() = schemas.values.toList()

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    builder.run {
      keyword.key to json {
        for ((key,schema) in schemas) {
          val schemaJson = when (schema) {
            is RefSchema-> schema.toJson()
            else-> schema.asVersion(version).toJson()
          }

          // Writes the key to the json builder
          key to schemaJson
        }
      }
    }
  }

  operator fun plus(schema:Pair<String, Schema>):SchemaMapKeyword {
    return SchemaMapKeyword(this.schemas + schema)
  }

  override fun toString(): String {
    return schemas.toString()
  }
}
