package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.mergeCombine
import lang.json.JsonPath
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

  override fun merge(strategy: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Set<JsonSchemaType>>, report: MergeReport): Keyword<Set<JsonSchemaType>> {
    other as TypeKeyword
    if (this.types.isNotEmpty() && other.types.isNotEmpty() && other.types != this.types) {
      report += mergeCombine(path, keyword, this.types, other.types)
    }
    return TypeKeyword(this.types + other.types, this.disallowedTypes + other.disallowedTypes)
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
