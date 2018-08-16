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

import assertk.assertions.isEqualTo
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.assertThat
import lang.URI
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

@RunWith(Parameterized::class)
class ReferenceScopeResolverTest(expectedOutput: String, parentScope: String,
                                 encounteredSegment: String) {

  private val expectedOutput: URI

  private val parentScope: URI

  private val encounteredSegment: URI

  init {
    this.expectedOutput = URI(expectedOutput)
    this.parentScope = URI(parentScope)
    this.encounteredSegment = URI(encounteredSegment)
  }

  @Test
  fun test() {
    val parentLocation = SchemaLocation.documentRoot(parentScope)
    val childLocation = parentLocation.child(encounteredSegment)
    childLocation.uniqueURI.assertThat().isEqualTo(expectedOutput)
  }

  companion object {

    @Parameters(name = "{0}")
    @JvmStatic
    fun params(): List<Array<out Any>> {
      return Arrays.asList(
          parList("fragment id", "http://x.y.z/root.json#foo", "http://x.y.z/root.json", "#foo"),
          parList("rel path", "http://example.org/foo", "http://example.org/bar", "foo"),
          parList("file name change", "http://x.y.z/schema/child.json",
              "http://x.y.z/schema/parent.json",
              "child.json"),
          parList("file name after folder path", "http://x.y.z/schema/child.json",
              "http://x.y.z/schema/", "child.json"),
          parList("new root", "http://bserver.com", "http://aserver.com/",
              "http://bserver.com"))
    }

    private fun parList(vararg params: String): Array<out Any> {
      return params
    }
  }
}
