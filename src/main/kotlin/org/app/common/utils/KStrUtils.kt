package org.app.common.utils

object KStrUtils {
    fun clean(str: String?): String? = str?.trim()?.takeIf { it.isNotEmpty() }

    fun clean(str: String): String = str.trim().takeIf { it.isNotEmpty() } ?: ""

    fun clean(str: String, default: String): String = str.trim().takeIf { it.isNotEmpty() } ?: default

    fun clean(strs: Array<String>): String {
        return strs.first { isClean(it) }
    }

    fun clean(strs: Array<String>, default: String): String {
        return strs.firstOrNull { isClean(it) } ?: default
    }

    fun isClean(str: String?): Boolean = clean(str) != null

    fun isEmpty(str: String?): Boolean = str == null || str.isEmpty()

    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)

    fun isBlank(str: String?): Boolean = str == null || str.isBlank()

    fun isNotBlank(str: String?): Boolean = !isBlank(str)
}