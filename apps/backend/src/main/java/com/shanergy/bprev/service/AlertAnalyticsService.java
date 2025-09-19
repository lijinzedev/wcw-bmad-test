package com.shanergy.bprev.service;

import com.shanergy.bprev.dto.AnalyticsDtos;
import com.shanergy.bprev.model.MonitoringAlert;
import com.shanergy.bprev.model.User;
import com.shanergy.bprev.repository.MonitoringAlertRepository;
import com.shanergy.bprev.util.CsvExporter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AlertAnalyticsService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private final MonitoringAlertRepository alertRepository;
    private final AuditService auditService;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public AlertAnalyticsService(MonitoringAlertRepository alertRepository, AuditService auditService) {
        this.alertRepository = alertRepository;
        this.auditService = auditService;
    }

    public List<AnalyticsDtos.BucketCount> trend(OffsetDateTime from,
                                                 OffsetDateTime to,
                                                 String bucket,
                                                 String metricCode,
                                                 String severity,
                                                 User actor) {
        AnalyticsQuery query = normalize(from, to, AnalyticsDtos.Bucket.from(bucket), metricCode, severity, null);
        List<AnalyticsDtos.BucketCount> result = cached("trend", query, () -> buildBuckets(query));
        auditService.audit("analysis.alerts.trend", actor, query.auditDetails());
        return copyBuckets(result);
    }

    public List<AnalyticsDtos.BucketCount> distribution(OffsetDateTime from,
                                                        OffsetDateTime to,
                                                        String bucket,
                                                        String metricCode,
                                                        String severity,
                                                        User actor) {
        AnalyticsQuery query = normalize(from, to, AnalyticsDtos.Bucket.from(bucket), metricCode, severity, null);
        List<AnalyticsDtos.BucketCount> result = cached("distribution", query, () -> buildBuckets(query));
        auditService.audit("analysis.alerts.distribution", actor, query.auditDetails());
        return copyBuckets(result);
    }

    public List<AnalyticsDtos.TopMetric> topMetrics(OffsetDateTime from,
                                                    OffsetDateTime to,
                                                    String bucket,
                                                    String metricCode,
                                                    String severity,
                                                    Integer limit,
                                                    User actor) {
        AnalyticsQuery query = normalize(from, to, AnalyticsDtos.Bucket.from(bucket), metricCode, severity, limit);
        List<AnalyticsDtos.TopMetric> result = cached("top-metrics", query, () -> buildTopMetrics(query));
        auditService.audit("analysis.alerts.top-metrics", actor, query.auditDetails());
        return copyMetrics(result, query.limit());
    }

    public byte[] exportCsv(OffsetDateTime from,
                            OffsetDateTime to,
                            String bucket,
                            String metricCode,
                            String severity,
                            User actor) {
        AnalyticsQuery query = normalize(from, to, AnalyticsDtos.Bucket.from(bucket), metricCode, severity, null);
        List<AnalyticsDtos.BucketCount> buckets = cached("trend", query, () -> buildBuckets(query));
        List<MonitoringAlert> alerts = loadAlerts(query);
        auditService.audit("analysis.alerts.export", actor, query.auditDetails());
        return CsvExporter.toCsv(buildCsvRows(query, buckets, alerts));
    }

    public void evictCache() {
        cache.clear();
    }

    private List<AnalyticsDtos.BucketCount> buildBuckets(AnalyticsQuery query) {
        Map<OffsetDateTime, AnalyticsDtos.BucketCount> map = new LinkedHashMap<>();
        for (MonitoringAlert alert : loadAlerts(query)) {
            OffsetDateTime occurredAt = alert.getOccurredAt();
            if (occurredAt == null) {
                continue;
            }
            OffsetDateTime bucketStart = bucketStart(occurredAt, query.bucket());
            AnalyticsDtos.BucketCount bucket = map.computeIfAbsent(bucketStart, key -> {
                AnalyticsDtos.BucketCount bc = new AnalyticsDtos.BucketCount();
                bc.setBucketStart(key);
                bc.setBucketEnd(bucketEnd(key, query.bucket()));
                return bc;
            });
            bucket.increment(alert.getSeverity());
        }
        return new ArrayList<>(map.values());
    }

    private List<AnalyticsDtos.TopMetric> buildTopMetrics(AnalyticsQuery query) {
        Map<String, AnalyticsDtos.TopMetric> map = new LinkedHashMap<>();
        for (MonitoringAlert alert : loadAlerts(query)) {
            if (!StringUtils.hasText(alert.getMetricCode())) {
                continue;
            }
            String code = alert.getMetricCode();
            AnalyticsDtos.TopMetric metric = map.computeIfAbsent(code, key -> {
                AnalyticsDtos.TopMetric tm = new AnalyticsDtos.TopMetric();
                tm.setMetricCode(key);
                tm.setMetricName(alert.getMetricName());
                return tm;
            });
            if (!StringUtils.hasText(metric.getMetricName()) && StringUtils.hasText(alert.getMetricName())) {
                metric.setMetricName(alert.getMetricName());
            }
            metric.increment(alert.getSeverity());
        }
        List<AnalyticsDtos.TopMetric> metrics = new ArrayList<>(map.values());
        metrics.sort(Comparator.comparingLong(AnalyticsDtos.TopMetric::getTotal).reversed()
                .thenComparing(tm -> tm.getMetricCode() == null ? "" : tm.getMetricCode()));
        if (metrics.size() > query.limit()) {
            return new ArrayList<>(metrics.subList(0, query.limit()));
        }
        return metrics;
    }

    private List<MonitoringAlert> loadAlerts(AnalyticsQuery query) {
        Specification<MonitoringAlert> spec = Specification.where(null);
        if (StringUtils.hasText(query.metricCode())) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("metricCode"), query.metricCode()));
        }
        if (StringUtils.hasText(query.severity())) {
            String severityUpper = query.severity().toUpperCase(Locale.ROOT);
            spec = spec.and((root, cq, cb) -> cb.equal(cb.upper(root.get("severity")), severityUpper));
        }
        spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
        spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), query.to()));
        return alertRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "occurredAt"));
    }

    private OffsetDateTime bucketStart(OffsetDateTime time, AnalyticsDtos.Bucket bucket) {
        OffsetDateTime truncated = time.truncatedTo(ChronoUnit.DAYS);
        return switch (bucket) {
            case WEEK -> truncated.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> truncated.with(TemporalAdjusters.firstDayOfMonth());
            case DAY -> truncated;
        };
    }

    private OffsetDateTime bucketEnd(OffsetDateTime bucketStart, AnalyticsDtos.Bucket bucket) {
        return switch (bucket) {
            case WEEK -> bucketStart.plusWeeks(1);
            case MONTH -> bucketStart.plusMonths(1);
            case DAY -> bucketStart.plusDays(1);
        };
    }

    private AnalyticsQuery normalize(OffsetDateTime from,
                                     OffsetDateTime to,
                                     AnalyticsDtos.Bucket bucket,
                                     String metricCode,
                                     String severity,
                                     Integer limit) {
        OffsetDateTime upper = to != null ? to : OffsetDateTime.now();
        OffsetDateTime lower;
        if (from != null) {
            lower = from;
        } else {
            lower = switch (bucket) {
                case MONTH -> upper.minusMonths(3);
                case WEEK -> upper.minusWeeks(6);
                case DAY -> upper.minusDays(30);
            };
        }
        if (lower.isAfter(upper)) {
            OffsetDateTime swap = lower;
            lower = upper;
            upper = swap;
        }
        // normalise to inclusive upper bound by aligning to bucket end
        lower = lower.truncatedTo(ChronoUnit.MINUTES);
        upper = upper.truncatedTo(ChronoUnit.MINUTES);
        String normalizedMetric = StringUtils.hasText(metricCode) ? metricCode.trim() : null;
        String normalizedSeverity = StringUtils.hasText(severity) ? severity.trim() : null;
        int normalizedLimit = limit == null ? 5 : Math.max(1, Math.min(limit, 20));
        return new AnalyticsQuery(lower, upper, bucket, normalizedMetric, normalizedSeverity, normalizedLimit, datasetFingerprint());
    }

    private String datasetFingerprint() {
        long count = alertRepository.count();
        Optional<MonitoringAlert> latest = alertRepository.findTopByOrderByOccurredAtDesc();
        String latestValue = latest.map(alert -> {
            OffsetDateTime occurred = alert.getOccurredAt();
            return occurred == null ? "none" : occurred.toString();
        }).orElse("none");
        return count + "@" + latestValue;
    }

    private List<List<String>> buildCsvRows(AnalyticsQuery query,
                                            List<AnalyticsDtos.BucketCount> buckets,
                                            List<MonitoringAlert> alerts) {
        List<List<String>> rows = new LinkedList<>();
        rows.add(List.of("Summary"));
        rows.add(List.of("Bucket Start", "Bucket End", "Total", "Severity Breakdown"));
        for (AnalyticsDtos.BucketCount bucket : buckets) {
            String breakdown = bucket.getSeverityCounts().entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(" | "));
            rows.add(List.of(
                    stringify(bucket.getBucketStart()),
                    stringify(bucket.getBucketEnd()),
                    String.valueOf(bucket.getTotal()),
                    breakdown
            ));
        }
        rows.add(List.of(""));
        rows.add(List.of("Alert ID", "Occurred At", "Metric Code", "Metric Name", "Severity", "Value", "Unit", "Location"));
        for (MonitoringAlert alert : alerts) {
            rows.add(List.of(
                    stringify(alert.getAlertId()),
                    stringify(alert.getOccurredAt()),
                    nullSafe(alert.getMetricCode()),
                    nullSafe(alert.getMetricName()),
                    nullSafe(alert.getSeverity()),
                    alert.getMeasuredValue() == null ? "" : String.valueOf(alert.getMeasuredValue()),
                    nullSafe(alert.getUnit()),
                    nullSafe(alert.getLocation())
            ));
        }
        rows.add(List.of(""));
        rows.add(List.of("Filters"));
        rows.add(List.of("From", stringify(query.from())));
        rows.add(List.of("To", stringify(query.to())));
        rows.add(List.of("Bucket", query.bucket().name()));
        if (StringUtils.hasText(query.metricCode())) {
            rows.add(List.of("Metric", query.metricCode()));
        }
        if (StringUtils.hasText(query.severity())) {
            rows.add(List.of("Severity", query.severity()));
        }
        return rows;
    }

    private static String stringify(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private List<AnalyticsDtos.BucketCount> copyBuckets(List<AnalyticsDtos.BucketCount> source) {
        return source.stream().map(b -> {
            AnalyticsDtos.BucketCount copy = new AnalyticsDtos.BucketCount();
            copy.setBucketStart(b.getBucketStart());
            copy.setBucketEnd(b.getBucketEnd());
            copy.setTotal(b.getTotal());
            copy.setSeverityCounts(b.getSeverityCounts());
            return copy;
        }).collect(Collectors.toList());
    }

    private List<AnalyticsDtos.TopMetric> copyMetrics(List<AnalyticsDtos.TopMetric> source, int limit) {
        return source.stream().limit(limit).map(m -> {
            AnalyticsDtos.TopMetric copy = new AnalyticsDtos.TopMetric();
            copy.setMetricCode(m.getMetricCode());
            copy.setMetricName(m.getMetricName());
            copy.setTotal(m.getTotal());
            copy.setSeverityCounts(m.getSeverityCounts());
            return copy;
        }).collect(Collectors.toList());
    }

    private <T> T cached(String prefix, AnalyticsQuery query, Supplier<T> loader) {
        long now = System.currentTimeMillis();
        String key = prefix + "|" + query.cacheKey();
        CacheEntry entry = cache.get(key);
        if (entry != null && entry.expiresAt > now) {
            @SuppressWarnings("unchecked")
            T value = (T) entry.value;
            return value;
        }
        T value = loader.get();
        cache.put(key, new CacheEntry(value, now + CACHE_TTL.toMillis()));
        return value;
    }

    private record CacheEntry(Object value, long expiresAt) {
    }

    private record AnalyticsQuery(OffsetDateTime from,
                                  OffsetDateTime to,
                                  AnalyticsDtos.Bucket bucket,
                                  String metricCode,
                                  String severity,
                                  int limit,
                                  String fingerprint) {
        String cacheKey() {
            return fingerprint + "|" + from + "|" + to + "|" + bucket + "|" +
                    Objects.toString(metricCode, "-") + "|" + Objects.toString(severity, "-") + "|" + limit;
        }

        Map<String, Object> auditDetails() {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("from", from);
            details.put("to", to);
            details.put("bucket", bucket.name());
            if (StringUtils.hasText(metricCode)) {
                details.put("metricCode", metricCode);
            }
            if (StringUtils.hasText(severity)) {
                details.put("severity", severity);
            }
            details.put("limit", limit);
            return details;
        }
    }
}
