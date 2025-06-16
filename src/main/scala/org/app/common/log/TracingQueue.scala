package org.app.common.log

import akka.event.{LogSource, Logging, LoggingAdapter}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.app.common.design.PoisonPill
import org.app.common.entities.{Default, LogType, TracingLog}
import org.app.common.kafka.{KafkaAutoConfiguration, KafkaManager}
import org.app.common.utils.RandomUtils

import java.util.Properties
import scala.util.Random

object TracingQueue {
  
  private val system = akka.actor.ActorSystem("application.name", ConfigFactory.load())

  implicit val logSource: LogSource[TracingQueue.type] = (t: TracingQueue.type) => "TracingQueue"

  val log: LoggingAdapter = Logging(system, TracingQueue)
  /**
   * sys.addShutdownHook {
   * println("Shutting down app, stopping LoggingWorker...")
   * isRunning=false
   * }
   * Add this to your main application to ensure graceful shutdown.
   * */
  @volatile var isRunning: Boolean = true // flag for clean shutdown

  // ✅ Load KafkaManager from KafkaAutoConfiguration
  private lazy val kafkaManager: KafkaManager = KafkaAutoConfiguration.load()
  
  val poisonPills: Map[LogType, PoisonPill[TracingLog]] = {
    Map(
      LogType.GRAYLOG -> initTracingLogQueue(system.name),
      LogType.KAFKA -> initTracingLogKafkaQueue(system.name)
    )
  }
  
  def close(): Unit = {
    poisonPills.values.foreach(_.stop())
    log.info("All tracing queues have been stopped and Kafka manager closed.")
  }

  private def initTracingLogQueue(application: String): PoisonPill[TracingLog] = {
    val worker = new PoisonPill[TracingLog](
      "TracingLogWorker",
      Default.tracingLog(),
      tracingLog => {
        val token = RandomUtils.ID()
        val key = s"$application:$token"
        val message = s"[$key] Processing log: $tracingLog"
        log.info(message)
      })

    worker.start()
    worker
  }

  private def initTracingLogKafkaQueue(application: String): PoisonPill[TracingLog] = {
    kafkaManager.getProducer("tracing") match {
      case Some(producer) =>
        val worker = new PoisonPill[TracingLog](
          "TracingLogKafkaWorker",
          Default.tracingLog(),
          tracingLog => {
            val token = RandomUtils.ID()
            val key = s"$application:$token"
            val message = s"[$key] Processing log: $tracingLog"

            val record = new ProducerRecord[String, String]("tracing-topic", key, message)
            producer.send(record)
          })

        worker.start()
        worker

      case None =>
        throw new RuntimeException("Kafka producer for 'tracing' not found.")
    }
  }
}
