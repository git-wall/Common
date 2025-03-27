package org.app.common.entities.log;

import org.app.common.interceptor.log.InterceptorLog;

public interface IEntity {
    InterceptorLog.LogType getTracingLogType();
}
