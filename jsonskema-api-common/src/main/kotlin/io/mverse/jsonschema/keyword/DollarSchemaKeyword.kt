package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath
import lang.json.MutableJsrObject
import lang.net.URI

open class DollarSchemaKeyword(override val value: URI) : Keyword<URI> {

  override fun withValue(value: URI): Keyword<URI> {
    return DollarSchemaKeyword(value = value)
  }
  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    val metaSchema = value
    when {
      metaSchema == emptyUri-> version.metaschemaURI?.let { schemaUri ->
        builder.run {
          SCHEMA_KEYWORD *= schemaUri.toString()
        }
      }
      else-> builder.run {
        SCHEMA_KEYWORD *= value.toString()
      }
    }
  }

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<URI>, report: MergeReport): Keyword<URI> {
    report += mergeConflict(path, keyword, this, other)
    return DollarSchemaKeyword((other as DollarSchemaKeyword).value)
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is DollarSchemaKeyword && other.value == this.value
  }

  override fun toString(): String {
    return if(value == emptyUri) "latest" else value.toString()
  }

  companion object {
    const val SCHEMA_KEYWORD = "\$schema"
    val emptyUri = URI("")
  }
}
