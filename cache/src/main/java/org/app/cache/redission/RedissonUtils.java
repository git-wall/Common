package org.app.cache.redission;

import org.redisson.config.Config;

public class RedissonUtils {

    public static Config buildConfig(String address, String password) {
        address = address == null ? "redis://127.0.0.1:6379" : address;
        Config config = new Config();
        // Redis Sever (Single, Cluster, Sentinel...)
        config.useSingleServer()
            .setAddress(address)
            .setPassword(password);
        return config;
    }
}
