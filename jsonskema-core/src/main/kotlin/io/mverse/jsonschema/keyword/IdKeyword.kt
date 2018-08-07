package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import lang.URI

data class IdKeyword(private val idValue: URI) : URIKeyword(idValue) {

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    if (keywordValue != null) {
      builder.run {
        when {
          version.isBefore(JsonSchemaVersion.Draft5) -> ID to idValue.toString()
          else -> DOLLAR_ID to idValue.toString()
        }
      }
    }
  }

  companion object {
    val ID = "id"
    val DOLLAR_ID = "\$id"
  }
}
