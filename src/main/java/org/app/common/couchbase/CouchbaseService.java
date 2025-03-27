package org.app.common.couchbase;

import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.UpsertOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CouchbaseService {

    private final Collection collection;

    public void upsert(String id, Object document) {
        collection.upsert(id, document);
    }

    public void upsertWithExpiry(String id, Object document, Duration expiry) {
        collection.upsert(id, document, UpsertOptions.upsertOptions().expiry(expiry));
    }

    public <T> T get(String id, Class<T> documentClass) {
        GetResult result = collection.get(id);
        return result.contentAs(documentClass);
    }

    public void remove(String id) {
        collection.remove(id);
    }

    public boolean exists(String id) {
        return collection.exists(id).exists();
    }
}