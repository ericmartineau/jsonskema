package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder

data class SingleSchemaKeyword(override val value: Schema) : KeywordImpl<Schema>() {
  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.apply {
      keyword.key to value.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
    }
  }

  override fun withValue(value: Schema): Keyword<Schema> = this.copy(value=value)
}
