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

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordInfoSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNull
import lang.Name
import lang.SerializableWith
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.jsrObject
import lang.json.toKtArray
import lang.net.URI
import lang.string.format

/**
 * Thrown by [Schema] subclasses on validation failure.
 */
@Serializable
data class ValidationError(
    /**
     * The schema that generated this error
     */
    @Transient
    @Name("violatedSchema")
    val violatedSchema: Schema? = null,

    /**
     * A pointer to the violation within the input document we validated.
     */
    @Name("pointerToViolation")
    val pointerToViolation: JsonPath? = null,

    @Name("code")
    val code: String? = null,

    /**
     * Returns a programmer-readable error description. Unlike [.getMessage] this doesn't
     * contain the JSON pointer denoting the violating document fragment.
     *
     * @return the error description
     */
    @Transient
    @Name("errorMessage")
    private val errorMessage: String? = null,

    @Name("messageTemplate")
    private val messageTemplate: String? = null,

    @Name("causes")
    val causes: List<ValidationError> = emptyList(),

    @Name("keyword")
    @SerializableWith(KeywordInfoSerializer::class)
    val keyword: KeywordInfo<*>? = null,

    @Name("arguments")
    val arguments: List<Any>? = null
) {

  /**
   * Returns all messages collected from all violations, including nested causing exceptions.
   *
   * @return all messages
   */
  @Transient
  val allMessages: List<ValidationError>
    get() = if (causes.isEmpty()) {
      listOf(this)
    } else {
      causes.flattenErrors()
    }

  /**
   * Returns a programmer-readable error description prepended by [ the pointer to the violating fragment][.getPointerToViolation] of the JSON document.
   *
   * @return the error description
   */
  @Transient
  val message: String
    get () = "$pathToViolation: $resolvedMessage"

  @Transient
  val resolvedMessage: String
    get() {
      return when {
        errorMessage != null -> errorMessage
        arguments != null && messageTemplate != null -> messageTemplate.format(*arguments.toTypedArray())
        else -> ""
      }
    }

  /**
   * A JSON pointer denoting the part of the document which violates the schema. It always points
   * from the root of the document to the violating data fragment, therefore it always starts with
   * `#`.
   *
   * @return the JSON pointer
   */
  val pathToViolation: String?
    get() = pointerToViolation?.uriFragment?.toString()

  val schemaLocation: URI?
    get() = violatedSchema!!.pointerFragmentURI

  val violationCount: Int
    get() = causes.violationCount

  fun toJson(withCauses: Boolean = true): JsrObject {
    return jsrObject {
      if (arguments?.isNotEmpty() == true) {
        "template" *= messageTemplate ?: JsonNull
      }

      if (code != null) {
        "code" *= code
      }

      if (violatedSchema != null) {
        "schemaLocation" *= schemaLocation!!.toString()
      }

      if (pointerToViolation == null) {
        "pointerToViolation" *= JsonNull
      } else {
        "pointerToViolation" *= pathToViolation ?: JsonNull
      }

      if (arguments?.isNotEmpty() == true) {
        "arguments" *= arguments.map { it.toString() }
      }

      if (keyword != null) {
        "keyword" *= keyword.key
      }

      if (withCauses && causes.isNotEmpty()) {
        "causes" *= causes.map { it.toJson() }
      }

      "message" *= resolvedMessage
    }
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
  fun toJsonErrors(): kotlinx.serialization.json.JsonArray {
    return this.allMessages
        .map { e -> e.toJson(false) }
        .toKtArray()
  }

  override fun toString(): String {
    return "ValidationError{" +
        "pointerToViolation=" + pointerToViolation +
        ", causingExceptions=" + causes +
        ", keyword='" + keyword + '\''.toString() +
        ", message='" + errorMessage + '\''.toString() +
        '}'.toString()
  }

  fun withError(message: String, vararg args: Any): ValidationError {
    return this.copy(messageTemplate = message,
        errorMessage = message.format(*args),
        arguments = args.toList())
  }

  fun withKeyword(keyword: KeywordInfo<*>, message: String): ValidationError {
    return this.copy(
        keyword = keyword,
        errorMessage = message,
        code = "validation.keyword." + keyword.key)
  }

  companion object {

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
      return when {
        failures.isEmpty() -> null
        failures.size == 1 -> failures.first()
        else -> ValidationError(
            violatedSchema = rootFailingSchema,
            pointerToViolation = currentLocation,
            messageTemplate = "%d schema violations found",
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

fun List<ValidationError>.flattenErrors(): List<ValidationError> {
  return filter { it.causes.isEmpty() } +
      filter { !it.causes.isEmpty() }
          .flatMap { it.causes.flattenErrors() }
}
