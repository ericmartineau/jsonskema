package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.MutableJsrObject
import lang.net.URI

data class IdKeyword(override val value: URI) : KeywordImpl<URI>() {
  override fun withValue(value: URI): Keyword<URI> = this.copy(value = value)

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.run {
      when {
        version.isBefore(JsonSchemaVersion.Draft5) -> ID *= value.toString()
        else -> DOLLAR_ID *= value.toString()
      }
    }
  }

  companion object {
    const val ID = "id"
    const val DOLLAR_ID = "\$id"
  }
}
