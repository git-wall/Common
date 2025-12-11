package org.app.common.module.call_center.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallDefinition {
    private String id;
    private String code;
    private String name;
    private String description;
    private Integer version;
    private Repository repository;
    private Call call;
    private List<InputField> input;
    private List<OutputField> output;
    private Options options;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Repository {
        private String type; // sql, api, es, graphql, grpc, customList
        private String url;
        private String baseUrl;
        private String user;
        private String pwd;
        private String index;
        private AuthInfo auth;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Call {
        private String query;
        private String endpoint;
        private String method;
        private Map<String, Object> params;
        private Object body;
        private Object dsl;
        private List<Map<String, Object>> data;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Options {
        private Integer retry = 0;
        private Boolean retryOnAuthFail = false;
        private Integer timeout = 30000; // 30 seconds default
        private Integer limit = 1000;
        private String fallbackDefinition;
    }
}