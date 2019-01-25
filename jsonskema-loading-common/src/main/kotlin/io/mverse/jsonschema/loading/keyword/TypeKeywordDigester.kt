package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaTypes
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import lang.json.JsrArray
import lang.json.JsrType
import lang.json.unbox
import lang.json.values

class TypeKeywordDigester : KeywordDigester<TypeKeyword> {

  override val includedKeywords = TYPE.getTypeVariants(JsrType.STRING, JsrType.ARRAY)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<TypeKeyword>? {
    val type = jsonObject.path(Keywords.TYPE)
    val wrapped = type.wrapped
    return when (wrapped) {
      is JsrArray -> {
        val typeArray = wrapped.values
            .map { it.unbox<String>() }
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
