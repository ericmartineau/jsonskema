package lang.time

class Stopwatch() {
  var start = currentTime()
  var stop: Long? = null
  val elapsed: Long
    get() {
      if (stop == null) {
        stop()
      }
      return stop!! - start
    }

  fun stop() {
    stop = currentTime()
  }

  fun restart() {
    start = currentTime()
    stop = null
  }
}
