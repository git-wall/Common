package org.app.common.utils

import org.app.common.utils.KStrUtils.clean
import javax.servlet.http.HttpServletRequest

object KHttpServletRequestProvider {
    fun HttpServletRequest.findFirstValidHeader(vararg strs: String): String? {
        return strs.firstNotNullOfOrNull { clean(getHeader(it)) }
    }

    fun HttpServletRequest.findFirstValidHeader(strs: Array<String>): String? {
        return strs.firstNotNullOfOrNull { clean(getHeader(it)) }
    }
}