package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.json

data class SchemaMapKeyword(override val value: Map<String, Schema> = emptyMap()): SubschemaKeyword,
    KeywordImpl<Map<String, Schema>>(){

  override val subschemas: List<Schema>
    get() = value.values.toList()

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.run {
      keyword.key to json {
        for ((key,schema) in value) {
          val schemaJson = when (schema) {
            is RefSchema-> schema.toJson(includeExtraProperties = includeExtraProperties)
            else-> schema.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
          }

          // Writes the key to the json builder
          key to schemaJson
        }
      }
    }
  }

  operator fun plus(schema:Pair<String, Schema>):SchemaMapKeyword {
    return SchemaMapKeyword(this.value + schema)
  }

  override fun toString(): String {
    return value.toString()
  }

  override fun withValue(value: Map<String, Schema>): Keyword<Map<String, Schema>> {
    return this.copy(value=value)
  }

}
