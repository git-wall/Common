package org.app.common.flink.data;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.function.Consumer;

/**
 * Helper class for working with Flink DataStreamSource.
 * Provides generic methods for common stream operations like filter, map, etc.
 */
public class DataHelper {
    /**
     * Creates a DataStreamSource from a Kafka source.
     *
     * @param env         The StreamExecutionEnvironment
     * @param kafkaSource The Kafka source
     * @param sourceName  Name for the source
     * @param <T>         Type of the stream elements
     * @return A DataStreamSource
     */
    public static <T> DataStreamSource<T> addSource(
            StreamExecutionEnvironment env,
            KafkaSource<T> kafkaSource,
            String sourceName) {
        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), sourceName);
    }

    /**
     * Applies a filter to a DataStreamSource.
     *
     * @param source The source stream
     * @param filter The filter function
     * @param <T>    Type of the stream elements
     * @return Filtered stream
     */
    public static <T> SingleOutputStreamOperator<T> filter(
            DataStreamSource<T> source,
            FilterFunction<T> filter) {
        return source.filter(filter);
    }

    /**
     * Applies a map transformation to a DataStreamSource.
     *
     * @param source    The source stream
     * @param mapper    The map function
     * @param <T>       Input type
     * @param <R>       Output type
     * @return Mapped stream
     */
    public static <T, R> SingleOutputStreamOperator<R> map(
            DataStreamSource<T> source,
            MapFunction<T, R> mapper) {
        return source.map(mapper);
    }

    /**
     * Applies a flatMap transformation to a DataStreamSource.
     *
     * @param source    The source stream
     * @param flatMapper The flatMap function
     * @param <T>       Input type
     * @param <R>       Output type
     * @return FlatMapped stream
     */
    public static <T, R> SingleOutputStreamOperator<R> flatMap(
            DataStreamSource<T> source,
            FlatMapFunction<T, R> flatMapper) {
        return source.flatMap(flatMapper);
    }

    /**
     * Groups a DataStreamSource by key.
     *
     * @param source       The source stream
     * @param keySelector  The key selector function
     * @param <T>          Type of the stream elements
     * @param <K>          Type of the key
     * @return Keyed stream
     */
    public static <T, K> KeyedStream<T, K> keyBy(
            DataStreamSource<T> source,
            KeySelector<T, K> keySelector) {
        return source.keyBy(keySelector);
    }

    /**
     * Applies a reduce operation to a DataStreamSource.
     *
     * @param source    The source stream
     * @param reducer   The reduce function
     * @param <T>       Type of the stream elements
     * @return Reduced stream
     */
    public static <T> SingleOutputStreamOperator<T> reduce(
            DataStreamSource<T> source,
            ReduceFunction<T> reducer) {
        return source.keyBy(t -> 1).reduce(reducer);
    }


    /**
     * Prints the elements of a DataStreamSource.
     *
     * @param source    The source stream
     * @param <T>       Type of the stream elements
     * @return The same stream after printing
     */
    public static <T> DataStreamSource<T> print(DataStreamSource<T> source) {
        source.print();
        return source;
    }

    /**
     * Applies a custom transformation to a DataStreamSource.
     *
     * @param source         The source stream
     * @param transformation The transformation function
     * @param <T>            Type of the stream elements
     * @param <R>            Type of the result elements
     * @return Transformed stream
     */
    public static <T, R> DataStream<R> transform(
            DataStreamSource<T> source,
            java.util.function.Function<DataStreamSource<T>, DataStream<R>> transformation) {
        return transformation.apply(source);
    }

    /**
     * Performs a side effect on each element of the stream.
     *
     * @param source    The source stream
     * @param action    The action to perform
     * @param <T>       Type of the stream elements
     * @return The same stream after applying the action
     */
    public static <T> SingleOutputStreamOperator<T> forEach(
            DataStreamSource<T> source,
            Consumer<T> action) {
        return source.map((MapFunction<T, T>) value -> {
            action.accept(value);
            return value;
        });
    }
}
