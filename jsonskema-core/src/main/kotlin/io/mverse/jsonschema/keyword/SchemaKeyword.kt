package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import lang.URI

class SchemaKeyword : JsonSchemaKeyword<URI> {

  override val value: URI? = null

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    version.metaschemaURI?.let { schemaUri ->
      builder.run {
        SCHEMA_KEYWORD to schemaUri.toString()
      }
    }
  }

  override fun hashCode(): Int {
    return 1
  }

  override fun equals(other: Any?): Boolean {
    return other is SchemaKeyword
  }

  override fun toString(): String {
    return "latest"
  }

  companion object {
    val SCHEMA_KEYWORD = "\$schema"
  }
}
