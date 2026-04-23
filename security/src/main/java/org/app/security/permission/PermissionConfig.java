package org.app.security.permission;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.jackson.JacksonUtils;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.casdoor.config.CasdoorConfiguration;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.watcher.RedisWatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

/**
 * casdoor:
 *   endpoint: http://casdoor:8000
 *   client-id: your-client-id
 *   client-secret: your-client-secret
 *   certificate: |
 *     -----BEGIN CERTIFICATE-----
 *     ...
 *     -----END CERTIFICATE-----
 *   organization-name: built-in
 *   application-name: your-app
 *   model:
 *     name: rbac_model   # tên model bạn tạo trên Casdoor UI
 *
 * redis:
 *   ip: redis
 *   port: 6379
 *   casbin:
 *     channel: casbin-policy-updated
 * */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(CasdoorConfiguration.class)
public class PermissionConfig {

    private final Environment env;

    private String fetchModel() {
        String endpoint     = env.getProperty("casdoor.endpoint");
        String owner        = env.getProperty("casdoor.organization-name");
        String modelName    = env.getProperty("casdoor.model.name");
        String clientId     = env.getProperty("casdoor.client-id");
        String clientSecret = env.getProperty("casdoor.client-secret");

        // Casdoor dùng Basic Auth: clientId:clientSecret
        String creds = Base64.getEncoder()
            .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        String url = endpoint + "/api/get-model?id=" + owner + "/" + modelName;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + creds)
            .GET()
            .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            // Response: {"name":"rbac_model","modelText":"[request_definition]\n...","owner":"built-in",...}
            JsonNode node = JacksonUtils.readTree(response.body());
            return node.get("modelText").asText();

        } catch (Exception e) {
            throw new RuntimeException("Cannot fetch Casdoor model", e);
        }
    }

    private String fetchModelDb(DataSource dataSource) throws Exception {
        String owner     = env.getProperty("casdoor.organization-name"); // "built-in"
        String modelName = env.getProperty("casdoor.model.name");        // "rbac_model"

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT model_text FROM model WHERE owner = ? AND name = ?"
             )) {
            ps.setString(1, owner);
            ps.setString(2, modelName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("model_text");
                }
                throw new RuntimeException("Model not found: " + owner + "/" + modelName);
            }
        }
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public Enforcer enforcer(DataSource dataSource) throws Exception {
        // 1. Định nghĩa Model (RBAC mẫu của Casbin)
        // Bạn có thể lấy nội dung này từ giao diện Model của Casdoor
//        Model model = new Model();
//        model.addDef("r", "r", "sub, obj, act");
//        model.addDef("p", "p", "sub, obj, act");
//        model.addDef("g", "g", "_, _");
//        model.addDef("e", "e", getRuleResult(env.getProperty("casbin.rule.result")));
//        model.addDef("m", "m", "g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act");

        // 1. Fetch model text từ Casdoor API
        String modelText = fetchModelDb(dataSource);

        // 2. Load model từ text thay vì hardcode addDef
        Model model = new Model();
        model.loadModelFromText(modelText);  // ← key method

        // 2. Sử dụng JDBC Adapter để đọc bảng 'casbin_rule' do Casdoor quản lý
        Adapter adapter = new JDBCAdapter(dataSource);

        // 3. Cấu hình Redis Watcher (Cái "loa" thông báo)
        // "redis:6379" là tên service redis trong docker-compose của bạn
        var redisIp = env.getProperty("redis.ip");
        var redisPort = env.getProperty("redis.port", Integer.class, 6379);
        var casbinChannel = env.getProperty("redis.casbin.channel");
        RedisWatcher watcher = new RedisWatcher(redisIp, redisPort, casbinChannel);

        Enforcer enforcer = new Enforcer(model, adapter);
        enforcer.setWatcher(watcher);
        enforcer.loadPolicy();  // 5. Tải chính sách từ DB vào RAM

        watcher.setUpdateCallback(enforcer::loadPolicy);

        return enforcer;
    }

    public enum RuleResult {
        // Mặc định CẤM.
        // Phải tìm thấy ít nhất 1 dòng "Cho phép" thì mới được vào. (Dùng cho hệ thống bảo mật cao).
        ALLOW,
        // Mặc định CHO PHÉP.
        // Chỉ cần không có dòng nào "Cấm" là được vào. (Dùng cho các hệ thống mở).
        DENY,
        // Ưu tiên CẤM.
        // Phải có dòng "Cho phép" VÀ không được có dòng "Cấm" nào đi kèm. (Dùng khi 1 người có nhiều Role mâu thuẫn).
        ALLOW_AND_NO_DENY;

        public static String getRuleResult(String finalResult) {
            var rs = RuleResult.valueOf(finalResult);
            switch (rs) {
                case ALLOW_AND_NO_DENY:
                    return "some(where (p.eft == allow)) && !some(where (p.eft == deny))";
                case DENY:
                    return "!some(where (p.eft == deny))";
                default:
                    return "some(where (p.eft == allow))";
            }
        }
    }
}
