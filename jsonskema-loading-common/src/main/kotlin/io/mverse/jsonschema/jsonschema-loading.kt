package io.mverse.jsonschema

import io.mverse.jsonschema.loading.SchemaLoaderImpl
import io.mverse.jsonschema.loading.SchemaReader
import lang.Name

internal val defaultSchemaReader = SchemaLoaderImpl()

@Name("readJsonSchema")
fun readSchema(schema: String): Schema {
  return defaultSchemaReader.readSchema(schema)
}

fun JsonSchema.createSchemaReader(): SchemaReader {
  return SchemaLoaderImpl()
}




