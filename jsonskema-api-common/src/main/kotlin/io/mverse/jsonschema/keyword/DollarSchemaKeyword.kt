package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.MutableJsrObject
import lang.net.URI

open class DollarSchemaKeyword : Keyword<URI> {

  override fun withValue(value: URI): Keyword<URI> {
    return this
  }

  override val value: URI = URI("")

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    version.metaschemaURI?.let { schemaUri ->
      builder.run {
        SCHEMA_KEYWORD *= schemaUri.toString()
      }
    }
  }

  override fun hashCode(): Int {
    return 1
  }

  override fun equals(other: Any?): Boolean {
    return other is DollarSchemaKeyword
  }

  override fun toString(): String {
    return "latest"
  }

  companion object {
    const val SCHEMA_KEYWORD = "\$schema"
  }
}
