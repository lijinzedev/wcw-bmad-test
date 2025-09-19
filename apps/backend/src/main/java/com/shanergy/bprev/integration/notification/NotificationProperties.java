package com.shanergy.bprev.integration.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    /** 是否启用外部通知网关调用 */
    private boolean enabled = false;
    /** 通知网关基础 URL */
    private String baseUrl = "";
    /** 应用Key（占位） */
    private String appKey;
    /** 应用Secret（占位） */
    private String appSecret;
    /** 最大重试次数 */
    private int maxRetries = 3;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}

