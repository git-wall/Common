package org.app.common.support

import org.apache.http.config.Lookup
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

object Find {
    fun <T> lookup(function: () -> T): T {
        return function()
    }

    fun lookupField(clazz: Class<*>): Lookup<Field?> {
        return Lookup { n: String ->
            try {
                return@Lookup clazz.getDeclaredField(n)
            } catch (e: NoSuchFieldException) {
                throw java.lang.IllegalArgumentException("Field not found: $n", e)
            }
        }
    }

    fun lookupMethod(clazz: Class<*>): Lookup<Array<Method>> {
        return Lookup { n: String? ->
            val methods = clazz.getDeclaredMethods().filter { method: Method -> method.name == n }.toTypedArray()
            require(methods.isNotEmpty()) { "Method not found: $n" }
            methods
        }
    }

    fun lookupResource(loader: ClassLoader): Lookup<InputStream> {
        return Lookup { n: String ->
            val inputStream = loader.getResourceAsStream(n)
            require(inputStream != null) { "Resource not found: $n" }
            return@Lookup inputStream
        }
    }

    fun lookupProperties(properties: Properties): Lookup<String> {
        return Lookup { n: String ->
            val p = properties.getProperty(n)
            require(p != null) { "Properties not found: $p" }
            return@Lookup p
        }
    }

    fun <T, R> transfer(lookup: Lookup<T>, transformer: (T) -> R): Lookup<R> {
        return Lookup { n: String ->
            val value = lookup.lookup(n)
            return@Lookup transformer.invoke(value)
        }
    }
}