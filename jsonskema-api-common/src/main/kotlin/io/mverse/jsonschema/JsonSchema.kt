package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.URI

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  val validatorFactory: SchemaValidatorFactory
  fun schemaBuilder(id: URI, block: SchemaBuilder.() -> Unit = {}): SchemaBuilder
  fun schemaBuilder(id: String, block: SchemaBuilder.() -> Unit = {}): SchemaBuilder
  val schemaBuilder: SchemaBuilder
}

fun JsonSchema.getValidator(schema: Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)

inline fun <reified D : DraftSchema<D>> JsonSchema.versionedSchema(id: String? = null, noinline block: SchemaBuilder.() -> Unit): D {

  val schema = schema(id, block)

  return when (D::class) {
    Draft7Schema::class -> schema.asDraft7() as D
    Draft6Schema::class -> schema.asDraft6() as D
    Draft4Schema::class -> schema.asDraft4() as D
    Draft3Schema::class -> schema.asDraft3() as D
    else -> this as D
  }
}

fun JsonSchema.schema(id: String? = null, block: SchemaBuilder.() -> Unit): Schema =
    when (id) {
      null -> schemaBuilder.invoke(block)
      else -> schemaBuilder(id, block).build()
    }

