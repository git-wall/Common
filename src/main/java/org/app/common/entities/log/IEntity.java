package org.app.common.entities.log;

import org.app.common.interceptor.log.LogMonitor;

public interface IEntity {
    LogMonitor.LogType[] getTracingLogType();
}
