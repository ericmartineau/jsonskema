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
package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonPath
import kotlinx.serialization.json.JsonObject
import lang.URI

/**
 * This interface is used by [io.mverse.jsonschema.loading.SchemaReader] to fetch the contents denoted by remote JSON
 * pointer.
 *
 * Implementations are expected to support the HTTP/1.1 protocol, the support of other protocols is
 * optional.
 */
interface JsonDocumentClient {

  fun findLoadedDocument(documentLocation: URI): kotlinx.serialization.json.JsonObject?

  fun registerLoadedDocument(documentLocation: URI, document: kotlinx.serialization.json.JsonObject)

  fun resolveSchemaWithinDocument(documentURI: URI, schemaURI: URI, document: kotlinx.serialization.json.JsonObject): JsonPath?

  /**
   * Returns a stream to be used for reading the remote content (response body) of the URL. In the
   * case of a HTTP URL, implementations are expected send HTTP GET requests and the response is
   * expected to be represented in UTF-8 character set.
   *
   * @param uri the URL of the remote resource
   * @return the input stream of the response
   * @throws java.io.UncheckedIOException if an IO error occurs.
   */
  fun fetchDocument(uri: URI): kotlinx.serialization.json.JsonObject

  fun fetchDocument(url: String): kotlinx.serialization.json.JsonObject {
    return fetchDocument(URI(url))
  }
}
