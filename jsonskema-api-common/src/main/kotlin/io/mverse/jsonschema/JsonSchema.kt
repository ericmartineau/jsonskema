package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.net.URI
import lang.net.toURI
import kotlin.reflect.KClass

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  var validatorFactory: SchemaValidatorFactory
  var schemaReader: SchemaReader
  val schemaLoader: SchemaLoader

  fun schemaBuilder(id: URI, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): MutableSchema
  fun schemaBuilder(location: SchemaLocation, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): MutableSchema

  fun schema(id: URI, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): Schema
  fun schema(location: SchemaLocation, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): Schema

  fun createSchemaBuilder(location: SchemaLocation, loader: SchemaLoader = schemaReader.loader): MutableSchema
  fun createSchemaBuilder(id: URI, loader: SchemaLoader = schemaReader.loader): MutableSchema
  inline fun <reified D:DraftSchema> draftSchema(schema:Schema):D
  fun <D:DraftSchema> draftSchema(schema:Schema, type:KClass<D>): D
  fun draft3Schema(schema:Schema): Draft3Schema
  fun draft4Schema(schema:Schema): Draft4Schema
  fun draft6Schema(schema:Schema): Draft6Schema
  fun draft7Schema(schema:Schema): Draft7Schema
}

fun JsonSchema.schema(id: String, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): Schema =
    JsonSchema.schema(id.toURI(), loader, block)

fun JsonSchema.schemaBuilder(id: String, loader: SchemaLoader = schemaReader.loader, block: MutableSchema.() -> Unit = {}): MutableSchema =
    JsonSchema.schemaBuilder(id.toURI(), loader, block)

fun JsonSchema.getValidator(schema: Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)
