package com.example.tracesplitting;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fire-and-forget async operation WITHOUT trace splitting.
 */
public class WithoutTraceSplitting {
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("trace-splitting-demo");
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @WithSpan("process-request-without-split")
    public static void processRequest() throws InterruptedException {
        Thread.sleep(100); // Simulate some work
        startAsyncOperation();
    }

    private static void startAsyncOperation() {
        // Submit the async work. The executor instrumentation will propagate the context automatically
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000); // 10-second delay simulating a busy executor

                // Create a child span (parent-child relationship maintained automatically)
                Span asyncSpan = tracer.spanBuilder("async-operation-without-split").startSpan();
                try (Scope scope = asyncSpan.makeCurrent()) {
                    // Do the actual async work
                    Thread.sleep(2000);
                } finally {
                    asyncSpan.end();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
