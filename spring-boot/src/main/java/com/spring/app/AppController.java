package com.spring.app;

import java.util.Random;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppController {

    @NonNull
    private final AppConfig appConfig;

    @Qualifier("parameter2")
    @NonNull
    private final Integer param;

    @NonNull
    private final MeterRegistry registry;

    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        final var rnd = new Random();
        final var summary = DistributionSummary.builder("tasks.scheduling.delay")
                                             .description("The time a task waiting for scheduled time")
                                             .tags("endpoint", "/hello")
                                             .distributionStatisticBufferLength(100)
                                             .register(registry);

        summary.record(rnd.nextInt(10000 - 100) + 100);
        return String.valueOf(param);
    }

}
