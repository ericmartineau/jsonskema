package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.mergeAdd
import lang.collection.freezeMap
import lang.json.JsonPath
import lang.json.MutableJsrObject
import lang.json.jsrObject

data class SchemaMapKeyword(val input: Map<String, Schema> = emptyMap()) : SubschemaKeyword,
    KeywordImpl<Map<String, Schema>>() {

  override val value = input.freezeMap()

  override val subschemas: List<Schema>
    get() = value.values.toList()

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.run {
      keyword.key *= jsrObject {
        for ((key, schema) in value) {
          val schemaJson = when (schema) {
            is RefSchema -> schema.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
            else -> schema.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
          }

          // Writes the key to the json builder
          key *= schemaJson
        }
      }
    }
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Map<String, Schema>>, report: MergeReport): Keyword<Map<String, Schema>> {
    val schemas = mutableMapOf<String, Schema>()
    schemas.putAll(this.value)
    other.value.forEach { (prop,schema) ->
      val child = path.child(prop)
      if (prop in schemas) {
        schemas[prop] = strategy.merge(child, schemas[prop]!!, schema, report, null)
      } else {
        report += mergeAdd(child, keyword)
        schemas[prop] = schema
      }
    }

    return SchemaMapKeyword(input = schemas.freezeMap())
  }

  operator fun plus(schema: Pair<String, Schema>): SchemaMapKeyword {
    return SchemaMapKeyword(this.value + schema)
  }

  override fun toString(): String {
    return value.toString()
  }

  override fun withValue(value: Map<String, Schema>): Keyword<Map<String, Schema>> {
    return this.copy(input = value)
  }
}
