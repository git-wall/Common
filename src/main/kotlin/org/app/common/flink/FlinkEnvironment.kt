package org.app.common.flink

import org.apache.flink.configuration.CheckpointingOptions
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object FlinkEnvironment {
    fun buildEnv(name: String, version: String): StreamExecutionEnvironment {
        val checkpointPath = String.format(System.getProperty("checkpointPath"), name, version)
        val template = System.getProperty("java.io.tmpdir")

        val config = Configuration()
        config.setString(CheckpointingOptions.CHECKPOINTS_DIRECTORY.key(), checkpointPath)
        config.setString(CheckpointingOptions.SAVEPOINT_DIRECTORY.key(), "file:///tmp/flink-savepoints")

        val env = StreamExecutionEnvironment.getExecutionEnvironment(config)

        // Enable checkpointing
        env.enableCheckpointing((60 * 1000).toLong()) // 1 minute

        // Configure checkpoint settings
        env.checkpointConfig.setMinPauseBetweenCheckpoints(1000)
        env.checkpointConfig.setCheckpointTimeout((60 * 60 * 1000).toLong()) // 1 hour
        env.checkpointConfig.setMaxConcurrentCheckpoints(2)
        env.checkpointConfig.setTolerableCheckpointFailureNumber(Int.Companion.MAX_VALUE)

        env.setDefaultSavepointDirectory(template)

        return env
    }

    fun buildEnvironment(): StreamExecutionEnvironment {
        val env = StreamExecutionEnvironment.getExecutionEnvironment()
        env.checkpointConfig.disableCheckpointing()
        return env
    }
}