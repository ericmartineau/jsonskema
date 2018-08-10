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
package io.mverse.jsonschema.validation.keywords.string.formatValidators

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.mverse.jsonschema.validation.FormatValidator
import org.junit.Assert
import org.junit.Test

import java.util.Optional

class DefaultFormatValidatorTest {

  private fun assertFailure(subject: String, format: FormatValidator,
                            expectedFailure: String) {
    val opt = format.validate(subject)
    assert(opt).isEqualTo(expectedFailure)
  }

  private fun assertSuccess(subject: String, format: FormatValidator) {
    val opt = format.validate(subject)
    assert(opt).isNull()
  }

  @Test
  fun dateTimeExceedingLimits() {
    assertFailure("1996-60-999T16:39:57-08:00", DateTimeFormatValidator(),
        "[1996-60-999T16:39:57-08:00] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]")
  }

  @Test
  fun dateTimeFormatFailure() {
    assertFailure("2015-03-13T11:00:000", DateTimeFormatValidator(),
        "[2015-03-13T11:00:000] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]")
  }

  @Test
  fun dateTimeWithSingleDigitInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.1Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithTwoDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.12Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithThreeDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.123Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithFourDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.1234Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithFiveDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.12345Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithSixDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.123456Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithSevenDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.1234567Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithEightDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.12345678Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithNineDigitsInSecFracSuccess() {
    assertSuccess("2015-02-28T11:00:00.123456789Z", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeWithTenDigitsInSecFracFailure() {
    assertFailure("2015-02-28T11:00:00.1234567890Z", DateTimeFormatValidator(),
        "[2015-02-28T11:00:00.1234567890Z] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]")
  }

  @Test
  fun dateTimeSuccess() {
    assertSuccess("2015-03-13T11:00:00+00:00", DateTimeFormatValidator())
  }

  @Test
  fun dateTimeZSuccess() {
    assertSuccess("2015-02-28T11:00:00Z", DateTimeFormatValidator())
  }

  @Test
  fun emailFailure() {
    assertFailure("a.@b.com", EmailFormatValidator(), "[a.@b.com] is not a valid email address")
  }

  @Test
  fun emailSuccess() {
    assertSuccess("a@b.com", EmailFormatValidator())
  }

  @Test
  fun hostnameLengthFailure() {
    val sb = StringBuilder()
    for (i in 0..255) {
      sb.append('a')
    }
    val subject = sb.toString()
    assertFailure(subject, HostnameFormatValidator(), "[" + subject
        + "] is not a valid hostname")
  }

  @Test
  fun hostnameSuccess() {
    assertSuccess("localhost", HostnameFormatValidator())
  }

  @Test
  fun ipv4Failure() {
    assertFailure("asd", IPV4Validator(), "[asd] is not a valid ipv4 address")
  }

  @Test
  fun ipv4LengthFailure() {
    assertFailure(IPV6_ADDR, IPV4Validator(),
        "[2001:db8:85a3:0:0:8a2e:370:7334] is not a valid ipv4 address")
  }

  @Test
  fun ipv4Success() {
    assertSuccess(THERE_IS_NO_PLACE_LIKE, IPV4Validator())
  }

  @Test
  fun ipv6Failure() {
    assertFailure("asd", IPV6Validator(), "[asd] is not a valid ipv6 address")
  }

  @Test
  fun ipv6LengthFailure() {
    assertFailure(THERE_IS_NO_PLACE_LIKE, IPV6Validator(),
        "[127.0.0.1] is not a valid ipv6 address")
  }

  @Test
  fun ipv6Success() {
    assertSuccess(IPV6_ADDR, IPV6Validator())
  }

  @Test
  fun uriFailure() {
    assertFailure("12 34", URIFormatValidator(), "[12 34] is not a valid URI")
  }

  @Test
  fun uriSuccess() {
    assertSuccess("http://example.org:8080/example.html", URIFormatValidator())
  }

  companion object {

    private val THERE_IS_NO_PLACE_LIKE = "127.0.0.1"

    private val IPV6_ADDR = "2001:db8:85a3:0:0:8a2e:370:7334"
  }
}
