package org.app.common.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "couchbase.enabled", havingValue = "true")
public class CouchbaseConfig {

    @Value("${couchbase.connection-string}")
    private String connectionString;

    @Value("${couchbase.username}")
    private String username;

    @Value("${couchbase.password}")
    private String password;

    @Value("${couchbase.bucket-name}")
    private String bucketName;

    @Bean
    public Cluster couchbaseCluster() {
        return Cluster.connect(
                connectionString,
                ClusterOptions.clusterOptions(username, password)
        );
    }

    @Bean
    public Collection couchbaseCollection(Cluster cluster) {
        return cluster.bucket(bucketName).defaultCollection();
    }
}