package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonSchema.schemaBuilder
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.enums.JsonSchemaType.INTEGER
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import lang.URI

object ValidationMocks {

  fun ValidationMocks.createTestValidator(schema: SchemaBuilder): SchemaValidator {
    return ValidationMocks.createTestValidator(schema.build())
  }

  fun ValidationMocks.createTestValidator(schema: Schema): SchemaValidator {
    return SchemaValidatorFactoryImpl.builder().build().createValidator(schema)
  }

  val mockArraySchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = ARRAY }
    }

  val mockBooleanSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = BOOLEAN }
    }

  fun mockBooleanSchema(id: URI): SchemaBuilder {
    return schemaBuilder(id) { type = BOOLEAN }
  }

  fun mockBooleanSchema(id: String): SchemaBuilder {
    return schemaBuilder(id) { type = BOOLEAN }
  }

  val mockIntegerSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = INTEGER }
    }

  val mockNullSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = NULL }
    }

  val mockNumberSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = NUMBER }
    }

  val mockObjectSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = OBJECT }
    }

  val mockSchema:SchemaBuilder get() = JsonSchemaBuilder()

  fun mockObjectSchema(id: String): SchemaBuilder {
    return schemaBuilder(id) { type = OBJECT }
  }

  fun mockObjectSchema(id: URI): SchemaBuilder {
    return schemaBuilder(id) { type = OBJECT }
  }

  fun createTestValidator(schema: Schema): SchemaValidator =
      JsonSchema.createValidatorFactory().createValidator(schema)

  val mockStringSchema: SchemaBuilder
    get() {
      return schemaBuilder.apply { type = STRING }
    }
}
