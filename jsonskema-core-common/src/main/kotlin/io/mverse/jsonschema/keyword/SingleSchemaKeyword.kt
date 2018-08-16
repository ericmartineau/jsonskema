package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion

data class SingleSchemaKeyword(override val value: Schema) : JsonSchemaKeywordImpl<Schema>() {
  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    builder.apply {
      keyword.key to value.asVersion(version).toJson()
    }
  }

  override fun withValue(value: Schema): JsonSchemaKeyword<Schema> = this.copy(value=value)
}
