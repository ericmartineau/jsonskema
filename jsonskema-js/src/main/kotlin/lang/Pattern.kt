package lang

import kotlin.js.RegExp

actual class Pattern actual constructor(actual val regex: String) {
  private val reg = RegExp(regex)
  actual fun find(subject: String): Boolean = reg.test(subject)
}
