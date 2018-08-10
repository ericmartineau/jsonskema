package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder

class SingleSchemaKeyword(val schema: Schema) : JsonSchemaKeywordImpl<Schema>(schema) {

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    builder.apply {
      keyword.key to schema.asVersion(version).toJson()
    }
  }
}
