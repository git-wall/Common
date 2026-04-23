package org.app.network

import java.net.InetAddress

object Network {
    @JvmStatic
    fun localHostAddress(): String = InetAddress.getLocalHost().hostAddress
    @JvmStatic
    fun localHostName(): String = InetAddress.getLocalHost().hostName
    @JvmStatic
    fun localAddress(): ByteArray = InetAddress.getLocalHost().address
}
