package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.validation.SchemaValidator
import lang.URI

object ValidationMocks {

  fun createTestValidator(schema: SchemaBuilder): SchemaValidator {
    return createTestValidator(schema.build())
  }

  fun createTestValidator(schema: Schema): SchemaValidator {
    return JsonSchema.createValidatorFactory().createValidator(schema)
  }

  fun mockArraySchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.ARRAY)
  }

  fun mockBooleanSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.BOOLEAN)
  }

  fun mockBooleanSchema(id: URI): SchemaBuilder {
    return JsonSchemaBuilder(id).type(JsonSchemaType.BOOLEAN)
  }

  fun mockBooleanSchema(id: String): SchemaBuilder {
    return JsonSchema.schemaBuilder(id).type(JsonSchemaType.BOOLEAN)
  }

  fun mockIntegerSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.INTEGER)
  }

  fun mockNullSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.NULL)
  }

  fun mockNumberSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.NUMBER)
  }

  fun mockObjectSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.OBJECT)
  }

  fun mockObjectSchema(id: String): SchemaBuilder {
    return JsonSchema.schemaBuilder(id).type(JsonSchemaType.OBJECT)
  }

  fun mockObjectSchema(id: URI): SchemaBuilder {
    return JsonSchemaBuilder(id).type(JsonSchemaType.OBJECT)
  }

  fun mockSchema(): SchemaBuilder {
    return JsonSchemaBuilder()
  }

  fun mockStringSchema(): SchemaBuilder {
    return JsonSchemaBuilder().type(JsonSchemaType.STRING)
  }
}
