package lang

import kotlin.reflect.KClass

actual fun <T : Any> KClass<T>.newInstance(): T {
  return this.java.newInstance()
}
