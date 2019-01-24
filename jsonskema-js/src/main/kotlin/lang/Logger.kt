package lang

actual class Logger actual constructor(name: String) {
  actual fun warn(msg: String) {
    console.log(msg)
  }
}
