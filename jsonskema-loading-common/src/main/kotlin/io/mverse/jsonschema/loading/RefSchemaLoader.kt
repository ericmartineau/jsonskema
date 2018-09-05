package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.SchemaLocation.Companion.BLANK_URI
import io.mverse.jsonschema.SchemaLocation.Companion.ROOT_URI
import io.mverse.jsonschema.utils.JsonUtils
import io.mverse.jsonschema.utils.isJsonPointer
import io.mverse.jsonschema.utils.withoutFragment
import kotlinx.serialization.json.JsonNull
import lang.URI
import lang.fragment
import lang.json.get
import lang.resolveUri
import lang.scheme

data class RefSchemaLoader(val documentClient: JsonDocumentClient, val schemaLoader: SchemaLoader) {


  fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: kotlinx.serialization.json.JsonObject?, report: LoadingReport): Schema {
    // Cache ahead to deal with any infinite recursion.
    val currentLocation = referencedFrom.location
    schemaLoader.withPreloadedSchema(referencedFrom)

    // Make sure we're dealing with an absolute URI
    val absoluteReferenceURI = currentLocation.resolutionScope.resolveUri(refURI)
    val documentURI = currentLocation.documentURI

    // Look for a cache schema at this URI
    val cachedSchema = schemaLoader.findLoadedSchema(absoluteReferenceURI)
    if (cachedSchema != null) {
      return cachedSchema
    }

    val schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument, report)
        ?: findRefInRemoteDocument(absoluteReferenceURI, report)
    val refSchema = schemaBuilder.build()
    schemaLoader.withPreloadedSchema(refSchema)
    return refSchema
  }

  internal fun loadDocument(referenceURI: URI): kotlinx.serialization.json.JsonObject {
    val remoteDocumentURI = referenceURI.withoutFragment()

    val foundDoc = documentClient.findLoadedDocument(remoteDocumentURI)
    return foundDoc
        ?: {
          val scheme = referenceURI.scheme?.toLowerCase() ?: ""
          if (!scheme.startsWith("http")) {
            throw SchemaException(referenceURI, "Couldn't resolve ref within document, but can't readSchema non-http scheme: %s", scheme)
          }

          // Load document remotely
          val document = documentClient.fetchDocument(remoteDocumentURI)
          documentClient.registerLoadedDocument(remoteDocumentURI, document)
          document
        }()
  }

  internal fun findRefInRemoteDocument(referenceURI: URI, report: LoadingReport): SchemaBuilder {
    val remoteDocumentURI = referenceURI.resolveUri("#")
    val remoteDocument = loadDocument(remoteDocumentURI)
    return findRefInDocument(remoteDocumentURI, referenceURI, remoteDocument, report)
        ?: throw SchemaException(referenceURI, "Unable to locate fragment: \n\tFragment: '#%s' in document\n\tDocument:'%s'", referenceURI.fragment ?: "", remoteDocument)
  }

  internal fun findRefInDocument(documentURI: URI, referenceURI: URI, parentDocument: kotlinx.serialization.json.JsonObject?,
                                 report: LoadingReport): SchemaBuilder? {
    var documentURIVar = documentURI
    var parentDocumentVar = parentDocument
    if (parentDocumentVar == null) {
      parentDocumentVar = loadDocument(referenceURI)
    }

    //Remove any fragments from the parentDocument URI
    documentURIVar = documentURIVar.withoutFragment()

    // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
    // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
    val relativeURL = documentURIVar.relativize(referenceURI)

    val pathWithinDocument: JsonPath?
    if (relativeURL.equals(ROOT_URI) || relativeURL.equals(BLANK_URI)) {
      // The parentDocument is the target
      pathWithinDocument = JsonPath.rootPath()
    } else if (relativeURL.isJsonPointer()) {
      //This is a json fragment
      pathWithinDocument = JsonPath.parseFromURIFragment(relativeURL)
    } else {
      //This must be a reference $id somewhere in the parentDocument.
      pathWithinDocument = documentClient.resolveSchemaWithinDocument(documentURIVar, referenceURI, parentDocumentVar)
    }
    if (pathWithinDocument != null) {

      val jsonElement = parentDocumentVar[pathWithinDocument]

      val schemaObject: kotlinx.serialization.json.JsonObject = when (jsonElement) {
        JsonNull->throw SchemaException(referenceURI, "Unable to resolve '#$relativeURL' as JSON Pointer within '$documentURIVar'")
        is kotlinx.serialization.json.JsonObject -> jsonElement.jsonObject
        else-> throw SchemaException(referenceURI, "Expecting JsonObject at #$relativeURL, but found ${jsonElement.type}")
      }

      val foundId = JsonUtils.extractIdFromObject(schemaObject)
      val fetchedDocumentLocation = SchemaLocation.refLocation(documentURIVar, foundId, pathWithinDocument)
      val schemaJson = JsonValueWithPath.fromJsonValue(parentDocumentVar, schemaObject, fetchedDocumentLocation)
      return schemaLoader.subSchemaBuilder(schemaJson, parentDocumentVar, report)
    }
    return null
  }
}
