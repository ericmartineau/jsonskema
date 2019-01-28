package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft4
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.impl.DraftSchemaImpl
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.exception.illegalArgument
import lang.net.URI
import lang.suppress.Suppressions
import kotlin.reflect.KClass

actual object JsonSchema {
  actual var validatorFactory: SchemaValidatorFactory = defaultValidatorFactory
  actual var schemaReader: SchemaReader = defaultSchemaReader
  actual val schemaLoader: SchemaLoader get() = defaultSchemaReader.loader
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, loader: SchemaLoader, block: MutableSchema.() -> Unit): MutableSchema = MutableJsonSchema(loader, id = id).apply(block)
//  actual fun schemaBuilder(loader: SchemaLoader, block: MutableSchema.() -> Unit): MutableSchema = createSchemaBuilder(loader).apply(block)
  actual fun createSchemaBuilder(location: SchemaLocation, loader: SchemaLoader): MutableSchema = MutableJsonSchema(loader, location)
  actual fun createSchemaBuilder(id: URI, loader: SchemaLoader): MutableSchema = MutableJsonSchema(loader, id)

  actual fun schema(id: URI, loader: SchemaLoader, block: MutableSchema.() -> Unit): Schema =
      MutableJsonSchema(loader, id).apply { block() }.build()

  actual fun schema(location: SchemaLocation, loader: SchemaLoader, block: MutableSchema.() -> Unit): Schema =
      MutableJsonSchema(loader, location).apply(block).build()


  actual inline fun <reified D : DraftSchema> draftSchema(schema: Schema): D =
      JsonSchema.draftSchema(schema, D::class)

  actual fun <D : DraftSchema> draftSchema(schema: Schema, type: KClass<D>): D {
    @Suppress(Suppressions.UNCHECKED_CAST)
    return when (type) {
      Draft3Schema::class -> DraftSchemaImpl(Draft3Schema::class, schema.withVersion(Draft3))
      Draft4Schema::class -> DraftSchemaImpl(Draft4Schema::class, schema.withVersion(Draft4))
      Draft6Schema::class -> DraftSchemaImpl(Draft6Schema::class, schema.withVersion(Draft6))
      Draft7Schema::class -> DraftSchemaImpl(Draft7Schema::class, schema.withVersion(Draft7))
      else -> illegalArgument("Can't infer schema version from $type")
    } as D
  }

  actual fun draft3Schema(schema: Schema): Draft3Schema = draftSchema(schema)

  actual fun draft4Schema(schema: Schema): Draft4Schema = draftSchema(schema)

  actual fun draft6Schema(schema: Schema): Draft6Schema = draftSchema(schema)

  actual fun draft7Schema(schema: Schema): Draft7Schema = draftSchema(schema)
  actual fun schemaBuilder(location: SchemaLocation, loader: SchemaLoader, block: MutableSchema.() -> Unit): MutableSchema {
    return MutableJsonSchema(schemaLoader = loader, location = location).apply(block)
  }
}
