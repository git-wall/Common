package org.app.common.support

import com.fasterxml.jackson.core.type.TypeReference

object TypeRefer {
    fun <T> refer(): TypeReference<T> {
        return object : TypeReference<T>() {}
    }
}