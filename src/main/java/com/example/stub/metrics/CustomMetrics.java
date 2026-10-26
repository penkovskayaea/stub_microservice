package com.example.stub.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    private Counter totalRequests;
    private Counter failedRequests;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        totalRequests = Counter.builder("stub.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);

        failedRequests = Counter.builder("stub.requests.failed")
                .description("Number of failed requests (status >= 400)")
                .register(meterRegistry);
    }

    public void incrementTotal() {
        totalRequests.increment();
    }

    public void incrementFailed() {
        failedRequests.increment();
    }
}