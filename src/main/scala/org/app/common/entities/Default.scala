package org.app.common.entities

import org.app.common.entities.LogType

object Default {
  def tracingLog(): TracingLog = {
    TracingLog("", "", "", "", "", 0, 0, LogType.GRAYLOG)
  }
}
