package com.example.tracesplitting;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WithTraceSplitting {
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("trace-splitting-demo");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @WithSpan("process-request-with-split")
    public static void processRequest() throws InterruptedException {
        Thread.sleep(100); // Simulate some work
        startAsyncOperation();
    }

    private static void startAsyncOperation() {
        // Schedule task to run after 10 seconds
        scheduler.schedule(() -> {
            // Create a NEW root span (not a child) with a link to the parent span
            Span asyncSpan = tracer.spanBuilder("async-operation-with-split")
                    .addLink(Span.current().getSpanContext()) // Link to the original parent context
                    .setNoParent() // Explicitly make this a root span
                    .startSpan();

            try (Scope scope = asyncSpan.makeCurrent()) {
                Thread.sleep(2000); // Simulate some work
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                asyncSpan.end();
            }
        }, 10, TimeUnit.SECONDS);
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}
