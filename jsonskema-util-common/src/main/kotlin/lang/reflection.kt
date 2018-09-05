package lang

import kotlin.reflect.KClass

expect fun <T:Any> KClass<T>.newInstance():T
