package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.SchemaLocation.Companion.BLANK_URI
import io.mverse.jsonschema.SchemaLocation.Companion.ROOT_URI
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.schemaException
import io.mverse.jsonschema.utils.JsonUtils
import io.mverse.jsonschema.utils.isJsonPointer
import io.mverse.jsonschema.utils.withoutFragment
import lang.json.JsonPath
import lang.json.JsrNull
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.getOrNull
import lang.json.type
import lang.net.URI
import lang.net.fragment
import lang.net.resolveUri
import lang.suppress.Suppressions.Companion.NAME_SHADOWING

data class RefSchemaLoader(val documentClient: JsonDocumentClient, val schemaLoader: SchemaLoader) {

  fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: JsrObject?, report: LoadingReport): Schema? {
    // Cache ahead to deal with any stack overflows
    val currentLocation = referencedFrom.location
    schemaLoader += referencedFrom

    // Make sure we're dealing with an absolute URI
    val absoluteReferenceURI = currentLocation.resolutionScope.resolveUri(refURI)
    val documentURI = currentLocation.documentURI

    return when (val existing = findRefSchema(referencedFrom, refURI, currentDocument, report)) {
      null -> {
        val schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument, report)
            ?: findRefInRemoteDocument(absoluteReferenceURI, report)
            ?: return null
        val refSchema = schemaBuilder.build()
        schemaLoader += refSchema
        refSchema
      }
      else -> existing
    }
  }

  fun findRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: JsrObject?, report: LoadingReport): Schema? {
    // Cache ahead to deal with any infinite recursion.
    val currentLocation = referencedFrom.location
    schemaLoader += referencedFrom

    // Make sure we're dealing with an absolute URI
    val absoluteReferenceURI = currentLocation.resolutionScope.resolveUri(refURI)
    val documentURI = currentLocation.documentURI

    // Look for a cache schema at this URI
    val cachedSchema = schemaLoader.findLoadedSchema(absoluteReferenceURI)
    if (cachedSchema != null) {
      return cachedSchema
    }

    val schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument, report)
        ?: return null //Couldn't be resolved yet
    val refSchema = schemaBuilder.build()
    schemaLoader += refSchema
    return refSchema
  }

  internal fun loadDocument(referenceURI: URI): lang.json.JsrObject? {
    val remoteDocumentURI = referenceURI.withoutFragment()

    val foundDoc = documentClient.findLoadedDocument(remoteDocumentURI)
    return foundDoc
        ?: documentClient.fetchDocument(remoteDocumentURI).fetchedOrNull?.let cache@{ results ->
          documentClient.registerFetchedDocument(results)
          return@cache results.jsrObject
        }
  }

  internal fun findRefInRemoteDocument(referenceURI: URI, report: LoadingReport): MutableSchema? {
    val remoteDocumentURI = referenceURI.resolveUri("#")
    val remoteDocument = loadDocument(remoteDocumentURI) ?: return null
    return findRefInDocument(remoteDocumentURI, referenceURI, remoteDocument, report)
        ?: throw SchemaException(referenceURI, "Unable to locate fragment: \n\tFragment: '#${referenceURI.fragment
            ?: ""}' " +
            "in document\n\tDocument:'$remoteDocument'")
  }

  @Suppress(NAME_SHADOWING)
  fun findRefInDocument(documentURI: URI, referenceURI: URI, parentDocument: JsrObject?,
                        report: LoadingReport): MutableSchema? {

    var documentURI = documentURI
    val parentDocument = parentDocument ?: loadDocument(referenceURI) ?: return null

    //Remove any fragments from the parentDocument URI
    documentURI = documentURI.withoutFragment()

    // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
    // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
    val relativeURL = documentURI.relativize(referenceURI)

    val pathWithinDocument = when {
      /* The parentDocument is the target*/
      relativeURL == ROOT_URI || relativeURL == BLANK_URI -> JsonPath.rootPath
      //This is a json fragment
      relativeURL.isJsonPointer() -> JsonPath.fromURI(relativeURL)
      //This must be a reference $id somewhere in the parentDocument.
      else -> documentClient.resolveSchemaWithinDocument(documentURI, referenceURI, parentDocument)
    } ?: return null

    val jsrValue: JsrValue = parentDocument.getOrNull(pathWithinDocument) ?: JsrNull
    val schemaObject: JsrObject = when (jsrValue) {
      JsrNull -> schemaException(referenceURI, "Unable to resolve '#$relativeURL' as JSON Pointer within '$documentURI'")
      is JsrObject -> jsrValue
      else -> schemaException(referenceURI, "Expecting JsrObject at #$relativeURL, but found ${jsrValue.type}")
    }

    val foundId = JsonUtils.extractIdFromObject(schemaObject)
    val fetchedDocumentLocation = SchemaLocation.refLocation(documentURI, foundId, pathWithinDocument)
    val schemaJson = JsonValueWithPath.fromJsonValue(parentDocument, schemaObject, fetchedDocumentLocation)
    return schemaLoader.subSchemaBuilder(schemaJson, parentDocument, report)
  }
}
