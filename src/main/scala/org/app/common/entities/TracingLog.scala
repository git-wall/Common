package org.app.common.entities

case class TracingLog(
                       requestId: String,
                       request: String,
                       response: String,
                       method: String,
                       uri: String,
                       status: Int,
                       durationMs: Long,
                       logType: LogType)
