package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema

data class SchemaListKeyword(val schemas: List<Schema> = listOf())
  // Extends
  : SubschemaKeyword, JsonSchemaKeywordImpl<List<Schema>>(schemas) {
  override val subschemas: List<Schema> = schemas

  operator fun plus(schema:Schema): SchemaListKeyword {
    return this.copy(schemas = schemas + schema)
  }
}
