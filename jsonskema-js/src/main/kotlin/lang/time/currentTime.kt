package lang.time

import kotlin.js.Date

actual fun currentTime(): Long {
  return Date().getTime().toLong()
}
