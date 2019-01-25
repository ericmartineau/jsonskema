package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.DollarSchemaKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.SCHEMA_KEYWORD
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import io.mverse.jsonschema.utils.withoutFragment
import lang.json.JsrString
import lang.json.stringValue
import lang.net.URI

class SchemaKeywordDigester : KeywordDigester<DollarSchemaKeyword> {
  override val includedKeywords: List<KeywordInfo<DollarSchemaKeyword>>
    get() = listOf(SCHEMA)

  override fun extractKeyword(jsonObject: JsonValueWithPath,
                              builder: MutableSchema,
                              schemaLoader: SchemaLoader,
                              report: LoadingReport): KeywordDigest<DollarSchemaKeyword>? {

    return when (jsonObject.containsKey(SCHEMA_KEYWORD)) {
      true -> {
        val uriValue = jsonObject[SCHEMA_KEYWORD] as JsrString
        val metaSchema = URI(uriValue.stringValue).withoutFragment()
        val isKnownVersion = JsonSchemaVersion.publicVersions.any {
          it.metaschemaURI?.withoutFragment() == metaSchema
        }
        if (isKnownVersion) {
          SCHEMA.digest(DollarSchemaKeyword(emptyUri))
        } else {
          SCHEMA.digest(DollarSchemaKeyword(metaSchema))
        }
      }
      false -> null
    }
  }
}
