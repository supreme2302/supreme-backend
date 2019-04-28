package com.supreme.spa.backend.vue.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
