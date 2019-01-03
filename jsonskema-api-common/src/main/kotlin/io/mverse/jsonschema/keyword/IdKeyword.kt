package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import lang.URI

data class IdKeyword(override val value: URI) : KeywordImpl<URI>() {
  override fun withValue(value: URI): Keyword<URI> = this.copy(value=value)

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.run {
      when {
        version.isBefore(JsonSchemaVersion.Draft5) -> ID to value.toString()
        else -> DOLLAR_ID to value.toString()
      }
    }
  }

  companion object {
    const val ID = "id"
    const val DOLLAR_ID = "\$id"
  }
}
