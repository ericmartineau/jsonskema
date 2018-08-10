package io.mverse.jsonschema.loading

import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.Companion.ALL_OF
import io.mverse.jsonschema.keyword.Keywords.Companion.ANY_OF
import io.mverse.jsonschema.keyword.Keywords.Companion.COMMENT
import io.mverse.jsonschema.keyword.Keywords.Companion.CONST
import io.mverse.jsonschema.keyword.Keywords.Companion.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.Companion.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.Companion.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.Companion.DEFAULT
import io.mverse.jsonschema.keyword.Keywords.Companion.DEFINITIONS
import io.mverse.jsonschema.keyword.Keywords.Companion.DESCRIPTION
import io.mverse.jsonschema.keyword.Keywords.Companion.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.Companion.ELSE
import io.mverse.jsonschema.keyword.Keywords.Companion.ENUM
import io.mverse.jsonschema.keyword.Keywords.Companion.EXAMPLES
import io.mverse.jsonschema.keyword.Keywords.Companion.FORMAT
import io.mverse.jsonschema.keyword.Keywords.Companion.IF
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_LENGTH
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_LENGTH
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.MULTIPLE_OF
import io.mverse.jsonschema.keyword.Keywords.Companion.NOT
import io.mverse.jsonschema.keyword.Keywords.Companion.ONE_OF
import io.mverse.jsonschema.keyword.Keywords.Companion.PATTERN
import io.mverse.jsonschema.keyword.Keywords.Companion.PATTERN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTY_NAMES
import io.mverse.jsonschema.keyword.Keywords.Companion.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.Companion.REF
import io.mverse.jsonschema.keyword.Keywords.Companion.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.Companion.THEN
import io.mverse.jsonschema.keyword.Keywords.Companion.TITLE
import io.mverse.jsonschema.keyword.Keywords.Companion.UNIQUE_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.WRITE_ONLY
import io.mverse.jsonschema.loading.keyword.BooleanKeywordDigester
import io.mverse.jsonschema.loading.keyword.DependenciesKeywordDigester
import io.mverse.jsonschema.loading.keyword.ItemsKeywordDigester
import io.mverse.jsonschema.loading.keyword.JsonArrayKeywordDigester
import io.mverse.jsonschema.loading.keyword.JsonValueKeywordDigester
import io.mverse.jsonschema.loading.keyword.ListKeywordDigester
import io.mverse.jsonschema.loading.keyword.MapKeywordDigester
import io.mverse.jsonschema.loading.keyword.NumberKeywordDigester
import io.mverse.jsonschema.loading.keyword.SchemaKeywordDigester
import io.mverse.jsonschema.loading.keyword.SingleSchemaKeywordDigester
import io.mverse.jsonschema.loading.keyword.StringKeywordDigester
import io.mverse.jsonschema.loading.keyword.StringSetKeywordDigester
import io.mverse.jsonschema.loading.keyword.TypeKeywordDigester
import io.mverse.jsonschema.loading.keyword.URIKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.IdKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.AdditionalItemsBooleanKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.AdditionalItemsKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.AdditionalPropertiesBooleanKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.AdditionalPropertiesKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.LimitBooleanKeywordDigester
import io.mverse.jsonschema.loading.keyword.versions.flex.LimitKeywordDigester
import kotlinx.serialization.json.ElementType.ARRAY
import kotlinx.serialization.json.ElementType.BOOLEAN
import kotlinx.serialization.json.ElementType.NULL
import kotlinx.serialization.json.ElementType.NUMBER
import kotlinx.serialization.json.ElementType.OBJECT
import kotlinx.serialization.json.ElementType.STRING

interface KeywordDigesters {
  companion object {
    fun defaultKeywordLoaders(): List<KeywordDigester<*>> {
      return mutableListOf(
          SchemaKeywordDigester(),
          URIKeywordDigester(REF),
          IdKeywordDigester(DOLLAR_ID),
          IdKeywordDigester(Keywords.ID),
          StringKeywordDigester(TITLE),
          StringKeywordDigester(DESCRIPTION),
          MapKeywordDigester(DEFINITIONS),
          JsonValueKeywordDigester(DEFAULT, ARRAY, OBJECT, STRING, BOOLEAN, NUMBER, NULL),
          MapKeywordDigester(PROPERTIES),
          NumberKeywordDigester(MAX_PROPERTIES),
          StringSetKeywordDigester(REQUIRED),
          NumberKeywordDigester(MIN_PROPERTIES),
          DependenciesKeywordDigester(),
          MapKeywordDigester(PATTERN_PROPERTIES),
          SingleSchemaKeywordDigester(PROPERTY_NAMES),
          TypeKeywordDigester(),
          NumberKeywordDigester(MULTIPLE_OF),
          LimitBooleanKeywordDigester.maximumExtractor(),
          LimitBooleanKeywordDigester.minimumExtractor(),
          LimitKeywordDigester.minimumExtractor(),
          LimitKeywordDigester.maximumExtractor(),
          AdditionalPropertiesBooleanKeywordDigester(),
          AdditionalPropertiesKeywordDigester(),
          StringKeywordDigester(FORMAT),
          NumberKeywordDigester(MAX_LENGTH),
          NumberKeywordDigester(MIN_LENGTH),
          StringKeywordDigester(PATTERN),
          ItemsKeywordDigester(),
          AdditionalItemsBooleanKeywordDigester(),
          AdditionalItemsKeywordDigester(),
          NumberKeywordDigester(MAX_ITEMS),
          NumberKeywordDigester(MIN_ITEMS),
          BooleanKeywordDigester(UNIQUE_ITEMS),
          SingleSchemaKeywordDigester(CONTAINS),
          JsonArrayKeywordDigester(ENUM),
          JsonArrayKeywordDigester(EXAMPLES),
          JsonValueKeywordDigester(CONST, ARRAY, OBJECT, STRING, BOOLEAN, NUMBER, NULL),
          SingleSchemaKeywordDigester(NOT),
          ListKeywordDigester(ALL_OF),
          ListKeywordDigester(ANY_OF),
          ListKeywordDigester(ONE_OF),
          // Draft7 Additions
          SingleSchemaKeywordDigester(IF),
          SingleSchemaKeywordDigester(THEN),
          SingleSchemaKeywordDigester(ELSE),
          StringKeywordDigester(COMMENT),
          BooleanKeywordDigester(READ_ONLY),
          BooleanKeywordDigester(WRITE_ONLY),
          StringKeywordDigester(CONTENT_ENCODING),
          StringKeywordDigester(CONTENT_MEDIA_TYPE))
    }
  }
}
