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

import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.integration.ServletSupport
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import org.junit.After
import org.junit.Test

class RelativeURITest {

  private lateinit var servletSupport: ServletSupport

  @Test
  fun test() {
    servletSupport = ServletSupport.withDocumentRoot("/io/mverse/jsonschema/loading/relative-uri/")
    servletSupport.run(Runnable{
      runTests()
    })
  }

  @After
  fun after() {
    servletSupport.stopJetty()
  }

  private fun runTests() {
    val schemaLoader = JsonSchemas.createSchemaReader()
    val jsonObject = JsonSchemas.resourceLoader(this::class).readJsonObject("relative-uri/schema/main.json")
    schemaLoader.readSchema(jsonObject)
  }
}
