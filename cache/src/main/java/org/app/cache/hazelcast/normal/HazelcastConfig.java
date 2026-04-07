package org.app.cache.hazelcast.normal;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Arrays;

public class HazelcastConfig {


    public Config hazelcastConfig(String clusterName,
                                  int port,
                                  String members,
                                  int syncBackupCount,
                                  int asyncBackupCount) {
        Config config = new Config();
        config.setClusterName(clusterName);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(port);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).setMembers(Arrays.asList(members.split(",")));

        // Dynamic backup configuration based on properties
        config.getMapConfig("default")
            .setBackupCount(syncBackupCount) // Dynamic synchronous replicas
            .setAsyncBackupCount(asyncBackupCount); // Dynamic asynchronous replicas

        return config;
    }

    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
