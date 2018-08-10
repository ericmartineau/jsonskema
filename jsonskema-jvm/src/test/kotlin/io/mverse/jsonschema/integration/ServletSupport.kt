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

import java.util.Objects.requireNonNull

import java.io.File
import java.net.URISyntaxException
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder

class ServletSupport(private val documentRoot: File) {

  private var server: Server? = null

  fun run(runnable: Runnable) {
    initJetty()
    runnable.run()
    stopJetty()
  }

  fun initJetty() {
    server = Server(1234)
    val handler = ServletHandler()
    server!!.handler = handler
    handler.addServletWithMapping(ServletHolder(IssueServlet(documentRoot)), "/*")
    try {
      server!!.start()
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  fun stopJetty() {
    if (server != null) {
      try {
        server!!.stop()
      } catch (e: Exception) {
        throw RuntimeException(e)
      }
    }
    server = null
  }

  companion object {

    fun withDocumentRoot(path: String): ServletSupport {
      try {
        return withDocumentRoot(File(ServletSupport::class.java.getResource(path).toURI()))
      } catch (e: URISyntaxException) {
        throw RuntimeException(e)
      }
    }

    fun withDocumentRoot(documentRoot: File): ServletSupport {
      return ServletSupport(documentRoot)
    }
  }
}
