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
//package io.mverse.jsonschema
//
//import java.util.Objects.requireNonNull
//
//import com.google.common.io.ByteStreams
//import kotlinx.io.IOException
//
//import java.io.File
//import java.io.FileInputStream
//import java.io.IOException
//import java.io.InputStream
//import java.util.Arrays
//import javax.servlet.ServletException
//import javax.servlet.http.HttpServlet
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//
//class IssueServlet(documentRoot: File) : HttpServlet() {
//
//  private val documentRoot: File
//
//  init {
//    this.documentRoot = requireNonNull(documentRoot, "documentRoot cannot be null")
//  }
//
//  protected fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
//    println("GET " + req.getPathInfo())
//    val content = fileByPath(req.getPathInfo())
//    resp.setContentType("application/json")
//
//    FileInputStream(content).use { schemaFileStream -> ByteStreams.copy(schemaFileStream, resp.getOutputStream()) }
//  }
//
//  private fun fileByPath(pathInfo: String?): File {
//    var toLookUp = documentRoot
//    if (pathInfo != null && pathInfo != "/" && !pathInfo.isEmpty()) {
//      val segments = pathInfo.trim { it <= ' ' }.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//      for (fileName in segments) {
//        if (fileName.isEmpty()) {
//          continue
//        }
//        toLookUp = Arrays.stream(toLookUp.listFiles()!!)
//            .filter { file -> file.name == fileName }
//            .findFirst()
//            .orElseThrow { RuntimeException("file [$pathInfo] not found") }
//      }
//    }
//    return toLookUp
//  }
//
//
//}
