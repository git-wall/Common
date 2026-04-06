package org.app.database.minio;

import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Minio {
    private final MinioClient minioClient;

    public Minio(String url, String accessKey, String secretKey) {
        this.minioClient = MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build();
    }

    @SneakyThrows
    public void createBucket(String bucketName) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    public List<String> listBuckets() {
        return minioClient.listBuckets().stream()
            .map(Bucket::name)
            .collect(Collectors.toList());
    }

    @SneakyThrows
    public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .contentType(contentType)
                .build()
        );
    }

    @SneakyThrows
    public InputStream downloadFile(String bucketName, String objectName) {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    @SneakyThrows
    public void deleteFile(String bucketName, String objectName) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    @SneakyThrows
    public List<String> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .build()
        );
        return StreamSupport.stream(results.spliterator(), false)
            .map(result -> {
                try {
                    return result.get().objectName();
                } catch (Exception e) {
                    return null;
                }
            })
            .collect(Collectors.toList());
    }

    @SneakyThrows
    public boolean isBucketExist(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }
}
