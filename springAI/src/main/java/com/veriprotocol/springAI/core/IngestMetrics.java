package com.veriprotocol.springAI.core;


import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class IngestMetrics {

    private final Counter ingestReceived;
    private final Counter ingestSucceeded;
    private final Counter ingestFailed;
    private final Counter leaseSkipped;
    private final Timer ingestLatency;

    public IngestMetrics(MeterRegistry registry) {
        this.ingestReceived = registry.counter("smartsearch.ingest.received.total");
        this.ingestSucceeded = registry.counter("smartsearch.ingest.succeeded.total");
        this.ingestFailed = registry.counter("smartsearch.ingest.failed.total");
        this.leaseSkipped = registry.counter("smartsearch.ingest.lease_skipped.total");

        this.ingestLatency = Timer.builder("smartsearch.ingest.latency")
                .publishPercentiles(0.5, 0.9, 0.99)
                .register(registry);
    }

    public void onReceived() { ingestReceived.increment(); }
    public void onSucceeded() { ingestSucceeded.increment(); }
    public void onFailed() { ingestFailed.increment(); }
    public void onLeaseSkipped() { leaseSkipped.increment(); }

    public void recordLatencyNanos(long nanos) {
        ingestLatency.record(nanos, TimeUnit.NANOSECONDS);
    }
}

