package org.app.common.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import pureconfig.{ConfigReader, ConfigSource}

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

object KafkaAutoConfiguration {

  def load(): KafkaManager = {
    val config = ConfigSource.default.at("kafka.brokers").loadOrThrow[Map[String, BrokerConfig]]
    val producers = new ConcurrentHashMap[String, KafkaProducer[String, String]]()
    val consumers = new ConcurrentHashMap[String, KafkaConsumer[String, String]]()

    config.foreach { case (brokerId, brokerConfig) =>
      brokerConfig.`type` match {
        case BrokerType.PRODUCER_ONLY | BrokerType.BOTH =>
          val props = new Properties()
          props.put("bootstrap.servers", brokerConfig.bootstrapServers)
          props.put("key.serializer", classOf[StringSerializer].getName)
          props.put("value.serializer", classOf[StringSerializer].getName)
          brokerConfig.producer.foreach { p =>
            props.put("acks", p.acks)
            props.put("retries", p.retries.toString)
            props.put("batch.size", p.batchSize.toString)
            props.put("linger.ms", p.lingerMs.toString)
            props.put("buffer.memory", p.bufferMemory.toString)
            props.put("compression.type", p.compressionType)
          }
          val producer = new KafkaProducer[String, String](props)
          producers.put(brokerId, producer)

        case _ => // do nothing
      }

      brokerConfig.`type` match {
        case BrokerType.CONSUMER_ONLY | BrokerType.BOTH =>
          val props = new Properties()
          props.put("bootstrap.servers", brokerConfig.bootstrapServers)
          props.put("group.id", brokerConfig.groupId)
          props.put("key.deserializer", classOf[StringDeserializer].getName)
          props.put("value.deserializer", classOf[StringDeserializer].getName)
          brokerConfig.consumer.foreach { c =>
            props.put("enable.auto.commit", c.enableAutoCommit.toString)
            props.put("auto.offset.reset", c.autoOffsetReset)
            props.put("max.poll.records", c.maxPollRecords.toString)
          }
          val consumer = new KafkaConsumer[String, String](props)
          consumers.put(brokerId, consumer)

        case _ => // do nothing
      }
    }

    new KafkaManager(
      producers = scala.jdk.CollectionConverters.MapHasAsScala(producers).asScala.toMap,
      consumers = scala.jdk.CollectionConverters.MapHasAsScala(consumers).asScala.toMap
    )
  }
}
