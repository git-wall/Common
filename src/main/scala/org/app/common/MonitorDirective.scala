package org.app.common

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Directive0, RouteResult}
import org.app.common.context.ContextHolder
import org.app.common.entities.{LogType, TracingLog}
import org.app.common.log.TracingQueue
import org.app.common.utils.RandomUtils

import java.time.Instant
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object MonitorDirective {
  def monitor(log: LoggingAdapter, logType: LogType): Directive0 = {
    extractRequestContext.flatMap { ctx =>
      val request = ctx.request
      val start = Instant.now.toEpochMilli

      import ctx.{executionContext, materializer}

      val reqFut = request.entity.toStrict(Duration(5, "seconds")).map(_.data.utf8String)

      mapRouteResultFuture { resultFuture =>
        resultFuture.andThen {
          case Success(RouteResult.Complete(response)) =>
            val duration = Instant.now.toEpochMilli - start
            val resFut = response.entity.toStrict(Duration(5, "seconds")).map(_.data.utf8String)
            for {
              reqBody <- reqFut
              resBody <- resFut
            } yield {
              val tracingLog = TracingLog(
                requestId = ContextHolder.get("traceId").getOrElse("unknown"),
                request = reqBody,
                response = resBody,
                method = request.method.value,
                uri = request.uri.toString(),
                status = response.status.intValue(),
                durationMs = duration,
                logType = logType
              )
              TracingQueue.poisonPills.get(logType).foreach(_.put(tracingLog))
            }
          case Success(RouteResult.Rejected(rejections)) =>
            val duration = Instant.now.toEpochMilli - start
            val message = s"[${request.method}] ${request.uri} was rejected (${rejections.size} reasons) in ${duration}ms"
            for {
              reqBody <- reqFut
            } yield {
              val tracingLog = TracingLog(
                requestId = ContextHolder.get("traceId").getOrElse("unknown"),
                request = reqBody,
                response = s"REJECTED: ${rejections.map(_.toString).mkString(", ")}",
                method = request.method.value,
                uri = request.uri.toString(),
                status = 0,
                durationMs = duration,
                logType = logType
              )
              TracingQueue.poisonPills.get(logType).foreach(_.put(tracingLog))
            }
          case Failure(ex) =>
            val duration = Instant.now.toEpochMilli - start
            val message = s"[${request.method}] ${request.uri} failed $ex"
            for {
              reqBody <- reqFut
            } yield {
              val tracingLog = TracingLog(
                requestId = ContextHolder.get("traceId").getOrElse("unknown"),
                request = reqBody,
                response = ex.getMessage,
                method = request.method.value,
                uri = request.uri.toString(),
                status = 500,
                durationMs = duration,
                logType = logType
              )
              TracingQueue.poisonPills.get(logType).foreach(_.put(tracingLog))
            }
        }
      }
    }
  }
}
