package com.shanergy.bprev.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    StoredObject upload(MultipartFile file) throws IOException;

    default PresignedUpload createPresignedUpload(String originalName, String contentType) throws IOException {
        throw new UnsupportedOperationException("Presigned upload not supported");
    }

    class StoredObject {
        private final String key;
        private final String url;
        private final String contentType;
        private final long size;
        private final String originalName;

        public StoredObject(String key, String url, String contentType, long size, String originalName) {
            this.key = key;
            this.url = url;
            this.contentType = contentType;
            this.size = size;
            this.originalName = originalName;
        }

        public String getKey() { return key; }
        public String getUrl() { return url; }
        public String getContentType() { return contentType; }
        public long getSize() { return size; }
        public String getOriginalName() { return originalName; }
    }

    class PresignedUpload {
        private final String key;
        private final String uploadUrl;
        private final String downloadUrl;
        private final String contentType;
        private final long expiresInSeconds;

        public PresignedUpload(String key, String uploadUrl, String downloadUrl, String contentType, long expiresInSeconds) {
            this.key = key;
            this.uploadUrl = uploadUrl;
            this.downloadUrl = downloadUrl;
            this.contentType = contentType;
            this.expiresInSeconds = expiresInSeconds;
        }

        public String getKey() { return key; }
        public String getUploadUrl() { return uploadUrl; }
        public String getDownloadUrl() { return downloadUrl; }
        public String getContentType() { return contentType; }
        public long getExpiresInSeconds() { return expiresInSeconds; }
    }
}
