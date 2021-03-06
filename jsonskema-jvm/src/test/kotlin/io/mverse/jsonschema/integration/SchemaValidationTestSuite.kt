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
package io.mverse.jsonschema.integration

import com.google.common.base.Preconditions
import io.mverse.json.jsr353.clean
import io.mverse.json.jsr353.nullable
import io.mverse.json.jsr353.untyped
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.createSchemaReader
import kotlinx.serialization.json.JsonArray
import lang.json.JsrValue
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.content
import lang.json.JsonKey
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.get
import lang.json.unbox
import lang.json.unboxAsAny
import lang.json.unboxOrNull
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

@RunWith(Parameterized::class)
class SchemaValidationTestSuite(private val schemaDescription: String, private val schemaJson: lang.json.JsrObject,
                                private val inputDescription: String, private val input: JsrValue,
                                private val expectedToBeValid: Boolean) {

  @Test
  fun test() {
    try {
      val schema = JsonSchema.createSchemaReader().readSchema(schemaJson)
      val validator = createTestValidator(schema)
      val validationErrors = validator.validate(input)
      val failed = validationErrors != null
      if (expectedToBeValid && failed) {
        throw AssertionError("false failure for $inputDescription\n$validationErrors")
      }
      if (!expectedToBeValid && !failed) {
        throw AssertionError("false success for $inputDescription")
      }
    } catch (e: SchemaException) {
      throw AssertionError("schema loading failure for $schemaDescription", e)
    } catch (e: Exception) {
      throw AssertionError("schema loading error for $schemaDescription", e)
    }
  }

  companion object {

    private var server: Server? = null

    @JvmStatic
    @Parameters(name = "{2}")
    fun params(): List<Array<Any>> {
      Preconditions.checkNotNull("jsonApi must not be null")
      val rval = ArrayList<Array<Any>>()
      val refs = Reflections("io.mverse.jsonschema.draft6", ResourcesScanner())
      val paths = refs.getResources(Pattern.compile(".*\\.json"))
      for (path in paths) {
        if (path.contains("/optional/") || path.contains("/remotes/")) {
          continue
        }
        val fileName = path.substring(path.lastIndexOf('/') + 1)
        val arr = loadTests(SchemaValidationTestSuite::class.java.getResourceAsStream("/$path"))
        arr.map { it as JsrObject }.forEach { schemaTest ->
          val testInputs = schemaTest["tests"] as JsrArray
          testInputs.map { it as JsrObject }.forEach { input ->
            val params = mutableListOf<Any>()
            params += "[" + fileName + "]/" + schemaTest[JsonKey("description")].unbox()
            params += schemaTest["schema"] as JsrObject
            params += "[" + fileName + "]/" + input[JsonKey("description")].unbox()
            params += input["data"]!!
            params += input["valid"]!!.unbox<Boolean>()
            rval.add(params.toTypedArray())
          }
        }
      }
      if (rval.size == 0) {
        Assert.fail("No tests found.  Check classpath")
      }
      return rval
    }

    @BeforeClass
    @Throws(Exception::class)
    @JvmStatic
    fun startJetty() {
      server = Server(1234)
      val handler = ServletHandler()
      server!!.handler = handler
      handler.addServletWithMapping(ServletHolder(IssueServlet(File(ServletSupport::class.java
          .getResource("/io/mverse/jsonschema/draft6/remotes").toURI()))), "/*")
      server!!.start()
    }

    @AfterClass
    @Throws(Exception::class)
    @JvmStatic
    fun stopJetty() {
      if (server != null) {
        server!!.stop()
      }
    }

    private fun loadTests(input: InputStream): JsrArray = input.parseJsrJson() as JsrArray
  }
}
