package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath
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

  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<URI>, report: MergeReport): Keyword<URI> {
    report += mergeConflict(path, keyword, this, other)
    return IdKeyword((other as IdKeyword).value)
  }

  companion object {
    const val ID = "id"
    const val DOLLAR_ID = "\$id"
  }
}
