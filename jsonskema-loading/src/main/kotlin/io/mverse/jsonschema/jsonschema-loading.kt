package io.mverse.jsonschema

import io.mverse.jsonschema.loading.SchemaLoaderImpl
import io.mverse.jsonschema.loading.SchemaReader

internal val defaultSchemaReader = SchemaLoaderImpl()
val JsonSchema.schemaReader:SchemaReader get() = defaultSchemaReader

fun JsonSchema.schemaReader(): SchemaReader {
  return defaultSchemaReader
}

fun JsonSchema.createSchemaReader(): SchemaReader {
  return SchemaLoaderImpl()
}




