package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertAll
import assertk.assertions.isNotNull
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.AllKeywords
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.JsonSchemas.draft3Schema
import io.mverse.jsonschema.JsonSchemas.draft4Schema
import io.mverse.jsonschema.JsonSchemas.draft6Schema
import io.mverse.jsonschema.JsonSchemas.draft7Schema
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
  fun <D : AllKeywords> testCommonKeywords(param: TestParam<AllKeywords>) {
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
      assert(schema.properties["selfRef"]).isNotNull()
      assert(schema.properties.getValue("selfRef")).isNotNull()
      assert(schema.patternProperties["^abc.*$"]).isNotNull()
      assert(schema.patternProperties.getValue("^abc.*$")).isNotNull()
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
      assert(schema.metaSchemaURI).isNotNull()
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
      assert(schema.metaSchemaURI).isNotNull()
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

      assert(schema.metaSchemaURI).isNotNull()
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

      assert(schema.metaSchemaURI).isNotNull()
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

  fun testingDraftSchemas(): Array<TestParam<Schema>> {
    val schema = kitchenSinkSchema
    val dogSchemaLocation = "https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/dog/jsonschema-draft6.json"
    return TestParam.builder<Schema>()
        .addTestParam("draft3", draft3Schema(schema))
        .addTestParam("draft4", draft4Schema(schema))
        .addTestParam("draft6", draft6Schema(schema))
        .addTestParam("draft7", draft7Schema(schema))
        .addTestParam("draft3Ref", RefJsonSchema(loader, schema.location, URI(dogSchemaLocation), schema).draft3())
        .addTestParam("draft4Ref", RefJsonSchema(loader, schema.location, URI(dogSchemaLocation), schema).draft4())
        .addTestParam("draft6Ref", RefJsonSchema(loader, schema.location, URI(dogSchemaLocation), schema).draft6())
        .addTestParam("draft7Ref", RefJsonSchema(loader, schema.location, URI(dogSchemaLocation), schema).draft7())
        .build()
  }

  fun paramsForDraft3Schema(): Array<TestParam<Draft3Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft3Schema>()
        .addTestParam("draft3Schema", draft3Schema(schema))
        .addTestParam("draft3RefSchema", RefJsonSchema(loader, schema.location,
                dogRefURI,
                schema).draft3())
        .build()
  }

  fun paramsForDraft4Schema(): Array<TestParam<Draft4Schema>> {
    val schema = kitchenSinkSchema

    return TestParam.builder<Draft4Schema>()
        .addTestParam("draft4Schema", draft4Schema(schema))
        .addTestParam("draft4RefSchema", RefJsonSchema(loader, schema.location,
                dogRefURI,
                schema).draft4())
        .build()
  }

  fun paramsForDraft6Schema(): Array<TestParam<Draft6Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft6Schema>()
        .addTestParam("draft6Schema", draft6Schema(schema))
        .addTestParam("draft6RefSchema", RefJsonSchema(loader, schema.location,
                dogRefURI,
                schema).draft6())
        .build()
  }

  fun paramsForDraft7Schema(): Array<TestParam<Draft7Schema>> {
    val schema = kitchenSinkSchema
    return TestParam.builder<Draft7Schema>()
        .addTestParam("draft7Schema", draft7Schema(schema))
        .addTestParam("draft7RefSchema", RefJsonSchema(loader, schema.location,
                dogRefURI,
                schema).draft7())
        .build()
  }

  companion object {

    private val schemaReader = JsonSchemas.createSchemaReader()
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
