///*
// * Copyright (C) 2017 MVerse (http://mverse.io)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.mverse.jsonschema.integration
//
//import io.mverse.jsonschema.Schema
//import java.util.Objects.requireNonNull
//
//import io.mverse.jsonschema.utils.JsonUtils
//import io.mverse.jsonschema.validation.SchemaValidator
//import io.mverse.jsonschema.validation.ValidationError
//import lombok.SneakyThrows
//import org.junit.After
//import org.junit.Assert
//import org.junit.Assume
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.Parameterized
//import org.junit.runners.Parameterized.Parameters
//
//import java.io.File
//import java.io.FileInputStream
//import java.net.URISyntaxException
//import java.util.ArrayList
//import java.util.Arrays
//import java.util.Optional
//import javax.json.JsonArray
//import kotlin.io.json.JsonObject
//import javax.json.JsonStructure
//import javax.json.JsonValue
//import javax.json.spi.JsonProvider
//
//@RunWith(Parameterized::class)
//class IssueTest(issueDir: File, ignored: String) {
//
//  private val issueDir: File
//  private var servletSupport: ServletSupport? = null
//  private var validationFailureList: MutableList<String>? = null
//  private var expectedFailureList: MutableList<String>? = null
//
//  init {
//    this.issueDir = requireNonNull<File>(issueDir, "issueDir cannot be null")
//  }
//
//  @Test
//  fun test() {
//    Assume.assumeFalse("issue dir starts with 'x' - ignoring", issueDir.getName().startsWith("x"))
//    fileByName("remotes").ifPresent(Consumer<File> { this.initJetty(it) })
//    val schema = loadSchema()
//    fileByName("schema-valid.json").ifPresent({ file -> validate(file, schema, true) })
//    fileByName("schema-invalid.json").ifPresent({ file -> validate(file, schema, false) })
//  }
//
//  @After
//  fun shutdown() {
//    stopJetty()
//  }
//
//  private fun fileByName(fileName: String): Optional<File> {
//    return Arrays.stream(issueDir.listFiles()!!)
//        .filter({ file -> file.getName() == fileName })
//        .findFirst()
//  }
//
//  private fun initJetty(documentRoot: File) {
//    servletSupport = ServletSupport(documentRoot)
//    servletSupport!!.initJetty()
//  }
//
//  @SneakyThrows
//  private fun loadSchema(): Schema {
//    val schemaFile = fileByName("schema.json")
//    if (schemaFile.isPresent()) {
//      FileInputStream(schemaFile.get()).use({ schemaStream -> return JsonSchema.createSchemaReader().readSchema(schemaStream) })
//    }
//    throw RuntimeException(issueDir.getCanonicalPath() + "/schema.json is not found")
//  }
//
//  private fun stopJetty() {
//    if (servletSupport != null) {
//      servletSupport!!.stopJetty()
//    }
//  }
//
//  private fun validate(file: File, schema: Schema, shouldBeValid: Boolean) {
//    val subject = loadJsonFile(file)
//    val validator = ValidationMocks.createTestValidator(schema)
//    val errors = validator.validate(subject)
//
//    if (shouldBeValid && errors.isPresent()) {
//      val failureBuilder = StringBuilder("validation failed with: " + errors.get())
//      for (e in errors.get().getCauses()) {
//        failureBuilder.append("\n\t").append(e.getMessage())
//      }
//      Assert.fail(failureBuilder.toString())
//    }
//    if (!shouldBeValid && errors.isPresent()) {
//      val expectedFile = fileByName("expectedException.json")
//      if (expectedFile.isPresent()) {
//        if (!checkExpectedValues(expectedFile.get(), errors.get())) {
//          Assert.fail("Validation failures do not match expected values: \n" +
//              "Expected: " + expectedFailureList + ",\nActual:   " +
//              validationFailureList)
//        }
//      }
//    }
//    if (!shouldBeValid && !errors.isPresent()) {
//      Assert.fail("did not throw ValidationException for invalid schema")
//    }
//  }
//
//  // TODO - it would be nice to see this moved out of loading to the main
//  // source so that it cann be used as a convenience method by users also...
//  @SneakyThrows
//  private fun loadJsonFile(file: File): JsonValue? {
//    var subject: JsonStructure? = null
//    FileInputStream(file).use({ fileInputStream -> subject = JsonProvider.provider().createReader(fileInputStream).read() })
//    return subject
//  }
//
//  /**
//   * Allow users to provide expected values for validation failures. This method reads and parses
//   * files formatted like the following:
//   *
//   *
//   * { "message": "#: 2 schema violations found", "causingExceptions": [ { "message": "#/0/name:
//   * expected type: STRING, found: JSONArray", "causingExceptions": [] }, { "message": "#/1:
//   * required key [price] not found", "causingExceptions": [] } ] }
//   *
//   *
//   * The expected contents are then compared against the actual validation failures reported in the
//   * ValidationException and nested causingExceptions.
//   */
//  private fun checkExpectedValues(expectedExceptionsFile: File,
//                                  ve: ValidationError): Boolean {
//
//    // Read the expected values from user supplied file
//    val expected = JsonUtils.readJsonObject(expectedExceptionsFile)
//    expectedFailureList = ArrayList<String>()
//
//    // NOTE: readExpectedValues() will update expectedFailureList
//    readExpectedValues(expected)
//
//    // Read the actual validation failures into a list
//    validationFailureList = ArrayList<String>()
//    // NOTE: processValidationFailures() will update validationFailureList
//    processValidationFailures(ve)
//
//    // Compare expected to actual
//    return expectedFailureList == validationFailureList
//  }
//
//  // Recursively process the ValidationExceptions, which can contain lists
//  // of sub-exceptions...
//  // TODO - it would be nice to see this moved out of loading to the main
//  // source so that it can be used as a convenience method by users also...
//  private fun processValidationFailures(ve: ValidationError) {
//    val causes = ve.getCauses()
//    if (causes.isEmpty()) {
//      // This was a leaf node, i.e. only one validation failure
//      validationFailureList!!.add(ve.getMessage())
//    } else {
//      // Multiple validation failures exist, so process the sub-exceptions
//      // to obtain them. NOTE: Not sure we should keep the message from
//      // the current exception in this case. When there are causing
//      // exceptions, the message in the containing exception is merely
//      // summary information, e.g. "2 schema violations found".
//      validationFailureList!!.add(ve.getMessage())
//      causes.forEach(Consumer<ValidationError> { this.processValidationFailures(it) })
//    }
//  }
//
//  // Recursively process the expected values, which can contain nested arrays
//  private fun readExpectedValues(expected: JsonObject) {
//    expectedFailureList!!.add(expected.getString("message"))
//    if (expected.containsKey("causingExceptions")) {
//      val causingEx = expected.getJsonArray("causingExceptions")
//      for (subJson in causingEx.getValuesAs(JsonObject::class.java)) {
//        readExpectedValues(subJson)
//      }
//    }
//  }
//
//  companion object {
//
//    @Parameters(name = "{1}")
//    fun params(): List<Array<Any>> {
//      val rval = ArrayList<Array<Any>>()
//      try {
//        val issuesDir = File(
//            IssueTest::class.java!!.getResource("/io/mverse/jsonschema/issues").toURI())
//        for (issue in issuesDir.listFiles()!!) {
//          rval.add(arrayOf<Any>(issue, issue.getName()))
//        }
//      } catch (e: URISyntaxException) {
//        throw RuntimeException(e)
//      }
//
//      return rval
//    }
//  }
//}
