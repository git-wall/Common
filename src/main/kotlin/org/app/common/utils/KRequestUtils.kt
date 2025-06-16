package org.app.common.utils

import org.app.common.utils.KHttpServletRequestProvider.findFirstValidHeader
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

object KRequestUtils {
    const val REQUEST_ID: String = "X-Request-ID"

    const val TOKEN_HEADER: String = "Authorization"
    const val TOKEN_PREFIX: String = "Bearer "

    // device id
    const val DCM_GU_ID: String = "x-dcmguid"

    const val SUB_NO: String = " x-up-subno"

    const val J_PHONE_UID: String = "x-jphone-uid"

    const val EM_UID: String = "x-em-uid"

    val IP_HEADER_CANDIDATES: Array<String> = arrayOf(
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    )

    val HOST_HEADER: Array<String> = arrayOf(
        "Host",
        "Origin",
        "Referer",
        "X-Forwarded-Host",
        "X-Forwarded-Proto"
    )

    fun getHttpServletRequest(): HttpServletRequest? {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        return attributes?.request
    }

    fun getToken(): String? {
        return getToken(this.getHttpServletRequest())
    }

    fun getToken(request: HttpServletRequest?): String? {
        return request?.getHeader(TOKEN_HEADER)?.takeIf { it.isNotEmpty() && it.startsWith(TOKEN_PREFIX) }
    }

    fun getRequestId(): String? {
        return getRequestId(this.getHttpServletRequest())
    }

    fun getRequestId(request: HttpServletRequest?): String? {
        return request?.getHeader(REQUEST_ID)
    }

    fun getDeviceId(): String? = getDeviceId(this.getHttpServletRequest())

    fun getDeviceId(request: HttpServletRequest?): String? =
        request?.findFirstValidHeader(DCM_GU_ID, J_PHONE_UID, EM_UID, SUB_NO)

    fun getRemoteAddr(): String? = getRemoteAddr(this.getHttpServletRequest())

    fun getRemoteAddr(request: HttpServletRequest?): String? {
        return request?.findFirstValidHeader(IP_HEADER_CANDIDATES)
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: request?.remoteAddr
    }

    fun getFullUrl(): String? = getFullUrl(this.getHttpServletRequest())

    fun getFullUrl(request: HttpServletRequest?): String? {
        val query = request?.queryString
        val url = request?.requestURL?.toString()
        return if (query != null) url?.split("\\?")?.firstOrNull() else url
    }

    fun getUrl(): String? = getUrl(this.getHttpServletRequest())

    fun getUrl(request: HttpServletRequest?): String? {
        return request?.requestURL?.toString()
    }

    fun getDomain(): String? = getDomain(this.getHttpServletRequest())

    fun getDomain(request: HttpServletRequest?): String? {
        val host = request?.findFirstValidHeader(HOST_HEADER)

        return host?.takeIf { it.contains(":") }?.substring(0, host.indexOf(':'))
    }

    fun getRequestHeaders(): String? = getRequestHeaders(this.getHttpServletRequest())

    fun getRequestHeaders(request: HttpServletRequest?): String? {
        if (request == null) return ""
        val map: MutableMap<String?, String?> = HashMap()
        val headerNames = request.headerNames
        while (headerNames.hasMoreElements()) {
            val key = headerNames.nextElement()
            val value = request.getHeader(key)
            map.put(key, value)
        }
        return map.toString()
    }
}