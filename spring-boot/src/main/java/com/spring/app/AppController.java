package com.spring.app;

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

    @GetMapping("/hello")
    public String hello() {
        return String.valueOf(param);
    }

}
