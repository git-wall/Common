package org.app.common.utils;

import lombok.SneakyThrows;
import org.apache.http.HttpHost;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

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

    @SneakyThrows(value = UnknownHostException.class)
    public static byte[] getAddress() {return InetAddress.getLocalHost().getAddress();}

    public static List<HttpHost> getHosts(String strHosts, int port, String protocol) {
        String[] hosts = Objects.requireNonNull(strHosts).split(",");

        return ArrayUtils.map(hosts,host -> new HttpHost(host, port, protocol));
    }
}
