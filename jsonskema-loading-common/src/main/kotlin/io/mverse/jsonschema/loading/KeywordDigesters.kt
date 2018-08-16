package io.mverse.jsonschema.loading

import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ALL_OF
import io.mverse.jsonschema.keyword.Keywords.ANY_OF
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.CONST
import io.mverse.jsonschema.keyword.Keywords.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.DEFAULT
import io.mverse.jsonschema.keyword.Keywords.DEFINITIONS
import io.mverse.jsonschema.keyword.Keywords.DESCRIPTION
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ELSE
import io.mverse.jsonschema.keyword.Keywords.ENUM
import io.mverse.jsonschema.keyword.Keywords.EXAMPLES
import io.mverse.jsonschema.keyword.Keywords.FORMAT
import io.mverse.jsonschema.keyword.Keywords.IF
import io.mverse.jsonschema.keyword.Keywords.MAX_ITEMS
import io.mverse.jsonschema.keyword.Keywords.MAX_LENGTH
import io.mverse.jsonschema.keyword.Keywords.MAX_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.MIN_ITEMS
import io.mverse.jsonschema.keyword.Keywords.MIN_LENGTH
import io.mverse.jsonschema.keyword.Keywords.MIN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.MULTIPLE_OF
import io.mverse.jsonschema.keyword.Keywords.NOT
import io.mverse.jsonschema.keyword.Keywords.ONE_OF
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.keyword.Keywords.PATTERN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.PROPERTY_NAMES
import io.mverse.jsonschema.keyword.Keywords.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.THEN
import io.mverse.jsonschema.keyword.Keywords.TITLE
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.keyword.Keywords.WRITE_ONLY
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
