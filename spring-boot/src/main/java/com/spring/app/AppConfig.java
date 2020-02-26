package com.spring.app;

import lombok.NonNull;
import lombok.Value;

@Value
public class AppConfig {
    @NonNull
    private String appName;
    @NonNull
    private String appUrl;
}
