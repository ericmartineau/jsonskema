package io.mverse.jsonschema.loading

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
import lang.json.JsonKey
import lang.json.JsonPath
import lang.json.JsrNull
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.KtObject
import lang.json.get
import lang.json.getOrNull
import lang.json.type
import lang.net.URI
import lang.net.fragment
import lang.net.resolveUri
import lang.net.scheme

data class RefSchemaLoader(val documentClient: JsonDocumentClient, val schemaLoader: SchemaLoader) {

  fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: lang.json.JsrObject?, report: LoadingReport): Schema {
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

  internal fun loadDocument(referenceURI: URI): lang.json.JsrObject {
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
        ?: throw SchemaException(referenceURI, "Unable to locate fragment: \n\tFragment: '#%s' in document\n\tDocument:'%s'", referenceURI.fragment
            ?: "", remoteDocument)
  }

  fun findRefInDocument(documentURI: URI, referenceURI: URI, parentDocument: JsrObject?,
                        report: LoadingReport): SchemaBuilder? {
    var documentURIVar = documentURI
    val parentDocumentVar = parentDocument ?: loadDocument(referenceURI)

    //Remove any fragments from the parentDocument URI
    documentURIVar = documentURIVar.withoutFragment()

    // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
    // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
    val relativeURL = documentURIVar.relativize(referenceURI)

    val pathWithinDocument: JsonPath?
    if (relativeURL.equals(ROOT_URI) || relativeURL.equals(BLANK_URI)) {
      // The parentDocument is the target
      pathWithinDocument = JsonPath.rootPath
    } else if (relativeURL.isJsonPointer()) {
      //This is a json fragment
      pathWithinDocument = JsonPath.fromURI(relativeURL)
    } else {
      //This must be a reference $id somewhere in the parentDocument.
      pathWithinDocument = documentClient.resolveSchemaWithinDocument(documentURIVar, referenceURI, parentDocumentVar)
    }
    if (pathWithinDocument != null) {

      val jsrValue: JsrValue = parentDocumentVar.getOrNull(pathWithinDocument) ?: JsrNull

      val schemaObject: JsrObject = when (jsrValue) {
        JsrNull -> throw SchemaException(referenceURI, "Unable to resolve '#$relativeURL' as JSON Pointer within '$documentURIVar'")
        is JsrObject -> jsrValue
        else -> throw SchemaException(referenceURI, "Expecting JsrObject at #$relativeURL, but found ${jsrValue.type}")
      }

      val foundId = JsonUtils.extractIdFromObject(schemaObject)
      val fetchedDocumentLocation = SchemaLocation.refLocation(documentURIVar, foundId, pathWithinDocument)
      val schemaJson = JsonValueWithPath.fromJsonValue(parentDocumentVar, schemaObject, fetchedDocumentLocation)
      return schemaLoader.subSchemaBuilder(schemaJson, parentDocumentVar, report)
    }
    return null
  }
}
