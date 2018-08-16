package io.mverse.jsonschema.utils

import kotlinx.io.Writer

private val NEWLINE = '\n'

class IndentingWriter(private val wrapped: Writer,
                      private val indentChars: String) : Writer() {

  override fun write(ch: Int) {
    wrapped.write(ch)
    if (ch == NEWLINE.toInt()) {
      wrapped.write(indentChars)
    }
  }

  override fun close() = wrapped.close()
  override fun flush() = wrapped.flush()
  override fun write(str: String) = wrapped.write(indent(str))
  override fun write(src: CharArray, off: Int, len: Int) = wrapped.write(indent(src, off, len))

  private fun indent(input: CharArray, offset: Int, length: Int): String {
    return indent(input.copyOfRange(offset, offset + length).contentToString())
  }

  private fun indent(input: CharSequence): String {
    val replace = StringBuilder()
    var i = 0

    while (i < input.length) {
      val c = input[i++]
      replace.append(c)
      if (c == NEWLINE) {
        replace.append(indentChars)
      }
    }
    return replace.toString()
  }
}
