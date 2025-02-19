package org.app.common.utils;

import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
    private NetworkUtils() {
    }

    @SneakyThrows(value = UnknownHostException.class)
    public static String getLocalHostAddress() {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @SneakyThrows(value = UnknownHostException.class)
    public static String getLocalHostName() {
        return InetAddress.getLocalHost().getHostName();
    }
}
