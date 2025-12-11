package org.app.common.entities.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.common.context.AuthContext;
import org.app.common.interceptor.log.InterceptorLog;
import org.app.common.utils.RequestUtils;
import org.app.common.utils.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {
    private String id;
    private String deviceId;
    private String accessToken;
    private String username;
    private String[] userAddress;
    private int level;
    private long time;
    private String application;
    private String headers;
    private String ip;
    private String source;
    private TracingLog tracingLog;
    private String description;

    public static final RequestLog EMPTY = new RequestLog();

    public static RequestLog of(HttpServletRequest hsr, int ordinal, TracingLog tracingLog, String application) {
        RequestLog e = new RequestLog();
        e.setId(TokenUtils.generateId("log_user", 12));
        e.setDeviceId(RequestUtils.getDeviceId(hsr));
        e.setAccessToken(RequestUtils.getToken(hsr));
        e.setUsername(AuthContext.getUserName());
        e.setUserAddress(new String[]{tracingLog.getUserAddress()});
        e.setLevel(ordinal);
        e.setTime(new Date().getTime());
        e.setHeaders(RequestUtils.getRequestHeaders(hsr));
        e.setIp(RequestUtils.getRemoteAddress(hsr));
        e.setSource(RequestUtils.getDomain(hsr));
        e.setTracingLog(tracingLog);
        e.setApplication(application);
        return e;
    }

    public InterceptorLog.LogType getTracingLogType() {
        return tracingLog.getType();
    }
}
