package ru.nechaev.pasteshare.service;

public interface S3Service {
    byte[] getObject(String bucketName, String key);

    void putObject(String bucketName, String key, byte[] text);
}
