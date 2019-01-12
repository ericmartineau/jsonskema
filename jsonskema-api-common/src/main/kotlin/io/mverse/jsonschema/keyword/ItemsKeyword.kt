package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.utils.isFalseSchema
import kotlinx.serialization.json.JsonBuilder
import lang.json.toKtArray

data class ItemsKeyword(val indexedSchemas: List<Schema> = emptyList(),
                        val allItemSchema: Schema? = null,
                        val additionalItemSchema: Schema? = null)
  : SchemaListKeyword(indexedSchemas) {

  val hasIndexedSchemas: Boolean get() = indexedSchemas.isNotEmpty()

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    if (!indexedSchemas.isEmpty()) {
      builder.run {
        Keywords.ITEMS.key to indexedSchemas.map { it.asVersion(version).toJson() }.toKtArray()
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
        if (version.isBefore(JsonSchemaVersion.Draft6) && schema.isFalseSchema) {
          Keywords.ADDITIONAL_ITEMS.key to false
        } else {
          Keywords.ADDITIONAL_ITEMS.key to additionalItemSchema.asVersion(version).toJson()
        }
      }

    }
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

  override fun withValue(value: List<Schema>): ItemsKeyword = this.copy(indexedSchemas = value)
}
