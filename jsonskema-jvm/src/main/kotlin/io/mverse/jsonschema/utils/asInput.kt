package io.mverse.jsonschema.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.io.core.IoBuffer
import kotlinx.io.streams.asInput
import kotlinx.serialization.InternalSerializationApi
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@OptIn(InternalSerializationApi::class)
actual fun InputStream.asInput(): Input = this.asInput(IoBuffer.Pool)
actual fun interrupt() = Thread.currentThread().interrupt()

actual fun createDispatcher(name: String, poolSize: Int): CoroutineDispatcher {
  val executor = ThreadPoolExecutor(0,
      poolSize, 0, TimeUnit.MILLISECONDS, ArrayBlockingQueue(poolSize * 2), ThreadFactoryBuilder()
      .setNameFormat(name).build())

  return executor.asCoroutineDispatcher()
}