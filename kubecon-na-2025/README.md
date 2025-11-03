# Trace Splitting Demo - KubeCon NA 2025

This demo shows the importance of splitting traces for fire-and-forget async operations.

## Scenario

You have an API that triggers async background work but doesn't wait for it. The async work may take a while to even START due to executor queue backlog (10 seconds in this demo).

## The Problem (Example 1: WITHOUT Trace Splitting)

If you keep the async work as a child span of the request span:

- **Misleading duration**: The parent span completes immediately (after ~100ms). A parent span's duration should represent the time it took to perform its own work. The parent's work is finished when it successfully enqueues the async operation. If you were to keep it "open" and waiting for the child to start and finish, the parent's duration would be artificially inflated by the "wait time," which is misleading.

- **Difficult visualization**: Traces with extremely long spans (or huge gaps) are difficult to read and analyze in visualization tools. It can look like a single operation took seconds or minutes when it was really two separate operations with a long delay in between.

- **Wrong relationship model**: A span link correctly models the relationship as "causally related" rather than "temporally part of." The parent span caused the child span to exist, but the child span is not a direct, synchronous part of the parent's execution.

- **Sampling problems**: Tail samplers struggle to work with this type of traces, because if they consider the total trace duration they will sample traces that were okay from the perspective of the original client, but they may have a big wait in the middle, or they may result in broken traces if the "wait time" for the sampler is shorter than the gap between spans.

## The Solution (Example 2: WITH Trace Splitting)

Split the trace into two separate traces with a span link:
- **First trace**: The API request that triggers the work (completes immediately)
- **Second trace**: The async work that runs later (starts when work begins)
- **Span link**: Connects them, showing the causal relationship
- This is similar to messaging patterns (e.g., Kafka, SQS) where separate traces are linked together

## Code Examples

### `WithoutTraceSplitting.java`
Demonstrates the problem - child span starts 10 seconds after parent span ends, creating a confusing trace with a large time gap.

### `WithTraceSplitting.java`
Demonstrates the solution - creates two separate traces linked together. The first trace ends immediately, and the second trace starts later when work begins.

## How to Run

### Prerequisites
- Java 17 or later
- New Relic account with a license key (or any OTLP-compatible backend)

### Steps

1. Set your New Relic license key:
```bash
export NEW_RELIC_LICENSE_KEY=your_license_key_here
```

2. Run the demo:
```bash
./gradlew run
```

3. The demo will:
   - Run both examples
   - Wait for async operations to complete (~25 seconds total)
   - Send traces to New Relic OTLP endpoint

4. Check your observability backend UI to see the difference in trace visualization

## What to Look For

### Example 1 (Without Trace Splitting):
- Find the trace for `process-request-without-split`
- Notice the weird 10-second gap between parent and child spans
- The child span appears disconnected from its parent
- The trace looks broken or confusing

### Example 2 (With Trace Splitting):
- Find the trace for `process-request-with-split`
- Notice it's a clean, separate trace for the request
- Look for the linked `async-operation-with-split` trace (separate trace)
- The span link shows the causal relationship between the two traces

## Key Takeaway

For fire-and-forget async operations that may be delayed, use trace splitting with span links to maintain clear, logical traces. This approach is similar to how messaging systems (Kafka, SQS, etc.) handle tracing.

## Project Structure

```
kubecon-na-2025/
├── build.gradle                          # Gradle build with OpenTelemetry
├── src/main/java/com/example/tracesplitting/
│   ├── TraceSplittingDemo.java          # Main application
│   ├── WithoutTraceSplitting.java       # Shows the problem
│   └── WithTraceSplitting.java          # Shows the solution
└── README.md                             # This file
```

## Configuration

The OpenTelemetry Java agent is automatically downloaded by Gradle and configured to:
- Send traces to New Relic's OTLP endpoint (`https://otlp.nr-data.net:4317`)
- Use the `NEW_RELIC_LICENSE_KEY` environment variable for authentication
- Set the service name to `trace-splitting-demo`
- Disable metrics and logs (traces only)
