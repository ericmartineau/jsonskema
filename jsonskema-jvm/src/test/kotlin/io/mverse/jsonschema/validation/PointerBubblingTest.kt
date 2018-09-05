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
package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.getValidator
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import org.junit.Assert
import org.junit.Test

class PointerBubblingTest {
  private val allSchemas = loader.readJsonObject("testschemas.json")
  private val rectangleSchema = JsonSchema.createSchemaReader().readSchema(allSchemas.getObject("pointerResolution"))
  private val testInputs = loader.readJsonObject("objecttestcases.json")

  @Test
  fun rectangleMultipleFailures() {
    val input = testInputs.getObject("rectangleMultipleFailures")
    val e = verifyFailure { JsonSchema.getValidator(rectangleSchema).validate(input) }
    Assert.assertEquals("#/rectangle", e.pathToViolation)
    Assert.assertEquals(2, e.causes.size)
    Assert.assertEquals(1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/a"))
    Assert.assertEquals(1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/b"))
  }

  @Test
  fun rectangleSingleFailure() {
    val input = testInputs.getObject("rectangleSingleFailure")
    ValidationTestSupport.expectFailure(rectangleSchema, RefSchema::class.java, "#/rectangle/a", input)
  }

  companion object {
    private val loader = JsonSchema.resourceLoader()
  }
}
