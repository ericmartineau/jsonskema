package io.mverse.jsonschema.loading.keyword.versions.flex

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.json.JsrType

data class AdditionalItemsKeywordDigester(
    override val includedKeywords: List<KeywordInfo<ItemsKeyword>> = Keywords.ADDITIONAL_ITEMS.getTypeVariants(JsrType.OBJECT))
  : KeywordDigester<ItemsKeyword> {

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<ItemsKeyword>? {

    val additionalItems = jsonObject.path(Keywords.ADDITIONAL_ITEMS)
    val addtlItemsBuilder = schemaLoader.subSchemaBuilder(additionalItems, additionalItems.rootObject, report)
    builder.schemaOfAdditionalItems = addtlItemsBuilder

    // empty because we added the vlaue to the builder manually
    return null
  }
}
