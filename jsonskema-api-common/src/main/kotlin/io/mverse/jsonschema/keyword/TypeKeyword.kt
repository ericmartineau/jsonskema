package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.json.MutableJsrObject

data class TypeKeyword(val types: Set<JsonSchemaType> = emptySet(),
                       val disallowedTypes: Set<JsonSchemaType> = JsonSchemaType.values().toSet().minus(types)) :
    KeywordImpl<Set<JsonSchemaType>>() {
  override val value: Set<JsonSchemaType> = types

  constructor(first: JsonSchemaType, vararg additionalTypes: JsonSchemaType) :
      this(types = setOf(first) + setOf(*additionalTypes))

  operator fun plus(type: JsonSchemaType): TypeKeyword {
    return TypeKeyword(types + type)
  }

  fun withAdditionalType(another: JsonSchemaType): TypeKeyword {
    return TypeKeyword(types + another)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    val type = Keywords.TYPE.key
    builder.apply {
      when {
        types.size == 1 -> type *= types.first().name.toLowerCase()
        else -> type *= types.map { it.name.toLowerCase() }
      }
    }
  }

  override fun toString(): String {
    return types.toString()
  }

  override fun withValue(value: Set<JsonSchemaType>): Keyword<Set<JsonSchemaType>> = this.copy(types = value)
}
