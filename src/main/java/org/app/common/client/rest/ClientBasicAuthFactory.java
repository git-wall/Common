package org.app.common.client.rest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.app.common.client.ClientBasicAuthInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

public class ClientBasicAuthFactory extends HttpComponentsClientHttpRequestFactory {
    private final HttpHost host;
    private final String userName;
    private final String password;

    public ClientBasicAuthFactory(HttpHost host) {
        this(host, null, null);
    }

    public ClientBasicAuthFactory(HttpHost host, String userName, String password) {
        super();
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    public ClientBasicAuthFactory(HttpClient httpClient, HttpHost host) {
        this(httpClient, host, null, null);
    }

    public ClientBasicAuthFactory(HttpClient httpClient, HttpHost host, String userName, String password) {
        super(httpClient);
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    public static ClientBasicAuthFactory of(ClientBasicAuthInfo clientInfo) {
        HttpClient client = HttpClientBuilder.create()
                .setMaxConnTotal(100000)
                .setMaxConnPerRoute(10000)
                .build();

        HttpHost host = new HttpHost(clientInfo.getHostname(), clientInfo.getPort(), clientInfo.getSchema());

        return of(client, host, clientInfo);
    }

    public static ClientBasicAuthFactory of(HttpClient client, HttpHost host, ClientBasicAuthInfo clientInfo) {
        ClientBasicAuthFactory basicAuth = new ClientBasicAuthFactory(client, host, clientInfo.getUsername(), clientInfo.getPassword());
        basicAuth.setConnectTimeout(clientInfo.timeout());
        basicAuth.setConnectionRequestTimeout(clientInfo.timeout());
        basicAuth.setReadTimeout(clientInfo.timeout());
        basicAuth.setBufferRequestBody(clientInfo.isBufferRequestBody());
        return basicAuth;
    }

    @Override
    protected HttpContext createHttpContext(@NotNull HttpMethod httpMethod, @NotNull URI uri) {
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();

        // Generate a BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext localcontext = HttpClientContext.create();
        localcontext.setAuthCache(authCache);

        if (userName != null) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(userName, password));
            localcontext.setCredentialsProvider(credsProvider);
        }
        return localcontext;
    }
}
