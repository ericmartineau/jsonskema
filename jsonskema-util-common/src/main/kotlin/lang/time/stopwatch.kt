package lang.time

class Stopwatch() {
  var startedAt = currentTime()
  var stoppedAt: Long? = null
  val elapsed: Long
    get() {
      if (stoppedAt == null) {
        stop()
      }
      return stoppedAt!! - startedAt
    }

  fun stop() {
    stoppedAt = currentTime()
  }

  fun restart() {
    startedAt = currentTime()
    stoppedAt = null
  }
}
