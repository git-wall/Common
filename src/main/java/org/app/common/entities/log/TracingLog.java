package org.app.common.entities.log;

import lombok.Data;
import org.app.common.context.TracingContext;
import org.app.common.interceptor.log.InterceptorLog;
import org.app.common.trace.Trace;
import org.app.common.utils.JacksonUtils;
import org.app.common.utils.RequestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Data
public class TracingLog implements Serializable {
    private static final long serialVersionUID = 8412664333538998193L;
    private String requestId;
    private String tracID;
    private InterceptorLog.LogType type;
    private String url;
    private String method;
    private String request;
    private long executeDuration;
    private String response;
    private String userAddress;
    private String curl;

    public static TracingLog of(HttpServletRequest hsr,
                                ProceedingJoinPoint jp,
                                InterceptorLog il,
                                Trace trace) {
        var tracingLog = new TracingLog();
        tracingLog.setRequestId(TracingContext.getRequestId());
        tracingLog.setTracID(trace.getId());
        tracingLog.setRequest(RequestUtils.requestAsString(jp));
        tracingLog.setUrl(RequestUtils.getUrlNoParams(hsr));
        tracingLog.setMethod(((MethodSignature) jp.getSignature()).getMethod().getName());
        tracingLog.setType(il.type());
        tracingLog.setUserAddress(RequestUtils.getRemoteAddress(hsr));
        tracingLog.setCurl(RequestUtils.getCurl());
        return tracingLog;
    }

    public void enrich(long executeDuration, Object result) {
        var response = result instanceof ResponseEntity ? ((ResponseEntity<?>) result).getBody() : result;
        this.response = JacksonUtils.toJson(response);
        this.executeDuration = executeDuration;
    }
}
