package org.app.common.action;

import org.app.common.db.QuerySupplier;
import org.app.common.filter.CuckooFilter;
import org.app.common.utils.HttpClientUtils;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CuckooIPBlacklistManager<T> {

    private final CuckooFilter<String> cuckooFilter;
    private final RedisTemplate<String, String> redisTemplate;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final QuerySupplier<T> query;
    private final String apiKey;
    private final long redisTTLSeconds;
    private static final String ABUSE_API = "https://api.abuseipdb.com/api/v2/check";

    public CuckooIPBlacklistManager(RedisTemplate<String, String> redisTemplate,
                                    QuerySupplier<T> query,
                                    Environment env,
                                    int capacity) {
        this.redisTemplate = redisTemplate;
        this.query = query;
        this.apiKey = env.getProperty("abuseipdb.api.key");
        this.redisTTLSeconds = env.getProperty("ip.blacklist.redis.ttl:3600", Long.class, 3600L);
        this.cuckooFilter = new CuckooFilter<>(capacity);
    }

    public void registerIp(String ip) {
        cuckooFilter.insert(ip);
        redisTemplate.opsForValue().set(ip, "BLACKLISTED", redisTTLSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String ip) {
        if (!cuckooFilter.contains(ip)) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(ip));
    }

    public boolean checkAndRegisterFromAbuse(String ip) throws IOException, InterruptedException {
        HttpRequest request = HttpClientUtils.requestGet(ABUSE_API + "?ipAddress=" + ip + "&maxAgeInDays=90", apiKey);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        int score = json.getJSONObject("data").getInt("abuseConfidenceScore");

        if (score >= 50) {
            registerIp(ip);
            return true;
        }
        return false;
    }

    @KafkaListener(topics = "ip-traffic-events", groupId = "cuckoo-ip-checker")
    public void listenToTraffic(String ip) throws IOException, InterruptedException {
        if (!isBlacklisted(ip)) {
            checkAndRegisterFromAbuse(ip);
        }
    }

    @Scheduled(fixedRateString = "${ip.blacklist.refresh.rate:3600000}")
    public void refreshFromKnownSources() {
        cuckooFilter.clear();
        List<String> knownIps = query.getFindFields().get();
        knownIps.forEach(this::registerIp);
    }

    public void clear() {
        cuckooFilter.clear();
    }

    public CuckooFilter<String> getFilter() {
        return cuckooFilter;
    }
}
