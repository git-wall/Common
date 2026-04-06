package org.app.database.elastic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Closeable;
import java.io.IOException;

/**
 * Lifecycle manager for {@link RestHighLevelClient} (ES 7.x).
 * Create once at startup, close on shutdown.
 *
 * <pre>
 * EsClientFactory factory = EsClientFactory.of(EsConfig.builder().build());
 * RestHighLevelClient client = factory.client();
 * // ...
 * factory.close();
 * </pre>
 */
public final class EsClientFactory implements Closeable {

    private final RestHighLevelClient client;

    private EsClientFactory(EsConfig cfg) {
        HttpHost[] hosts = cfg.getHosts().stream()
            .map(h -> {
                String[] parts = h.split(":");
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
                return new HttpHost(parts[0], port, cfg.getScheme());
            })
            .toArray(HttpHost[]::new);

        RestClientBuilder builder = RestClient.builder(hosts)
            .setRequestConfigCallback(req -> req
                .setConnectTimeout(cfg.getConnectTimeoutMs())
                .setSocketTimeout(cfg.getSocketTimeoutMs())
                .setConnectionRequestTimeout(cfg.getConnectionRequestMs()))
            .setHttpClientConfigCallback(http -> {
                http.setMaxConnTotal(cfg.getMaxConnTotal());
                http.setMaxConnPerRoute(cfg.getMaxConnPerRoute());
                if (cfg.getUsername() != null) {
                    var creds = new BasicCredentialsProvider();
                    creds.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(cfg.getUsername(), cfg.getPassword()));
                    http.setDefaultCredentialsProvider(creds);
                }
                return http;
            });

        this.client = new RestHighLevelClient(builder);
    }

    public static EsClientFactory of(EsConfig config) {
        return new EsClientFactory(config);
    }

    public RestHighLevelClient client() { return client; }

    /** Convenience: ping to verify connection. */
    public boolean ping() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() {
        try { client.close(); } catch (IOException ignored) {}
    }
}
