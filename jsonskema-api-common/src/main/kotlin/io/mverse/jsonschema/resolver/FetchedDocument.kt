package io.mverse.jsonschema.resolver

import io.mverse.jsonschema.loading.parseJsrObject
import lang.json.JsrObject
import lang.json.jkey
import lang.json.mutate
import lang.net.URI
import lang.time.currentTime

data class FetchedDocument(val fetcherKey: FetcherKey, val originalUri: URI, val uri: URI, val schemaData: String) {
  val isDifferentURI = originalUri != uri
  val jsrObject: JsrObject = timed("parse") {
    schemaData.parseJsrObject()
        .mutate {
          if (isDifferentURI) {
            val id = when {
              idKey in this -> idKey
              dollarIdKey in this -> dollarIdKey
              else -> dollarIdKey
            }
            id.value *= originalUri
          }
        }
  }

  companion object {
    val idKey = "id".jkey
    val dollarIdKey = "\$id".jkey
  }
}

