/*
 * Copyright (C) 2017 MVerse (http://mverse.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mverse.jsonschema.loading.reference

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.loading.JsonDocumentClient
import io.mverse.jsonschema.loading.parseJsonObject
import kotlinx.serialization.json.JsonObject
import lang.URI

/**
 * A [JsonDocumentClient] implementation which uses [URL] for reading the remote content.
 */
open class DefaultJsonDocumentClient(val schemaCache: SchemaCache = SchemaCache()) : JsonDocumentClient {

  override fun findLoadedDocument(documentLocation: URI): JsonObject? {
    return schemaCache.lookupDocument(documentLocation)
  }

  override fun registerLoadedDocument(documentLocation: URI, document: JsonObject) {
    schemaCache.cacheDocument(documentLocation, document)
  }

  override fun resolveSchemaWithinDocument(documentURI: URI, schemaURI: URI, document: JsonObject): JsonPath? {
    return schemaCache.resolveURIToDocumentUsingLocalIdentifiers(documentURI, schemaURI, document)
  }

  override fun fetchDocument(uri: URI): JsonObject = uri.readFully().parseJsonObject()
}
