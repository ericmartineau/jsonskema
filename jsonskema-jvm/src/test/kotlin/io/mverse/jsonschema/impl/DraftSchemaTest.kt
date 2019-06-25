package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertAll
import assertk.assertThat
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
      assertThat(schema.types).isNotNull()
      assertThat(schema.enumValues).isNotNull()
      assertThat(schema.defaultValue).isNotNull()
      assertThat(schema.format).isNotNull()
      assertThat(schema.minLength).isNotNull()
      assertThat(schema.maxLength).isNotNull()
      assertThat(schema.pattern).isNotNull()
      assertThat(schema.maximum).isNotNull()
      assertThat(schema.minimum).isNotNull()
      assertThat(schema.minItems).isNotNull()
      assertThat(schema.maxItems).isNotNull()
      assertThat(schema.allItemSchema).isNotNull()
      assertThat(schema.itemSchemas).isNotNull()
      assertThat(schema.additionalItemsSchema).isNotNull()
      assertThat(schema.properties).isNotNull()
      assertThat(schema.patternProperties).isNotNull()
      assertThat(schema.additionalPropertiesSchema).isNotNull()
      assertThat(schema.propertyDependencies).isNotNull()
      assertThat(schema.propertySchemaDependencies).isNotNull()
      assertThat(schema.requiresUniqueItems).isNotNull()
      assertThat(schema.properties["selfRef"]).isNotNull()
      assertThat(schema.properties.getValue("selfRef")).isNotNull()
      assertThat(schema.patternProperties["^abc.*$"]).isNotNull()
      assertThat(schema.patternProperties.getValue("^abc.*$")).isNotNull()
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

      assertThat(schema.location).isNotNull()
      assertThat(schema.metaSchemaURI).isNotNull()
      assertThat(schema.id).isNotNull()
      assertThat(schema.title).isNotNull()
      assertThat(schema.description).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assertThat(schema.types).isNotNull()
      assertThat(schema.enumValues).isNotNull()
      assertThat(schema.defaultValue).isNotNull()
      assertThat(schema.notSchema).isNotNull()
      assertThat(schema.allOfSchemas).isNotNull()
      assertThat(schema.anyOfSchemas).isNotNull()
      assertThat(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assertThat(schema.format).isNotNull()
      assertThat(schema.minLength).isNotNull()
      assertThat(schema.maxLength).isNotNull()
      assertThat(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################
      assertThat(schema.multipleOf).isNotNull()
      assertThat(schema.maximum).isNotNull()
      assertThat(schema.minimum).isNotNull()
      assertThat(schema.isExclusiveMinimum).isNotNull()
      assertThat(schema.isExclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assertThat(schema.minItems).isNotNull()
      assertThat(schema.maxItems).isNotNull()
      assertThat(schema.allItemSchema).isNotNull()
      assertThat(schema.itemSchemas).isNotNull()
      assertThat(schema.isAllowAdditionalItems).isNotNull()
      assertThat(schema.additionalItemsSchema).isNotNull()
      assertThat(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assertThat(schema.isAllowAdditionalProperties).isNotNull()
      assertThat(schema.additionalPropertiesSchema).isNotNull()
      assertThat(schema.propertyDependencies).isNotNull()
      assertThat(schema.propertySchemaDependencies).isNotNull()
      assertThat(schema.maxProperties).isNotNull()
      assertThat(schema.minProperties).isNotNull()
      assertThat(schema.requiredProperties).isNotNull()
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

      assertThat(schema.location).isNotNull()
      assertThat(schema.metaSchemaURI).isNotNull()
      assertThat(schema.id).isNotNull()
      assertThat(schema.title).isNotNull()
      assertThat(schema.description).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assertThat(schema.types).isNotNull()
      assertThat(schema.enumValues).isNotNull()
      assertThat(schema.defaultValue).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assertThat(schema.format).isNotNull()
      assertThat(schema.minLength).isNotNull()
      assertThat(schema.maxLength).isNotNull()
      assertThat(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################
      assertThat(schema.divisibleBy).isNotNull()
      assertThat(schema.maximum).isNotNull()
      assertThat(schema.minimum).isNotNull()
      assertThat(schema.isExclusiveMinimum).isNotNull()
      assertThat(schema.isExclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################
      assertThat(schema.minItems).isNotNull()
      assertThat(schema.maxItems).isNotNull()
      assertThat(schema.allItemSchema).isNotNull()
      assertThat(schema.itemSchemas).isNotNull()
      assertThat(schema.isAllowAdditionalItems).isNotNull()
      assertThat(schema.additionalItemsSchema).isNotNull()
      assertThat(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assertThat(schema.isAllowAdditionalProperties).isNotNull()
      assertThat(schema.additionalPropertiesSchema).isNotNull()
      assertThat(schema.propertyDependencies).isNotNull()
      assertThat(schema.propertySchemaDependencies).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft6Schema")
  fun testDraft6Keywords(param: TestParam<Draft6Schema>) {
    val schema = param.get()
    assertAll {
      assertThat(schema.location).isNotNull()
      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assertThat(schema.metaSchemaURI).isNotNull()
      assertThat(schema.id).isNotNull()
      assertThat(schema.title).isNotNull()
      assertThat(schema.description).isNotNull()
      assertThat(schema.examples).isNotNull()
      assertThat(schema.definitions).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################
      assertThat(schema.types).isNotNull()
      assertThat(schema.enumValues).isNotNull()
      assertThat(schema.defaultValue).isNotNull()
      assertThat(schema.notSchema).isNotNull()
      assertThat(schema.constValue).isNotNull()
      assertThat(schema.allOfSchemas).isNotNull()
      assertThat(schema.anyOfSchemas).isNotNull()
      assertThat(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assertThat(schema.format).isNotNull()
      assertThat(schema.minLength).isNotNull()
      assertThat(schema.maxLength).isNotNull()
      assertThat(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################

      assertThat(schema.multipleOf).isNotNull()
      assertThat(schema.maximum).isNotNull()
      assertThat(schema.minimum).isNotNull()
      assertThat(schema.exclusiveMinimum).isNotNull()
      assertThat(schema.exclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assertThat(schema.minItems).isNotNull()
      assertThat(schema.maxItems).isNotNull()
      assertThat(schema.allItemSchema).isNotNull()
      assertThat(schema.itemSchemas).isNotNull()
      assertThat(schema.additionalItemsSchema).isNotNull()
      assertThat(schema.containsSchema).isNotNull()
      assertThat(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assertThat(schema.properties).isNotNull()
      assertThat(schema.patternProperties).isNotNull()
      assertThat(schema.additionalPropertiesSchema).isNotNull()
      assertThat(schema.propertyNameSchema).isNotNull()
      assertThat(schema.propertyDependencies).isNotNull()
      assertThat(schema.propertySchemaDependencies).isNotNull()
      assertThat(schema.maxProperties).isNotNull()
      assertThat(schema.minProperties).isNotNull()
      assertThat(schema.requiredProperties).isNotNull()
    }
  }

  @Test
  @Parameters(method = "paramsForDraft7Schema")
  fun testDraft7Keywords(param: TestParam<Draft7Schema>) {
    val schema = param.get()
    assertAll {
      assertThat(schema.location).isNotNull()

      // ###################################
      // #### Meta KEYWORDS ##############
      // ###################################

      assertThat(schema.metaSchemaURI).isNotNull()
      assertThat(schema.id).isNotNull()
      assertThat(schema.title).isNotNull()
      assertThat(schema.description).isNotNull()
      assertThat(schema.examples).isNotNull()
      assertThat(schema.definitions).isNotNull()

      // ###################################
      // #### Draft 7 KEYWORDS #############
      // ###################################

      assertThat(schema.ifSchema).isNotNull()
      assertThat(schema.elseSchema).isNotNull()
      assertThat(schema.thenSchema).isNotNull()

      assertThat(schema.comment).isNotNull()
      assertThat(schema.isReadOnly).isNotNull()
      assertThat(schema.isWriteOnly).isNotNull()

      // ###################################
      // #### Shared KEYWORDS ##############
      // ###################################

      assertThat(schema.types).isNotNull()
      assertThat(schema.enumValues).isNotNull()
      assertThat(schema.defaultValue).isNotNull()
      assertThat(schema.notSchema).isNotNull()
      assertThat(schema.constValue).isNotNull()
      assertThat(schema.allOfSchemas).isNotNull()
      assertThat(schema.anyOfSchemas).isNotNull()
      assertThat(schema.oneOfSchemas).isNotNull()

      // ###################################
      // #### String KEYWORDS ##############
      // ###################################

      assertThat(schema.format).isNotNull()
      assertThat(schema.minLength).isNotNull()
      assertThat(schema.maxLength).isNotNull()
      assertThat(schema.pattern).isNotNull()

      // ###################################
      // #### NUMBER KEYWORDS ##############
      // ###################################

      assertThat(schema.multipleOf).isNotNull()
      assertThat(schema.maximum).isNotNull()
      assertThat(schema.minimum).isNotNull()
      assertThat(schema.exclusiveMinimum).isNotNull()
      assertThat(schema.exclusiveMaximum).isNotNull()

      // ###################################
      // #### ARRAY KEYWORDS  ##############
      // ###################################

      assertThat(schema.minItems).isNotNull()
      assertThat(schema.maxItems).isNotNull()
      assertThat(schema.allItemSchema).isNotNull()
      assertThat(schema.itemSchemas).isNotNull()
      assertThat(schema.additionalItemsSchema).isNotNull()
      assertThat(schema.containsSchema).isNotNull()
      assertThat(schema.requiresUniqueItems).isNotNull()

      // ###################################
      // #### OBJECT KEYWORDS  ##############
      // ###################################

      assertThat(schema.properties).isNotNull()
      assertThat(schema.patternProperties).isNotNull()
      assertThat(schema.additionalPropertiesSchema).isNotNull()
      assertThat(schema.propertyNameSchema).isNotNull()
      assertThat(schema.propertyDependencies).isNotNull()
      assertThat(schema.propertySchemaDependencies).isNotNull()
      assertThat(schema.maxProperties).isNotNull()
      assertThat(schema.minProperties).isNotNull()
      assertThat(schema.requiredProperties).isNotNull()
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
