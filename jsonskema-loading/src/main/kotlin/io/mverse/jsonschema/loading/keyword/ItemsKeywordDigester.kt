package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.Companion.ITEMS
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues.typeMismatch
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import kotlinx.serialization.json.ElementType.ARRAY
import kotlinx.serialization.json.ElementType.OBJECT

class ItemsKeywordDigester : KeywordDigester<ItemsKeyword> {

  override val includedKeywords: List<KeywordInfo<ItemsKeyword>>
    get() = ITEMS.getTypeVariants(OBJECT, ARRAY)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder<*>,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<ItemsKeyword>? {
    val itemsValue = jsonObject.path(Keywords.ITEMS)
    when (itemsValue.type) {
      OBJECT -> builder.allItemSchema(
          schemaLoader.subSchemaBuilder(itemsValue, itemsValue.rootObject, report)
      )
      ARRAY -> itemsValue.forEachIndex { idx, idxValue ->
        if (idxValue.type !== OBJECT) {
          report.error(typeMismatch(Keywords.ITEMS, idxValue))
        } else {
          builder.itemSchema(
              schemaLoader.subSchemaBuilder(idxValue, idxValue.rootObject, report)
          )
        }
      }
      else -> report.error(typeMismatch(Keywords.ITEMS, itemsValue))
    }

    return null
  }
}
