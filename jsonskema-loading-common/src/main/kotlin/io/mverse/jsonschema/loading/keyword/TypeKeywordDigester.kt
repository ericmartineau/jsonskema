package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaTypes
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import kotlinx.serialization.json.ElementType.ARRAY
import kotlinx.serialization.json.ElementType.STRING

class TypeKeywordDigester : KeywordDigester<TypeKeyword> {

  override val includedKeywords = TYPE.getTypeVariants(STRING, ARRAY)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<TypeKeyword>? {
    val type = jsonObject.path(Keywords.TYPE)
    val valueType = type.type
    return when (valueType) {
      ARRAY -> {
        val typeArray = type.jsonArray!!
            .map { it.primitive.content }
            .map { JsonSchemaTypes.fromString(it) }
            .toSet()

        TYPE.digest(TypeKeyword(typeArray))
      }
      else -> {
        val typeString = JsonSchemaTypes.fromString(type.string)
        return TYPE.digest(TypeKeyword(typeString))
      }
    }
  }
}
