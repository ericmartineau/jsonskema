package io.mverse.jsonschema

class KeyMissingException(schemaLocation: SchemaLocation, val key: String) :
    SchemaException(schemaLocation.jsonPointerFragment, "Missing value at key [$key]")

fun missingProperty(schema: DraftSchema, key: String): Nothing {
  throw KeyMissingException(schema.location, key)
}
