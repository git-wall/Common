package org.app.database.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import lombok.Getter;

@Getter
public class CouchbaseFactory {

    private final Cluster cluster;
    private final Bucket bucket;
    private final Collection collection;

    public CouchbaseFactory(String connectionString, String username, String password, String bucketName) {
        this.cluster = Cluster.connect(
            connectionString,
            ClusterOptions.clusterOptions(username, password)
        );
        this.bucket = cluster.bucket(bucketName);
        this.collection = bucket.defaultCollection();
    }
}
