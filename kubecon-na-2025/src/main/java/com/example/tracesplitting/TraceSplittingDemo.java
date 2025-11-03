package com.example.tracesplitting;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trace Splitting Demo for KubeCon NA 2025
 *
 * Demonstrates the difference between keeping async work as a child span (problematic)
 * vs splitting into separate traces with span links (recommended).
 *
 * See README.md for full documentation.
 */
public class TraceSplittingDemo {
    private static final Logger logger = LoggerFactory.getLogger(TraceSplittingDemo.class);
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("trace-splitting-demo");

    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("Trace Splitting Demo - KubeCon NA 2025");
        logger.info("=".repeat(80));
        logger.info("");

        try {
            // Run Example 1: Without trace splitting (shows the problem)
            logger.info("-".repeat(80));
            logger.info("EXAMPLE 1: Fire-and-forget WITHOUT trace splitting (THE PROBLEM)");
            logger.info("-".repeat(80));
            logger.info("Watch how the child span starts 10 seconds after the parent span ends.");
            logger.info("This creates a confusing trace with a large time gap.");
            logger.info("");

            simulateHttpRequest(WithoutTraceSplitting::processRequest);

            // Give it a moment
            Thread.sleep(2000);

            // Run Example 2: With trace splitting (shows the solution)
            logger.info("");
            logger.info("-".repeat(80));
            logger.info("EXAMPLE 2: Fire-and-forget WITH trace splitting (THE SOLUTION)");
            logger.info("-".repeat(80));
            logger.info("Watch how we create two separate traces linked together.");
            logger.info("The producer trace ends immediately, and the consumer trace starts later.");
            logger.info("This clearly shows the causal relationship without confusion.");
            logger.info("");

            simulateHttpRequest(WithTraceSplitting::processRequest);

            // Wait for all async operations to complete
            logger.info("");
            logger.info("=".repeat(80));
            logger.info("Waiting for async operations to complete (~15 seconds)...");
            logger.info("=".repeat(80));
            Thread.sleep(15000);

            logger.info("");
            logger.info("=".repeat(80));
            logger.info("Demo completed!");
            logger.info("=".repeat(80));
            logger.info("");
            logger.info("Now check New Relic UI to compare the traces:");
            logger.info("1. Find the trace for 'process-request-without-split' (Example 1)");
            logger.info("   - Notice the weird 10-second gap between parent and child spans");
            logger.info("");
            logger.info("2. Find the trace for 'process-request-with-split' (Example 2)");
            logger.info("   - Notice it's a separate, clean trace for the producer");
            logger.info("   - Look for the linked 'async-operation-with-split' trace");
            logger.info("   - The span link shows the relationship between them");
            logger.info("");
            logger.info("Key takeaway: For fire-and-forget async operations that may be delayed,");
            logger.info("use trace splitting with span links to maintain clear, logical traces.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Demo interrupted", e);
        } finally {
            // Shutdown executors
            WithoutTraceSplitting.shutdown();
            WithTraceSplitting.shutdown();

            // Give time for spans to flush
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Simulates an HTTP server request by creating an HTTP server span.
     * This helps visualize the trace as if it came from a real HTTP endpoint.
     */
    private static void simulateHttpRequest(RequestHandler handler) throws InterruptedException {
        Span httpSpan = tracer.spanBuilder("GET /api/trace-split")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("http.method", "GET")
                .setAttribute("http.route", "/api/trace-split")
                .setAttribute("http.target", "/api/trace-split")
                .setAttribute("http.scheme", "http")
                .startSpan();

        try (Scope scope = httpSpan.makeCurrent()) {
            handler.handle();
        } finally {
            httpSpan.end();
        }
    }

    @FunctionalInterface
    private interface RequestHandler {
        void handle() throws InterruptedException;
    }
}
