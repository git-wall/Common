1️⃣ Nguồn dữ liệu → Kafka (Ingest / CDC / Collect)
🔹 Data ingest / ETL / Flow

Apache NiFi

Apache Flume (cũ)

Logstash

Fluentd / Fluent Bit

Vector

Beats (Filebeat, Metricbeat)

🔹 CDC (Change Data Capture)

Debezium

Maxwell

Canal (Alibaba – MySQL)

Oracle GoldenGate

2️⃣ Kafka core & quản lý

Apache Kafka

Kafka Connect

Kafka Streams

Schema Registry

ksqlDB

MirrorMaker 2

Cruise Control

Burrow (consumer lag)

3️⃣ Stream Processing (đây là mớ bạn đang nhớ)
🔥 Real-time / Near real-time

Apache Flink

Apache Spark Streaming / Structured Streaming

Kafka Streams

ksqlDB

Apache Samza

Apache Storm (cũ)

🔥 Complex / Stateful processing

Flink CEP

Beam (runner: Flink / Spark)

Hazelcast Jet (đã merge vào Hazelcast)

4️⃣ Storage – Object / Lake / File
🟢 Object Storage

Amazon S3

MinIO

GCS

Azure Blob

HDFS

🟢 Data Lake Table Format

Apache Iceberg

Apache Hudi

Delta Lake

5️⃣ OLAP / Analytics Database
⚡ Real-time analytics

ClickHouse

Apache Druid

Apache Pinot

Rockset

🧊 Batch analytics

Apache Kylin

Presto / Trino (query engine)

Spark SQL

6️⃣ Search / Log / Observability

Elasticsearch

OpenSearch

Splunk

Graylog

Loki (Grafana)

OpenTSDB

7️⃣ Warehouse / BI

Snowflake

BigQuery

Redshift

Synapse

Apache Hive

Apache Impala

8️⃣ Time-series / Metrics

Prometheus

InfluxDB

VictoriaMetrics

TimescaleDB

9️⃣ ML / Feature / AI pipeline

Feast (Feature Store)

TensorFlow Data Validation

Hopsworks

Spark MLlib

Flink ML

Kubeflow Pipelines

🔟 Orchestration / Workflow

Apache Airflow

Argo Workflows

Apache Oozie (cũ)

Prefect

Dagster

1️⃣1️⃣ Query / Serving Layer

Apache Superset

Metabase

Grafana

Redash

Tableau

Power BI

1️⃣2️⃣ Governance / Catalog / Metadata

Apache Atlas

Amundsen

DataHub

Glue Data Catalog

1️⃣3️⃣ Security / Quality / Control

Apache Ranger

Apache Knox

Great Expectations

Deequ

1️⃣4️⃣ Container / Infra

Kubernetes

Helm

Strimzi (Kafka on K8s)

Confluent Platform

Redpanda

Pulsar (Kafka-alike)

1️⃣5️⃣ Tổng hợp cực nhanh (cheat sheet)
Ingest      : NiFi, Debezium
Stream      : Flink, Spark, Kafka Streams
Storage     : S3, MinIO
Lake Table  : Iceberg, Hudi
OLAP        : ClickHouse, Druid
Search      : Elasticsearch
Workflow    : Airflow
Query       : Trino, Spark SQL
Viz         : Grafana, Superset