package com.shanergy.bprev.integration.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {

    /** 是否启用监控系统轮询 */
    private boolean enabled = false;

    /** 外部监控系统基础 URL */
    private String baseUrl = "";

    /** 访问外部监控系统所需的 API Key（可选） */
    private String apiKey;

    /** 轮询间隔毫秒数 */
    private long pollDelayMs = 60_000L;

    /** 每次轮询回溯的分钟数 */
    private int lookbackMinutes = 10;

    /** SLA评估固定延迟毫秒数 */
    private long slaEvalDelayMs = 300_000L; // 5 minutes

    /** SLA评估回溯窗口（分钟） */
    private int slaLookbackMinutes = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public long getPollDelayMs() {
        return pollDelayMs;
    }

    public void setPollDelayMs(long pollDelayMs) {
        this.pollDelayMs = pollDelayMs;
    }

    public int getLookbackMinutes() {
        return lookbackMinutes;
    }

    public void setLookbackMinutes(int lookbackMinutes) {
        this.lookbackMinutes = lookbackMinutes;
    }

    public long getSlaEvalDelayMs() {
        return slaEvalDelayMs;
    }

    public void setSlaEvalDelayMs(long slaEvalDelayMs) {
        this.slaEvalDelayMs = slaEvalDelayMs;
    }

    public int getSlaLookbackMinutes() {
        return slaLookbackMinutes;
    }

    public void setSlaLookbackMinutes(int slaLookbackMinutes) {
        this.slaLookbackMinutes = slaLookbackMinutes;
    }
}
