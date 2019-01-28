package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonSchema.schemaBuilder
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.enums.JsonSchemaType.INTEGER
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.schemaBuilder
import lang.net.URI

object ValidationMocks {

  fun ValidationMocks.createTestValidator(schema: MutableSchema): SchemaValidator {
    return ValidationMocks.createTestValidator(schema.build())
  }

  fun ValidationMocks.createTestValidator(schema: Schema): SchemaValidator {
    return SchemaValidatorFactoryImpl.builder().build().createValidator(schema)
  }

  val mockArraySchema: MutableSchema
    get() {
      return schemaBuilder { type = ARRAY }
    }

  val mockBooleanSchema: MutableSchema
    get() {
      return schemaBuilder { type = BOOLEAN }
    }

  fun mockBooleanSchema(id: URI): MutableSchema {
    return schemaBuilder(id = id) { type = BOOLEAN }
  }

  fun mockBooleanSchema(id: String): MutableSchema {
    return JsonSchema.schemaBuilder(id = id) { type = BOOLEAN }
  }

  val mockIntegerSchema: MutableSchema
    get() {
      return schemaBuilder { type = INTEGER }
    }

  val mockNullSchema: MutableSchema
    get() {
      return schemaBuilder { type = NULL }
    }

  val mockNumberSchema: MutableSchema
    get() {
      return schemaBuilder { type = NUMBER }
    }

  val mockObjectSchema: MutableSchema
    get() {
      return schemaBuilder { type = OBJECT }
    }

  val mockSchema: MutableSchema get() = MutableJsonSchema(JsonSchema.schemaLoader)

  fun mockObjectSchema(id: String): MutableSchema {
    return JsonSchema.schemaBuilder(id = id) { type = OBJECT }
  }

  fun mockObjectSchema(id: URI): MutableSchema {
    return schemaBuilder(id = id) { type = OBJECT }
  }

  fun createTestValidator(schema: Schema): SchemaValidator =
      JsonSchema.createValidatorFactory().createValidator(schema)

  val mockStringSchema: MutableSchema
    get() {
      return schemaBuilder { type = STRING }
    }
}
