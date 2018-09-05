package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.schemaBuilder
import lang.URI

object ValidationMocks {

  fun ValidationMocks.createTestValidator(schema :SchemaBuilder): SchemaValidator {
    return ValidationMocks.createTestValidator(schema.build())
  }

  fun ValidationMocks.createTestValidator(schema: Schema): SchemaValidator {
    return SchemaValidatorFactoryImpl.builder().build().createValidator(schema)
  }

  fun mockArraySchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.ARRAY)
  }

  fun mockBooleanSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.BOOLEAN)
  }

  fun mockBooleanSchema(id: URI) :SchemaBuilder {
     return JsonSchema.schemaBuilder(id).type(JsonSchemaType.BOOLEAN)
  }

  fun mockBooleanSchema(id: String) :SchemaBuilder {
     return JsonSchema.schemaBuilder(id).type(JsonSchemaType.BOOLEAN)
  }

  fun mockIntegerSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.INTEGER)
  }

  fun mockNullSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.NULL)
  }

  fun mockNumberSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.NUMBER)
  }

  fun mockObjectSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.OBJECT)
  }

  fun mockObjectSchema(id: String) :SchemaBuilder {
     return JsonSchema.schemaBuilder(id).type(JsonSchemaType.OBJECT)
  }

  fun mockObjectSchema(id: URI) :SchemaBuilder {
     return JsonSchema.schemaBuilder(id).type(JsonSchemaType.OBJECT)
  }

  fun mockSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder()
  }

  fun mockStringSchema() :SchemaBuilder {
     return JsonSchema.schemaBuilder().type(JsonSchemaType.STRING)
  }
}
