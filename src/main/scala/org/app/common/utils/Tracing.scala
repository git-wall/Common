package org.app.common.utils

import akka.http.javadsl.server.RequestContext
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import org.app.common.context.ContextHolder

object Tracing {
  val withRequestContext: Directive[Unit] = extractRequest.map { req =>
    val traceId = req.getHeader("X-Trace-Id")
      .map(_.value)
      .orElse(
        req.getHeader("X-Request-ID").map(_.value).orElse(RandomUtils.ID())
      )
      
    val userId = req.getHeader("X-User-Id").map(_.value)
    ContextHolder.set("userId", userId)
    ContextHolder.set("traceId", traceId)
  }
}
