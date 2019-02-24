package io.mverse.jsonschema.validation.keywords.string.formatValidators

import assertk.assert
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class DateTimeFormatValidatorTest(val message: String, val value: String, val shouldBeValid: Boolean) {
  val formatter = DateTimeFormatValidator()
  @Test fun run() {
    val validate = formatter.validate(value)
    when (shouldBeValid) {
      true -> assert(validate).isNull()
      false -> assert(validate).isNotNull()
    }
  }

  companion object {
    @JvmStatic @Parameters(name = "{0} is valid = {2}") fun parameters() = arrayOf(
        arrayOf("With Z", "2014-12-23T12:23:11Z", true),
        arrayOf("No offset", "2014-12-23T12:23:11", true),
        arrayOf("No colon in offset", "2014-12-23T12:23:11+0000", true),
        arrayOf("No T separator", "2014-12-23 12:23:11Z", false),
        arrayOf("No T separator", "2014-12-23T12:23Z", true),
        arrayOf("Invalid year", "20134-12-23T12:23:11", false),
        arrayOf("No millis", "2014-12-23T12:23:11+00:00", true),
        arrayOf("Complete date", "2014-12-23T12:23:11.143+00:00", true)
    )
  }
}
