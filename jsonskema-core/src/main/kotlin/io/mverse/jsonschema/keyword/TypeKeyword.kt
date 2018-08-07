package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import lang.json.asJsonArray

data class TypeKeyword(val types:Set<JsonSchemaType> = emptySet(),
                       val disallowedTypes: Set<JsonSchemaType> = JsonSchemaType.values().toSet().minus(types)) :
    JsonSchemaKeywordImpl<Set<JsonSchemaType>>(types) {

  constructor(first: JsonSchemaType, vararg additionalTypes: JsonSchemaType):
      this(types = setOf(first) + setOf(*additionalTypes))

  operator fun plus(type:JsonSchemaType):TypeKeyword {
    return TypeKeyword(types + type)
  }

  fun withAdditionalType(another: JsonSchemaType): TypeKeyword {
    return TypeKeyword(types + another)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    val type = Keywords.TYPE.key
    builder.apply {
      when {
        types.size == 1-> type to types.first().toString()
        else-> type to types.map { it.toString() }.asJsonArray()
      }
    }
  }

  override fun toString(): String {
    return types.toString()
  }
}
