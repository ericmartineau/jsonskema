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

import io.mverse.jsonschema.enums.FormatType
import lang.illegalState
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

@RunWith(Parameterized::class)
class FormatValidatorTest(private val formatName: String) {

  @Test
  fun check() {
    SchemaValidatorFactoryImpl.forFormat(FormatType.fromFormat(formatName) ?: illegalState("Unable to get format enum"))
  }

  companion object {
    @Parameters(name = "{0}")
    fun params(): List<Array<Any>> {
      return Arrays.asList(
          arrayOf<Any>("date-time"),
          arrayOf<Any>("email"),
          arrayOf<Any>("hostname"),
          arrayOf<Any>("ipv6"),
          arrayOf<Any>("ipv4"),
          arrayOf<Any>("uri")
      )
    }
  }
}
