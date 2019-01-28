package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.asVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.utils.isFalseSchema
import lang.json.JsonPath
import lang.json.MutableJsrObject
import lang.json.createJsrArray

data class ItemsKeyword(val indexedSchemas: List<Schema> = emptyList(),
                        val allItemSchema: Schema? = null,
                        val additionalItemSchema: Schema? = null)
  : SchemaListKeyword(indexedSchemas) {

  val hasIndexedSchemas: Boolean get() = indexedSchemas.isNotEmpty()

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    if (!indexedSchemas.isEmpty()) {
      builder.apply {
        Keywords.ITEMS.key *= createJsrArray(indexedSchemas.map { it.asVersion(version).toJson(includeExtraProperties = true) })
      }
    } else {
      allItemSchema?.let { schema ->
        builder.run {
          Keywords.ITEMS.key *= schema.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
        }
      }
    }
    additionalItemSchema?.also { schema ->
      builder.apply {
        if (version.isBefore(JsonSchemaVersion.Draft6) && schema.isFalseSchema) {
          Keywords.ADDITIONAL_ITEMS.key *= false
        } else {
          Keywords.ADDITIONAL_ITEMS.key *= additionalItemSchema.asVersion(version).toJson(includeExtraProperties = true)
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

  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<List<Schema>>, report: MergeReport): ItemsKeyword {
    other as ItemsKeyword
    val allItemSchema = allItemSchema?.merge(path, other.allItemSchema, report)
        ?: other.allItemSchema
    val addtlItemSchema = additionalItemSchema?.merge(path.sibling(additionItemsKey), other.additionalItemSchema, report)
    val indexedSchemas = if (other.hasIndexedSchemas) other.indexedSchemas else indexedSchemas
    return ItemsKeyword(indexedSchemas, allItemSchema, addtlItemSchema)
  }

  override fun withValue(value: List<Schema>): ItemsKeyword = this.copy(indexedSchemas = value)

  companion object {
    val additionItemsKey = Keywords.ADDITIONAL_ITEMS.key
  }
}
