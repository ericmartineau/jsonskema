package lang.time

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

actual fun currentTime(): Long = System.currentTimeMillis()

