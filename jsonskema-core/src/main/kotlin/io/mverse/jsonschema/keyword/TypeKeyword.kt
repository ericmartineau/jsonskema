package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.toJsonArray

data class TypeKeyword(val types:Set<JsonSchemaType> = emptySet(),
                       val disallowedTypes: Set<JsonSchemaType> = JsonSchemaType.values().toSet().minus(types)) :
    JsonSchemaKeywordImpl<Set<JsonSchemaType>>() {

  override val value: Set<JsonSchemaType> = types

  constructor(first: JsonSchemaType, vararg additionalTypes: JsonSchemaType):
      this(types = setOf(first) + setOf(*additionalTypes))

  operator fun plus(type:JsonSchemaType):TypeKeyword {
    return TypeKeyword(types + type)
  }

  fun withAdditionalType(another: JsonSchemaType): TypeKeyword {
    return TypeKeyword(types + another)
  }

  override fun copy(value: Set<JsonSchemaType>): JsonSchemaKeyword<Set<JsonSchemaType>> {
    return this.copy(types = value)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    val type = Keywords.TYPE.key
    builder.apply {
      when {
        types.size == 1-> type to types.first().toString()
        else-> type to types.map { it.toString() }.toJsonArray()
      }
    }
  }

  override fun toString(): String {
    return types.toString()
  }
}
