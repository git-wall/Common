package org.app.common.entities.log;

import lombok.Data;
import org.app.common.interceptor.log.InterceptorLog;

import java.io.Serializable;

@Data
public class TracingLog implements Serializable {
    private static final long serialVersionUID = 8412664333538998193L;
    private String requestId;
    private String tracID;
    private InterceptorLog.LogType[] type;
    private String url;
    private String method;
    private String request;
    private long executeDuration;
    private String response;
    private String userAddress;
}
