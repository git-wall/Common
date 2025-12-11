package org.app.common.utils

object KStrUtils {
    fun clean(str: String?): String? = str?.trim()?.takeIf { it.isNotEmpty() } ?: ""

    fun cleanOrDefault(str: String, default: String): String = str.trim().takeIf { it.isNotEmpty() } ?: default

    fun firstClean(str: Array<String>): String {
        return str.first { isClean(it) }
    }

    fun cleansOrDefault(str: Array<String>, default: String): String {
        return str.firstOrNull { isClean(it) } ?: default
    }

    fun isClean(str: String?): Boolean = clean(str) != null

    fun isEmpty(str: String?): Boolean = str.isNullOrEmpty()

    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)

    fun isBlank(str: String?): Boolean = str.isNullOrBlank()

    fun isNotBlank(str: String?): Boolean = !isBlank(str)
}
