package com.honeytoken.gateway.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Evaluation metrics tracker for the deception-based IDS.
 *
 * Tracks:
 *   - True Positives  (TP): Honeytoken accesses correctly detected
 *   - False Positives (FP): Legitimate requests incorrectly flagged  (should be 0)
 *   - False Negatives (FN): Honeytoken accesses that slipped through  (should be 0)
 *   - Detection Latency: Time from request to alert
 *
 * Provides:
 *   - Detection Rate   = TP / (TP + FN)
 *   - False Positive Rate = FP / (FP + TN)
 *   - Precision        = TP / (TP + FP)
 */
@Slf4j
@Component
public class DetectionMetricsEvaluator {

    private final AtomicLong truePositives  = new AtomicLong(0);
    private final AtomicLong falsePositives = new AtomicLong(0);
    private final AtomicLong falseNegatives = new AtomicLong(0);
    private final AtomicLong totalRequests  = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicLong latencySamples = new AtomicLong(0);

    public void recordTruePositive(long detectionLatencyMs) {
        truePositives.incrementAndGet();
        totalRequests.incrementAndGet();
        totalLatencyMs.addAndGet(detectionLatencyMs);
        latencySamples.incrementAndGet();
    }

    public void recordFalsePositive() {
        falsePositives.incrementAndGet();
        totalRequests.incrementAndGet();
    }

    public void recordFalseNegative() {
        falseNegatives.incrementAndGet();
        totalRequests.incrementAndGet();
    }

    public void recordLegitimateRequest() {
        totalRequests.incrementAndGet();
    }

    public Map<String, Object> getSummary() {
        long tp = truePositives.get();
        long fp = falsePositives.get();
        long fn = falseNegatives.get();
        long tn = totalRequests.get() - tp - fp - fn;
        long samples = latencySamples.get();

        double detectionRate   = (tp + fn) > 0 ? (double) tp / (tp + fn)         : 0.0;
        double fpr             = (fp + tn) > 0 ? (double) fp / (fp + tn)          : 0.0;
        double precision       = (tp + fp) > 0 ? (double) tp / (tp + fp)          : 0.0;
        double avgLatencyMs    = samples  > 0  ? (double) totalLatencyMs.get() / samples : 0.0;

        return Map.of(
                "truePositives",     tp,
                "falsePositives",    fp,
                "falseNegatives",    fn,
                "trueNegatives",     tn,
                "totalRequests",     totalRequests.get(),
                "detectionRate",     String.format("%.4f", detectionRate),
                "falsePositiveRate", String.format("%.4f", fpr),
                "precision",         String.format("%.4f", precision),
                "avgDetectionLatencyMs", String.format("%.2f", avgLatencyMs)
        );
    }

    public void logSummary() {
        log.info("=== Intrusion Detection Evaluation Metrics ===");
        getSummary().forEach((k, v) -> log.info("  {} = {}", k, v));
    }
}

