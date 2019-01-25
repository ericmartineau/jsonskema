package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertAll
import assertk.assertions.isNotNull
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.unit.junit.TestParam
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.io.streams.asInput
import lang.net.URI
import org.junit.Test
import org.junit.runner.RunWith

val dogRefURI = URI("https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/dog/jsonschema-draft6.json")

@RunWith(JUnitParamsRunner::class)
class DraftSchemaTest {

  @Test
  @Parameters(method = "testingDraftSchemas")
  fun <D : DraftSchema<D>> testCommonKeywords(param: TestParam<DraftSchema<D>>) {
    val schema = param.get()
    assertAll {
      assert(schema.types).isNotNull()
      assert(schema.enumValues).isNotNull()
      assert(schema.defaultValue).isNotNull()
      assert(schema.format).isNotNull()
      assert(schema.minLength).isNotNull()
      assert(schema.maxLength).isNotNull()
      assert(schema.pattern).isNotNull()
      assert(schema.maximum).isNotNull()
      assert(schema.minimum).isNotNull()
      assert(schema.minItems).isNotNull()
      assert(schema.maxItems).isNotNull()
      assert(schema.allItemSchema).isNotNull()
      assert(schema.itemSchemas).isNotNull()
      assert(schema.additionalItemsSchema).isNotNull()
      assert(schema.properties).isNotNull()
      assert(schema.patternProperties).isNotNull()
      assert(schema.additionalPropertiesSchema).isNotNull()
      assert(schema.propertyDependencies).isNotNull()
      assert(schema.propertySchemaDependencies).isNotNull()
      assert(schema.requiresUniqueItems).isNotNull()
      assert(schema.findPropertySchema("selfRef")).isNotNull()
      assert(schema.getPropertySchema("selfRef")).isNotNull()
      assert(schema.findPatternSchema("^abc.*$")).isNotNull()
      assert(schema.getPatternSchema("^abc.*$")).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft4Schema")
  fun testDraft4Keywords(param: TestParam<Draft4Schema>) {
    val schema = param.get()
    assertAll {

      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assert(schema.location).isNotNull()
      assert(schema.schemaURI).isNotNull()
      assert(schema.id).isNotNull()
      assert(schema.title).isNotNull()
      assert(schema.description).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assert(schema.types).isNotNull()
      assert(schema.enumValues).isNotNull()
      assert(schema.defaultValue).isNotNull()
      assert(schema.notSchema).isNotNull()
      assert(schema.allOfSchemas).isNotNull()
      assert(schema.anyOfSchemas).isNotNull()
      assert(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assert(schema.format).isNotNull()
      assert(schema.minLength).isNotNull()
      assert(schema.maxLength).isNotNull()
      assert(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################
      assert(schema.multipleOf).isNotNull()
      assert(schema.maximum).isNotNull()
      assert(schema.minimum).isNotNull()
      assert(schema.isExclusiveMinimum).isNotNull()
      assert(schema.isExclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assert(schema.minItems).isNotNull()
      assert(schema.maxItems).isNotNull()
      assert(schema.allItemSchema).isNotNull()
      assert(schema.itemSchemas).isNotNull()
      assert(schema.isAllowAdditionalItems).isNotNull()
      assert(schema.additionalItemsSchema).isNotNull()
      assert(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assert(schema.isAllowAdditionalProperties).isNotNull()
      assert(schema.additionalPropertiesSchema).isNotNull()
      assert(schema.propertyDependencies).isNotNull()
      assert(schema.propertySchemaDependencies).isNotNull()
      assert(schema.maxProperties).isNotNull()
      assert(schema.minProperties).isNotNull()
      assert(schema.requiredProperties).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft3Schema")
  fun testDraft3Keywords(param: TestParam<Draft3Schema>) {
    val schema = param.get()
    assertAll {

      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assert(schema.location).isNotNull()
      assert(schema.schemaURI).isNotNull()
      assert(schema.id).isNotNull()
      assert(schema.title).isNotNull()
      assert(schema.description).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assert(schema.types).isNotNull()
      assert(schema.enumValues).isNotNull()
      assert(schema.defaultValue).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assert(schema.format).isNotNull()
      assert(schema.minLength).isNotNull()
      assert(schema.maxLength).isNotNull()
      assert(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################
      assert(schema.divisibleBy).isNotNull()
      assert(schema.maximum).isNotNull()
      assert(schema.minimum).isNotNull()
      assert(schema.isExclusiveMinimum).isNotNull()
      assert(schema.isExclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################
      assert(schema.minItems).isNotNull()
      assert(schema.maxItems).isNotNull()
      assert(schema.allItemSchema).isNotNull()
      assert(schema.itemSchemas).isNotNull()
      assert(schema.isAllowAdditionalItems).isNotNull()
      assert(schema.additionalItemsSchema).isNotNull()
      assert(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assert(schema.isAllowAdditionalProperties).isNotNull()
      assert(schema.additionalPropertiesSchema).isNotNull()
      assert(schema.propertyDependencies).isNotNull()
      assert(schema.propertySchemaDependencies).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft6Schema")
  fun testDraft6Keywords(param: TestParam<Draft6Schema>) {
    val schema = param.get()
    assertAll {
      assert(schema.location).isNotNull()
      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assert(schema.schemaURI).isNotNull()
      assert(schema.id).isNotNull()
      assert(schema.title).isNotNull()
      assert(schema.description).isNotNull()
      assert(schema.examples).isNotNull()
      assert(schema.definitions).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################
      assert(schema.types).isNotNull()
      assert(schema.enumValues).isNotNull()
      assert(schema.defaultValue).isNotNull()
      assert(schema.notSchema).isNotNull()
      assert(schema.constValue).isNotNull()
      assert(schema.allOfSchemas).isNotNull()
      assert(schema.anyOfSchemas).isNotNull()
      assert(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assert(schema.format).isNotNull()
      assert(schema.minLength).isNotNull()
      assert(schema.maxLength).isNotNull()
      assert(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################

      assert(schema.multipleOf).isNotNull()
      assert(schema.maximum).isNotNull()
      assert(schema.minimum).isNotNull()
      assert(schema.exclusiveMinimum).isNotNull()
      assert(schema.exclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assert(schema.minItems).isNotNull()
      assert(schema.maxItems).isNotNull()
      assert(schema.allItemSchema).isNotNull()
      assert(schema.itemSchemas).isNotNull()
      assert(schema.additionalItemsSchema).isNotNull()
      assert(schema.containsSchema).isNotNull()
      assert(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assert(schema.properties).isNotNull()
      assert(schema.patternProperties).isNotNull()
      assert(schema.additionalPropertiesSchema).isNotNull()
      assert(schema.propertyNameSchema).isNotNull()
      assert(schema.propertyDependencies).isNotNull()
      assert(schema.propertySchemaDependencies).isNotNull()
      assert(schema.maxProperties).isNotNull()
      assert(schema.minProperties).isNotNull()
      assert(schema.requiredProperties).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft7Schema")
  fun testDraft7Keywords(param: TestParam<Draft7Schema>) {
    val schema = param.get()
    assertAll {
      assert(schema.location).isNotNull()

      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assert(schema.schemaURI).isNotNull()
      assert(schema.id).isNotNull()
      assert(schema.title).isNotNull()
      assert(schema.description).isNotNull()
      assert(schema.examples).isNotNull()
      assert(schema.definitions).isNotNull()

      // ###################################
      // #### Draft 7 KEYWORDS #############
      // ###################################

      assert(schema.ifSchema).isNotNull()
      assert(schema.elseSchema).isNotNull()
      assert(schema.thenSchema).isNotNull()

      assert(schema.comment).isNotNull()
      assert(schema.isReadOnly).isNotNull()
      assert(schema.isWriteOnly).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assert(schema.types).isNotNull()
      assert(schema.enumValues).isNotNull()
      assert(schema.defaultValue).isNotNull()
      assert(schema.notSchema).isNotNull()
      assert(schema.constValue).isNotNull()
      assert(schema.allOfSchemas).isNotNull()
      assert(schema.anyOfSchemas).isNotNull()
      assert(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assert(schema.format).isNotNull()
      assert(schema.minLength).isNotNull()
      assert(schema.maxLength).isNotNull()
      assert(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################

      assert(schema.multipleOf).isNotNull()
      assert(schema.maximum).isNotNull()
      assert(schema.minimum).isNotNull()
      assert(schema.exclusiveMinimum).isNotNull()
      assert(schema.exclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assert(schema.minItems).isNotNull()
      assert(schema.maxItems).isNotNull()
      assert(schema.allItemSchema).isNotNull()
      assert(schema.itemSchemas).isNotNull()
      assert(schema.additionalItemsSchema).isNotNull()
      assert(schema.containsSchema).isNotNull()
      assert(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assert(schema.properties).isNotNull()
      assert(schema.patternProperties).isNotNull()
      assert(schema.additionalPropertiesSchema).isNotNull()
      assert(schema.propertyNameSchema).isNotNull()
      assert(schema.propertyDependencies).isNotNull()
      assert(schema.propertySchemaDependencies).isNotNull()
      assert(schema.maxProperties).isNotNull()
      assert(schema.minProperties).isNotNull()
      assert(schema.requiredProperties).isNotNull()
    }
  }

  fun testingDraftSchemas(): Array<TestParam<DraftSchema<*>>> {
    val schema = kitchenSinkSchema
    val dogSchemaLocation = "https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/dog/jsonschema-draft6.json"
    return TestParam.builder<DraftSchema<*>>()
        .addTestParam("draft3", Draft3SchemaImpl(loader, schema))
        .addTestParam("draft4", Draft4SchemaImpl(loader, schema))
        .addTestParam("draft6", Draft6SchemaImpl(loader, schema))
        .addTestParam("draft7", Draft7SchemaImpl(loader, schema))
        .addTestParam("draft3Ref", Draft3RefSchemaImpl(loader, schema.location, URI(dogSchemaLocation), schema.asDraft3()))
        .addTestParam("draft4Ref", Draft4RefSchemaImpl(loader, schema.location, URI(dogSchemaLocation), schema.asDraft4()))
        .addTestParam("draft6Ref", Draft6RefSchemaImpl(loader, schema.location,
            URI(dogSchemaLocation),
            schema.asDraft6()))
        .addTestParam("draft7Ref", Draft7RefSchemaImpl(loader, schema.location,
            URI(dogSchemaLocation),
            schema.asDraft7()))
        .build()
  }

  fun paramsForDraft3Schema(): Array<TestParam<Draft3Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft3Schema>()
        .addTestParam("draft3Schema", Draft3SchemaImpl(loader, schema))
        .addTestParam("draft3RefSchema", Draft3RefSchemaImpl(loader, schema.location,
            dogRefURI,
            schema.asDraft3()))
        .build()
  }

  fun paramsForDraft4Schema(): Array<TestParam<Draft4Schema>> {
    val schema = kitchenSinkSchema

    return TestParam.builder<Draft4Schema>()
        .addTestParam("draft4Schema", Draft4SchemaImpl(loader, schema))
        .addTestParam("draft4RefSchema", Draft4RefSchemaImpl(loader, schema.location,
            dogRefURI,
            schema.asDraft4()))
        .build()
  }

  fun paramsForDraft6Schema(): Array<TestParam<Draft6Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft6Schema>()
        .addTestParam("draft6Schema", Draft6SchemaImpl(loader, schema))
        .addTestParam("draft6RefSchema", Draft6RefSchemaImpl(loader, schema.location, dogRefURI, schema.asDraft6()))
        .build()
  }

  fun paramsForDraft7Schema(): Array<TestParam<Draft7Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft7Schema>()
        .addTestParam("draft7Schema", Draft7SchemaImpl(loader, schema))
        .addTestParam("draft7RefSchema", Draft7RefSchemaImpl(loader, schema.location, dogRefURI, schema.asDraft7()))
        .build()
  }

  companion object {

    private val schemaReader = JsonSchema.createSchemaReader()
    private val loader = schemaReader.loader
    private var kitchenSinkSchema: Schema

    init {
      val resourceAsStream = DraftSchemaTest::class.java.getResourceAsStream("/kitchen-sink-schema.json")
      val schemaJson = resourceAsStream.asInput().parseJsrObject()
      schemaReader.withPreloadedDocument(schemaJson)
      kitchenSinkSchema = schemaReader.readSchema(schemaJson)
    }
  }
}
