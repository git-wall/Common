package org.app.common.kafka

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

enum BrokerType derives ConfigReader:
  case PRODUCER_ONLY, CONSUMER_ONLY, BOTH

case class ProducerSettings(
                             acks: String = "all",
                             retries: Int = 3,
                             batchSize: Int = 16384,
                             lingerMs: Int = 1,
                             bufferMemory: Int = 33554432,
                             compressionType: String = "snappy"
                           )derives ConfigReader

case class ConsumerSettings(
                             enableAutoCommit: Boolean = false,
                             autoOffsetReset: String = "earliest",
                             maxPollRecords: Int = 500
                           )derives ConfigReader

case class BrokerConfig(
                         bootstrapServers: String,
                         groupId: String,
                         `type`: BrokerType,
                         producer: Option[ProducerSettings],
                         consumer: Option[ConsumerSettings]
                       )derives ConfigReader