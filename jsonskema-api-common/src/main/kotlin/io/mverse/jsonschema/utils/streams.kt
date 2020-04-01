package io.mverse.jsonschema.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
expect fun InputStream.asInput():Input
expect fun interrupt()

expect fun createDispatcher(name:String = "dispatcher-%d", poolSize:Int = 30): CoroutineDispatcher