package com.bms.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Removed path prefix configuration as controllers already include full paths
    // configurer.addPathPrefix("/api/v1", c -> c.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class));
}