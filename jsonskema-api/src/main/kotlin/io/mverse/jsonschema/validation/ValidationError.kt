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

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lang.URI
import lang.json.asJsonArray
import lang.json.toJson
import lang.json.toJsonObject

/**
 * Thrown by [Schema] subclasses on validation failure.
 */
data class ValidationError(
    /**
     * The schema that generated this error
     */
    val violatedSchema: Schema? = null,

    /**
     * A pointer to the violation within the input document we validated.
     */
    private val pointerToViolation: JsonPath? = null,

    val code: String? = null,
    /**
     * Returns a programmer-readable error description. Unlike [.getMessage] this doesn't
     * contain the JSON pointer denoting the violating document fragment.
     *
     * @return the error description
     */
    val errorMessage: String? = null,
    private val messageTemplate: String? = null,

    val causes: List<ValidationError>? = null,
    val keyword: KeywordInfo<*>? = null,
    val arguments: List<Any>? = null
) {

  /**
   * Returns all messages collected from all violations, including nested causing exceptions.
   *
   * @return all messages
   */
  val allMessages: List<ValidationError>
    get() = if (causes!!.isEmpty()) {
      listOf(this)
    } else {
      causes.allMessages
    }

  /**
   * Returns a programmer-readable error description prepended by [ the pointer to the violating fragment][.getPointerToViolation] of the JSON document.
   *
   * @return the error description
   */
  val message: String
    get() = getPointerToViolation() + ": " + errorMessage

  val pathToViolation: JsonPath?
    get() = pointerToViolation

  val schemaLocation: URI?
    get() = violatedSchema!!.pointerFragmentURI

  val violationCount: Int
    get() = causes!!.violationCount

  /**
   * A JSON pointer denoting the part of the document which violates the schema. It always points
   * from the root of the document to the violating data fragment, therefore it always starts with
   * `#`.
   *
   * @return the JSON pointer
   */
  fun getPointerToViolation(): String? {
    return pointerToViolation?.toURIFragment()?.toString()
  }

  fun toJson(withCauses: Boolean = true): JsonObject {
    val errorJson = mutableMapOf<String, JsonElement>()

    if (pointerToViolation == null) {
      errorJson["pointerToViolation"] = JsonNull
    } else {
      errorJson["pointerToViolation"] = getPointerToViolation().toJson()
    }
    if (this.keyword != null) {
      errorJson["keyword"] = keyword.key.toJson()
    }
    if (code != null) {
      errorJson["code"] = JsonPrimitive(code)
    }
    errorJson["message"] = this.errorMessage.toJson()
    if (violatedSchema != null) {
      errorJson["schemaLocation"] = schemaLocation!!.toString().toJson()
    }

    if (this.arguments!!.isNotEmpty()) {
      errorJson["template"] = this.messageTemplate.toJson()
      errorJson["arguments"] = arguments.map { it.toString() }.asJsonArray()
    }

    if (withCauses && causes!!.isNotEmpty()) {
      errorJson["causes"] = causes.map { it.toJson() }.asJsonArray()
    }

    return errorJson.toJsonObject()
  }

  /**
   * Creates a JSON representation of the failure.
   *
   *
   * The returned `JSONObject` contains the following keys:
   *
   *  * `"message"`: a programmer-friendly exception message. This value is a non-nullable
   * string.
   *  * `"keyword"`: a JSON Schema keyword which was used in the schema and violated by the
   * input JSON. This value is a nullable string.
   *  * `"pointerToViolation"`: a JSON Pointer denoting the path from the root of the
   * document to the invalid fragment of it. This value is a non-nullable string. See
   * [.getPointerToViolation]
   *  * `"causes"`: is a (possibly empty) array of violations which caused this
   * exception. See [.getCauses]
   *  * `"documentRoot"`: a string denoting the path to the violated schema keyword in the schema
   * JSON (since version 1.6.0)
   *
   *
   * @return a JSON description of the validation error
   */
  fun toJsonErrors(): JsonArray {
    return this.allMessages
        .map { e -> e.toJson(false) }
        .asJsonArray()
  }

  override fun toString(): String {
    return "ValidationError{" +
        "pointerToViolation=" + pointerToViolation +
        ", causingExceptions=" + causes +
        ", keyword='" + keyword + '\''.toString() +
        ", message='" + errorMessage + '\''.toString() +
        '}'.toString()
  }

  fun withKeyword(keyword: KeywordInfo<*>, message: String): ValidationError {
    return this.copy(
        keyword = keyword,
        errorMessage = message,
        code = "validation.keyword." + keyword.key)
  }

  //  class ValidationErrorBuilder {
  //
  //    private var pointerToViolation: JsonPath? = JsonPath.rootPath()
  //
  //    fun message(message: String, vararg args: Object): ValidationErrorBuilder {
  //      this.message = String.format(message, args)
  //      this.messageTemplate = message
  //      for (arg in args) {
  //        this.argument(arg)
  //      }
  //      return this
  //    }
  //
  //    fun message(message: String): ValidationErrorBuilder {
  //      this.message = message
  //      this.messageTemplate = message
  //      return this
  //    }
  //
  //    fun pointerToViolationURI(uriFragment: String?): ValidationErrorBuilder {
  //      if (uriFragment == null) {
  //        this.pointerToViolation = null
  //      } else {
  //        this.pointerToViolation(JsonPath.parseFromURIFragment(uriFragment))
  //      }
  //      return this
  //    }
  //  }

  companion object {

    val NULL_ERROR = ValidationError()

    /**
     * Sort of static factory method. It is used by validators to create `ValidationError`s, handling the case of multiple violations
     * occuring during validation.
     *
     *
     *
     *  * If `failures` is empty, then it doesn't do anything
     *  * If `failures` contains 1 exception instance, then that will be thrown
     *  * Otherwise a new exception instance will be created, its [ violated schema][.getViolatedSchema] will be `rootFailingSchema`, and its [ causing exceptions][.getCauses] will be the `failures` list
     *
     *
     * @param rootFailingSchema the schema which detected the `failures`
     * @param failures          list containing validation failures to be thrown by this method
     */
    fun collectErrors(rootFailingSchema: Schema,
                      currentLocation: JsonPath,
                      failures: List<ValidationError>): ValidationError? {
      val failureCount = failures.size
      return when {
        failures.isEmpty() -> null
        failures.size == 1 -> failures.first()
        else -> ValidationError(
            violatedSchema = rootFailingSchema,
            pointerToViolation = currentLocation,
            errorMessage = "%d schema violations found",
            arguments = listOf(failures.violationCount),
            code = "validation.multipleFailures",
            causes = failures,
            keyword = null)

      }
    }
  }
}

val List<ValidationError>.violationCount: Int
  get() {
    val causeCount = this.sumBy { it.violationCount }
    return kotlin.math.max(1, causeCount)
  }

val List<ValidationError>.allMessages: List<ValidationError>
  get() {
    val messages = this
        .filter { it.causes?.isNotEmpty() == true }
        .toMutableList()
    messages.addAll(this
        .filter { it.causes?.isNotEmpty() == true }
        .flatMap { it.allMessages })

    return messages.toList()
  }
