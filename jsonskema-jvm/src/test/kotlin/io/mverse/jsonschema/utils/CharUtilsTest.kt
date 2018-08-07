package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isEqualTo
import lang.Random
import lang.time.Stopwatch
import kotlin.test.Test

class CharUtilsTest {

  @Test
  fun testtryParsePositiveInt_HappyPath() {
    val i = CharUtils.tryParsePositiveInt("331")
    assert(i).isEqualTo(331)
  }

  @Test
  fun testtryParsePositiveInt_HappyPath_Decimal() {
    val i = CharUtils.tryParsePositiveInt("1.1")
    assert(i).isEqualTo(-1)
  }

  @Test
  fun testtryParsePositiveInt_HappyPath_Large() {
    val i = CharUtils.tryParsePositiveInt("12354312")
    assert(i).isEqualTo(12354312)
  }

  @Test
  fun testtryParsePositiveInt_HappyPath_Negative() {
    val i = CharUtils.tryParsePositiveInt("-5")
    assert(i).isEqualTo(-1)
  }

  @Test
  fun testtryParsePositiveInt_HappyPath_Zero() {
    val i = CharUtils.tryParsePositiveInt("0")
    assert(i).isEqualTo(0)
  }

  @Test
  fun testtryParsePositiveInt_NonHappyPath() {
    val i = CharUtils.tryParsePositiveInt("33d1")
    assert(i).isEqualTo(-1)
  }

  @Test
  fun testtryParsePositiveInt_Perf() {
    val attempts = arrayOfNulls<String>(2000000)
    val combinations = charArrayOf('1', '2', '3', '4', '5', '6', '.', '3', '-')
    val random = Random()
    var i = 0
    for (attempt in attempts) {
      val string = StringBuilder()
      val digits = random.nextInt(1..6)
      for (d in 0 until digits) {
        val pickedChar = combinations[random.nextInt(0 until combinations.size-1)]
        string.append(pickedChar)
      }
      attempts[i++] = string.toString()
    }

    val stopwatch = Stopwatch()
    for (attempt in attempts) {
      val parsed = attempt?.toIntOrNull()
    }
    val elapsed = stopwatch.elapsed

    stopwatch.restart()
    for (attempt in attempts) {
      val parsed = CharUtils.tryParsePositiveInt(attempt)
    }
    val elapsedMe = stopwatch.elapsed

    println(elapsedMe.toDouble() / elapsed)
  }
}
