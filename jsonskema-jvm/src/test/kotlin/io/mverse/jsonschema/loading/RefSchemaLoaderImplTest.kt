package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import kotlinx.serialization.json.JsonObject
import lang.URI
import lang.resolveUri
import org.junit.Before
import org.junit.Test

class RefSchemaLoaderImplTest {

  private lateinit var refSchemaLoader: RefSchemaLoader
  private lateinit var accountProfileJson: JsonObject
  private lateinit var documentURI: URI

  @Before
  fun before() {
    val schemaLoader = JsonSchema.createSchemaReader() as SchemaLoaderImpl
    this.refSchemaLoader = schemaLoader.refSchemaLoader
    accountProfileJson = readResource("mverse-account-profile.json")
    documentURI = URI("http://schema.mverse.io/mverse-account-profile.json")
  }

  @Test
  fun resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathPlusSchemeWithLocalId_ThenReturnSomething() {
    val relativeURI = URI("http://schema.mverse.io/mverse-account-profile.json#platformName")

    val absoluteURI = documentURI.resolveUri(relativeURI)

    val schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, LoadingReport())
    assert(schema).isNotNull()
  }

  @Test
  fun resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathWithLocalId_ThenReturnSomething() {
    val relativeURI = URI("/mverse-account-profile.json#platformName")

    val absoluteURI = documentURI.resolveUri(relativeURI)

    val schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, LoadingReport())
    assert(schema).isNotNull()
  }

  @Test
  fun resolve_WhenReferenceIsInsideDocument_AndRefHasLocalIdFragment_ThenReturnSomething() {
    val relativeURI = URI("#platformName")

    val absoluteURI = documentURI.resolveUri(relativeURI)

    val schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, LoadingReport())
    assert(schema).isNotNull()
  }

  @Test
  fun resolve_WhenReferenceIsInsideDocument_AndRefHasRelativePathWithLocalId_ThenReturnSomething() {
    val relativeURI = URI("mverse-account-profile.json#platformName")

    val absoluteURI = documentURI.resolveUri(relativeURI)

    val schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, LoadingReport())
    assert(schema).isNotNull()
  }

  @Test
  fun resolve_WhenReferenceIsOutsideDocument_ThenReturnEmpty() {
    val relativeURI = URI("/primitives.json#/definitions/color")

    val absoluteURI = documentURI.resolveUri(relativeURI)

    val schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, LoadingReport())
    assert(schema).isNull()
  }

  private fun readResource(relativePath: String): JsonObject {
    return JsonSchema.resourceLoader(this::class).readJsonObject(relativePath)
  }
}
