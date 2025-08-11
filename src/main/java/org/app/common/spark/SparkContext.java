package org.app.common.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkContext {
    public static JavaSparkContext getSparkContext(String appName) {
        SparkConf sparkConf = new SparkConf().setAppName(appName);
        return new JavaSparkContext(sparkConf);
    }
}
