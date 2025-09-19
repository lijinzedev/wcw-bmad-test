package com.shanergy.bprev.service;

import com.shanergy.bprev.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class MinioStorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final StorageProperties properties;
    private final MinioClient client;
    private final boolean enabled;

    public MinioStorageService(StorageProperties properties) {
        this.properties = properties;
        if (StringUtils.hasText(properties.getEndpoint()) &&
                StringUtils.hasText(properties.getAccessKey()) &&
                StringUtils.hasText(properties.getSecretKey()) &&
                StringUtils.hasText(properties.getBucket())) {
            this.client = MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();
            this.enabled = true;
            ensureBucket();
        } else {
            this.client = null;
            this.enabled = false;
            log.warn("MinIO configuration missing, using temporary filesystem storage for uploads");
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
        } catch (Exception ex) {
            log.error("Failed to ensure MinIO bucket {}: {}", properties.getBucket(), ex.getMessage());
            throw new IllegalStateException("Unable to initialize storage bucket", ex);
        }
    }

    @Override
    public StoredObject upload(MultipartFile file) throws IOException {
        if (enabled) {
            return uploadToMinio(file);
        }
        return storeLocally(file);
    }

    private StoredObject uploadToMinio(MultipartFile file) throws IOException {
        String objectName = buildObjectKey(file.getOriginalFilename());
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try (InputStream in = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentType)
                    .build();
            client.putObject(args);
        } catch (Exception ex) {
            throw new IOException("Failed to upload object to MinIO", ex);
        }
        String url = buildPublicUrl(objectName);
        return new StoredObject(objectName, url, contentType, file.getSize(), file.getOriginalFilename());
    }

    @Override
    public PresignedUpload createPresignedUpload(String originalName, String contentType) throws IOException {
        if (!enabled) {
            throw new UnsupportedOperationException("Presigned upload not available without MinIO configuration");
        }
        String objectName = buildObjectKey(originalName);
        String type = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        int expirySeconds = 3600;
        try {
            String uploadUrl = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .method(Method.PUT)
                            .expiry(expirySeconds)
                            .build()
            );
            String downloadUrl;
            if (StringUtils.hasText(properties.getPublicUrl())) {
                downloadUrl = buildPublicUrl(objectName);
            } else {
                downloadUrl = client.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(properties.getBucket())
                                .object(objectName)
                                .method(Method.GET)
                                .expiry(expirySeconds)
                                .build()
                );
            }
            return new PresignedUpload(objectName, uploadUrl, downloadUrl, type, expirySeconds);
        } catch (Exception ex) {
            throw new IOException("Failed to generate presigned url", ex);
        }
    }

    private StoredObject storeLocally(MultipartFile file) throws IOException {
        Path baseDir = Path.of(System.getProperty("java.io.tmpdir"), "hazard-uploads");
        Files.createDirectories(baseDir);
        String objectName = buildObjectKey(file.getOriginalFilename());
        Path target = baseDir.resolve(objectName.replace('/', '_'));
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        String url = "file:" + target.toAbsolutePath();
        return new StoredObject(objectName, url, file.getContentType(), file.getSize(), file.getOriginalFilename());
    }

    private static String buildObjectKey(String originalName) {
        String clean = StringUtils.hasText(originalName) ? originalName.replaceAll("\\s+", "_") : "attachment";
        return UUID.randomUUID() + "/" + clean;
    }

    private String buildPublicUrl(String objectName) {
        if (StringUtils.hasText(properties.getPublicUrl())) {
            String prefix = properties.getPublicUrl().endsWith("/") ? properties.getPublicUrl() : properties.getPublicUrl() + "/";
            return prefix + objectName;
        }
        return objectName;
    }
}
