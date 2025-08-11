package org.app.common.flink;

import lombok.SneakyThrows;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.app.common.annotation.Description;

public class StreamEnvironment {

    @Description(detail = "Builds a Flink StreamExecutionEnvironment with checkpointing enabled for env production.")
    @SneakyThrows
    public static StreamExecutionEnvironment buildEnv(String jobName, String jobVersion) {
        String checkpointPath = String.format(System.getProperty("checkpointPath"), jobName, jobVersion);
        String template = System.getProperty("java.io.tmpdir");

        Configuration config = new Configuration();
        config.setString(CheckpointingOptions.CHECKPOINTS_DIRECTORY.key(), checkpointPath);
        config.setString(CheckpointingOptions.SAVEPOINT_DIRECTORY.key(), "file:///tmp/flink-savepoints");

        // Create the Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);

        // Enable checkpointing
        env.enableCheckpointing(60 * 1000); // 1 minute

        // Configure checkpoint settings
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
        env.getCheckpointConfig().setCheckpointTimeout(60 * 60 * 1000); // 1 hour
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(Integer.MAX_VALUE);

        env.setDefaultSavepointDirectory(template);
        return env;
    }

    @Description(detail = "Builds a Flink StreamExecutionEnvironment with checkpointing disabled for env test.")
    @SneakyThrows
    public static StreamExecutionEnvironment buildEnv() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getCheckpointConfig().disableCheckpointing();
        return env;
    }

    public static StreamExecutionEnvironment buildEnv(Env env, String jobName, String jobVersion) {
        if (env == Env.PROD) {
            return buildEnv(jobName, jobVersion);
        }
        return buildEnv();
    }
}
