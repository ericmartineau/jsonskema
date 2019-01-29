package io.mverse.jsonschema

import io.mverse.jsonschema.loading.SchemaLoaderImpl
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.loading.reference.JsonSchemaCache
import lang.Name

internal val defaultCache = JsonSchemaCache()
internal val defaultSchemaReader = SchemaLoaderImpl(defaultCache)

@Name("readJsonSchema")
fun readSchema(schema: String): Schema {
  return defaultSchemaReader.readSchema(schema)
}

fun JsonSchemas.createSchemaReader(): SchemaReader {
  return SchemaLoaderImpl(JsonSchemaCache())
}




