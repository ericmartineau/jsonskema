package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues.typeMismatch
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.json.JsrType

class ItemsKeywordDigester : KeywordDigester<ItemsKeyword> {

  override val includedKeywords: List<KeywordInfo<ItemsKeyword>>
    get() = ITEMS.getTypeVariants(JsrType.OBJECT, JsrType.ARRAY)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<ItemsKeyword>? {
    val itemsValue = jsonObject.path(Keywords.ITEMS)
    when (itemsValue.type) {
      JsrType.OBJECT -> builder.allItemSchema = schemaLoader.subSchemaBuilder(itemsValue, itemsValue.rootObject, report)
      JsrType.ARRAY -> itemsValue.forEachIndex { _, idxValue ->
        if (idxValue.type !== JsrType.OBJECT) {
          report.error(typeMismatch(Keywords.ITEMS, idxValue))
        } else {
          builder.itemSchemas += schemaLoader.subSchemaBuilder(idxValue, idxValue.rootObject, report)
        }
      }
      else -> report.error(typeMismatch(Keywords.ITEMS, itemsValue))
    }

    return null
  }
}
