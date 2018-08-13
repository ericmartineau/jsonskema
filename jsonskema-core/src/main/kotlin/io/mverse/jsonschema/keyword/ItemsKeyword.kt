package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.utils.Schemas.falseSchema
import lang.json.toJsonArray

data class ItemsKeyword(val indexedSchemas: List<Schema> = emptyList(),
                        val allItemSchema: Schema? = null,
                        val additionalItemSchema: Schema? = null)
  : SchemaListKeyword(indexedSchemas) {

  val hasIndexedSchemas: Boolean get() = indexedSchemas.isNotEmpty()

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    if (!indexedSchemas.isEmpty()) {
      builder.run {
        Keywords.ITEMS.key to indexedSchemas.map { it.asVersion(version).toJson() }.toJsonArray()
      }
    } else {
      allItemSchema?.let { schema ->
        builder.run {
          Keywords.ITEMS.key to schema.asVersion(version).toJson()
        }
      }
    }
    additionalItemSchema?.also { schema ->
      builder.apply {
        if (version.isBefore(JsonSchemaVersion.Draft6) && falseSchema == schema) {
          Keywords.ADDITIONAL_ITEMS.key to false
        } else {
          Keywords.ADDITIONAL_ITEMS.key to additionalItemSchema.asVersion(version).toJson()
        }
      }

    }
  }

  override fun copy(value: List<Schema>): JsonSchemaKeyword<List<Schema>> {
    return this.copy(indexedSchemas = value)
  }

  override fun toString(): String {
    val result = StringBuilder()
    if (!indexedSchemas.isEmpty()) {
      result.append("[")
      for (schema in indexedSchemas) {
        result.append(schema.toString())
      }
      result.append("[")
    } else {
      when (allItemSchema) {
        null -> result.append("none")
        else -> result.append(allItemSchema.toString())
      }
    }
    if (additionalItemSchema != null) {
      result.append("additionalItems=").append(additionalItemSchema)
    }
    return result.toString()
  }
}
