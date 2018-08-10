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
//package io.mverse.jsonschema.loading
//
//import com.google.common.base.Preconditions.checkNotNull
//import io.mverse.jsonschema.JsonSchema
//
//import io.mverse.jsonschema.Schema
//import io.mverse.jsonschema.SchemaException
//import io.mverse.jsonschema.resourceLoader
//import io.mverse.jsonschema.schemaReader
//import kotlinx.serialization.json.JsonElement
//import kotlinx.serialization.json.JsonObject
//import java.util.Optional
//import java.util.function.Consumer
//import java.util.function.Predicate
//import org.junit.Assert
//
//object LoadingTestSupport {
//
//  fun failure(): Failure<*, *> {
//    return Failure()
//  }
//
//  fun <E : Exception> failWith(expectedException: Class<E>): Failure<*, E> {
//    checkNotNull(expectedException, "expectedException must not be null")
//    return Failure<Schema, E>().expectedException(expectedException)
//  }
//
//  fun <S : Schema, E : Exception> expectFailure(failure: Failure<S, E>): E? {
//    try {
//      val schemaFactory = failure.schemaFactory() ?: (JsonSchema.schemaReader())
//      schemaFactory.readSchema(failure.input()!!)
//    } catch (e: Throwable) {
//      failure.expectedException()
//          .ifPresent { expected ->
//            if (!expected.isAssignableFrom(e.javaClass)) {
//              e.printStackTrace()
//              Assert.fail(String.format("Exception is of wrong type.  Expected %s but got %s",
//                  expected, e))
//            } else {
//              val expectedException = expected.cast(e)
//              failure.expected().ifPresent { predicate -> Assert.assertTrue("Exception testing predicate failed", predicate.test(expectedException)) }
//
//              failure.expectedConsumer().ifPresent { consumer -> consumer.accept(expectedException) }
//            }
//          }
//      failure.expectedSchemaLocation().ifPresent { pointer ->
//        if (e is SchemaLoadingException) {
//          assertThat(e.getReport().getIssues().stream()
//              .filter({ err ->
//                err.getLocation()
//                    .getJsonPointerFragment().toString()
//                    .equals(pointer)
//              })
//              .findAny()).`as`("Should find an issue for location: $pointer")
//              .isPresent()
//        } else if (e is SchemaException) {
//          val schemaLocation = e.schemaLocation
//          Assert.assertEquals("Error documentRoot incorrect", pointer, schemaLocation)
//        } else {
//          Assert.fail("Trying to test pointer, but exception wasn't SchemaException, it was: $e")
//        }
//      }
//      return e as E
//    }
//
//    Assert.fail("Should have failed but didn't")
//    return null //Won't ever get here.
//  }
//
//  class Failure<S : Schema, E : Exception> {
//
//    private var input: JsonObject? = null
//
//    private var expectedSchemaLocation = "#"
//
//    private var schemaFactory: SchemaLoaderImpl? = null
//
//    private var expectedException: Class<E>? = null
//
//    private var expectedPredicate: Predicate<E>? = null
//
//    private var expectedConsumer: Consumer<E>? = null
//
//    fun expect(): E? {
//      return expectFailure(this)
//    }
//
//    fun expected(): Optional<Predicate<E>> {
//      return expectedPredicate
//    }
//
//    fun expected(expected: Predicate<E>): Failure<S, E> {
//      this.expectedPredicate = expected
//      return this
//    }
//
//    fun expected(expected: Consumer<E>): Failure<S, E> {
//      this.expectedConsumer = expected
//      return this
//    }
//
//    fun expectedConsumer(): Optional<Consumer<E>> {
//      return expectedConsumer
//    }
//
//    fun expectedSchemaLocation(expectedPointer: String): Failure<S, E> {
//      this.expectedSchemaLocation = expectedPointer
//      return this
//    }
//
//    fun expectedSchemaLocation(): Optional<String> {
//      return expectedSchemaLocation
//    }
//
//    fun expectedException(exceptionClass: Class<E>): Failure<S, E> {
//      this.expectedException = exceptionClass
//      return this
//    }
//
//    fun input(input: String): Failure<S, E> {
//      this.input = JsonSchema.resourceLoader().readJsonObject(input)
//      return this
//    }
//
//    fun input(input: JsonObject): Failure<S, E> {
//      this.input = input
//      return this
//    }
//
//    fun input(input: JsonElement): Failure<S, E> {
//      this.input = input.jsonObject
//      return this
//    }
//
//    fun input(): JsonObject? {
//      return input
//    }
//
//    fun nullInput(): Failure<*, *> {
//      this.input = null
//      return this
//    }
//
//    fun schemaFactory(schemaFactory: SchemaLoaderImpl): Failure<*, *> {
//      this.schemaFactory = schemaFactory
//      return this
//    }
//
//    fun schemaFactory(): SchemaLoaderImpl? {
//      return this.schemaFactory
//    }
//
//    private fun expectedException(): Class<E>? {
//      return expectedException
//    }
//  }
//}
