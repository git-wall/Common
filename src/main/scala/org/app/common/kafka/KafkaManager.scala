package org.app.common.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaManager(
                    val producers: Map[String, KafkaProducer[String, String]],
                    val consumers: Map[String, KafkaConsumer[String, String]],
                  ) {
  
  def getProducer(broker: String): Option[KafkaProducer[String, String]] = {
    producers.get(broker)
  }
  
  def send(p: KafkaProducer[String, String], topic: String, key: String, value: String): Unit = {
    val record = new ProducerRecord[String, String](topic, key, value)
    p.send(record)
  }
  
  def send(broker: String, topic: String, key: String, value: String): Unit = {
    producers.get(broker).foreach(e => {
      val record = new ProducerRecord[String, String](topic, key, value)
      e.send(record)
    })
  }
}
