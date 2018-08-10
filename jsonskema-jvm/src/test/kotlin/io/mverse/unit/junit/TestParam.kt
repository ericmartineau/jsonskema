package io.mverse.unit.junit

import java.util.function.Function
import java.util.function.Supplier

/**
 * Used to create nice readable junit test parameters.
 */
data class TestParam<T> private constructor(private val testSubject: T, private val name: Any) {
  private val nameString: String = name.toString()
  fun get(): T = testSubject


  override fun toString(): String  = nameString

  class TestParamsBuilder<T> {
    private val testParams = mutableListOf<TestParam<T>>()

    fun addTestParam(name: String, param: T): TestParamsBuilder<T> {
      testParams.add(TestParam(param, name))
      return this
    }

    fun build(): Array<TestParam<T>> {
      return testParams.toTypedArray()
    }
  }

  companion object {

    fun <T> testParam(testSubject: T, name: Function<T, *>): TestParam<T> {
      return TestParam(testSubject, name.apply(testSubject))
    }

    fun <T> testParam(testSubject: T, name: Supplier<*>): TestParam<T> {
      return TestParam(testSubject, name)
    }

    fun <T> testParam(testSubject: T, name: String): TestParam<T> {
      return TestParam(testSubject, name)
    }

    /**
     * More convenient for wrapping within a stream
     */
    fun <T> testParam(toString: T.()->Any): (T)->TestParam<T> {
      return { testSubject -> TestParam(testSubject, toString(testSubject)) }
    }

    inline fun <reified T> builder(): TestParamsBuilder<T> {
      return TestParamsBuilder()
    }
  }
}
