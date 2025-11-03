import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.w3c.dom.Attr;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class KcdEdinburgh {
  public static void main(String[] args) {
    Tracer tracer = GlobalOpenTelemetry.getTracer("kcd.tracer");
    Meter meter = GlobalOpenTelemetry.getMeter("kcd.meter");
    Logger logger = GlobalOpenTelemetry.get().getLogsBridge().get("kcd.logger");

    Attributes attrs = Attributes.of(AttributeKey.stringKey("kcd.key"), "kcd.value");

    LongCounter counter = meter.counterBuilder("kcd.counter")
            .setDescription("A simple counter")
            .setUnit("1")
            .build();

    Span span = tracer.spanBuilder("kcd.span")
            .setAllAttributes(attrs)
            .startSpan();
    try (Scope ignored = span.makeCurrent()) {
      counter.add(3, attrs);

      logger.logRecordBuilder()
          .setBody("my-log-message")
          .setSeverity(Severity.DEBUG)
          .setAllAttributes(attrs)
          .emit();
    } finally {
      span.end();
    }
  }
}

