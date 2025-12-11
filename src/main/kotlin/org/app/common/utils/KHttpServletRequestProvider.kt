package org.app.common.utils

import org.app.common.utils.KStrUtils.clean
import javax.servlet.http.HttpServletRequest

object KHttpServletRequestProvider {
    fun HttpServletRequest.findFirstValidHeader(vararg str: String): String? {
        return str.firstNotNullOfOrNull { clean(getHeader(it)) }
    }
}
