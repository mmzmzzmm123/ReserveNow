package com.room.hd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow all origins
        config.addAllowedOriginPattern("*");
        // Allow credentials
        config.setAllowCredentials(true);
        // Allow all headers
        config.addAllowedHeader("*");
        // Allow all methods
        config.addAllowedMethod("*");
        // Allow all exposed headers
        config.addExposedHeader("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 