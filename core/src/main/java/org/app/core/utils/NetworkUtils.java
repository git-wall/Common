package org.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.UnknownHostException;

// can extend to develop more methods
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class NetworkUtils {

    @SneakyThrows(value = UnknownHostException.class)
    public static String getLocalHostAddress() {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @SneakyThrows(value = UnknownHostException.class)
    public static String getLocalHostName() {
        return InetAddress.getLocalHost().getHostName();
    }

    @SneakyThrows(value = UnknownHostException.class)
    public static byte[] getAddress() {
        return InetAddress.getLocalHost().getAddress();
    }
}
