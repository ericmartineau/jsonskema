package io.mverse.jsonschema.loading.keyword.versions.flex

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.Companion.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.utils.Schemas.falseSchema
import kotlinx.serialization.json.ElementType.BOOLEAN

data class AdditionalPropertiesBooleanKeywordDigester(
    override val includedKeywords: List<KeywordInfo<SingleSchemaKeyword>> = ADDITIONAL_PROPERTIES.getTypeVariants(BOOLEAN)
) : KeywordDigester<SingleSchemaKeyword> {

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder<*>,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<SingleSchemaKeyword>? {
    val additionalProperties = jsonObject.path(Keywords.ADDITIONAL_PROPERTIES)
    return if (additionalProperties.isBoolean && additionalProperties.boolean == false) {
      KeywordDigest.ofNullable(includedKeywords[0], SingleSchemaKeyword(falseSchema))
    } else null
  }
}
