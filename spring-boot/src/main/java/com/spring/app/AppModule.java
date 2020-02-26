package com.spring.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppModule {

    @Bean
    public AppConfig appConfig() {
        return new AppConfig("appName", "appUrl");
    }

    @Bean
    public String parameter() {
        return "this is parameter";
    }

    @Bean(name = "parameter2") Integer parameter2() {
        return 1;
    }

}
