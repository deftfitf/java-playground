package sample;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class Main {
    public static void main(String[] args) {
        final var registry = new SimpleMeterRegistry();
        final var compositeRegistry = new CompositeMeterRegistry();
        compositeRegistry.add(registry);
        compositeRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public Id map(Id id) {
                return id.withName("namespace." + id.getName());
            }
        });

        final var counterRef1 = Counter.builder("counter").register(compositeRegistry);
        final var counterRef2 = Counter.builder("counter").register(compositeRegistry);
        final var counterRef3 = registry.counter("namespace.counter");

        counterRef1.increment();
        counterRef2.increment();
        counterRef3.increment();

        assert counterRef1.count() == 3;
        assert counterRef2.count() == 3;
        assert counterRef3.count() == 3;
    }
}
