package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.JsonPath
import lang.json.MutableJsrObject

data class SingleSchemaKeyword(override val value: Schema) : KeywordImpl<Schema>() {
  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.apply {
      keyword.key *= value.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
    }
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Schema>, report: MergeReport): Keyword<Schema> {
    return SingleSchemaKeyword(strategy.merge(path, value, other.value, report))
  }

  override fun withValue(value: Schema): Keyword<Schema> = this.copy(value = value)
}
