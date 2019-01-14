package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.MutableJsrObject

data class SingleSchemaKeyword(override val value: Schema) : KeywordImpl<Schema>() {
  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.apply {
      keyword.key *= value.asVersion(version).toJson(includeExtraProperties = includeExtraProperties)
    }
  }

  override fun withValue(value: Schema): Keyword<Schema> = this.copy(value = value)
}
