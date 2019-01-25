package io.mverse.jsonschema.loading.keyword.versions.flex

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest

data class AdditionalPropertiesKeywordDigester(
    override val includedKeywords: List<KeywordInfo<SingleSchemaKeyword>> = listOf(Keywords.ADDITIONAL_PROPERTIES)
) : KeywordDigester<SingleSchemaKeyword> {

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<SingleSchemaKeyword>? {
    val additionalProperties = jsonObject.path(Keywords.ADDITIONAL_PROPERTIES)
    val keywordValue = SingleSchemaKeyword(schemaLoader.loadSubSchema(additionalProperties, jsonObject.rootObject, report))
    return ADDITIONAL_PROPERTIES.digest(keywordValue)
  }
}
