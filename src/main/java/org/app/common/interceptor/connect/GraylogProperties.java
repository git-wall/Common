package org.app.common.interceptor.connect;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "graylog")
@Getter
@Setter
public class GraylogProperties {
    private String host;
    private int port;
    private int maxChunkSize;
    private boolean useCompression;
    private String originHost;
    private boolean includeRawMessage;
    private boolean includeMarker;
    private boolean includeMdcData;
    private boolean includeCallerData;
    private boolean includeRootCauseData;
    private boolean includeLevelName;
}
