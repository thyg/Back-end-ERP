package com.rtcomops.treasury.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        corsConfig.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",                // Pour tes tests locaux
            "http://127.0.0.1:3000",                // Pour tes tests locaux
            "https://banking-frontend1.vercel.app",  // 👈 AJOUT IMPORTANT : Ton site Vercel
            "https://banking-frontend1-kwjxlpibu-yimbouthedom-gmailcoms-projects.vercel.app"
        ));
        
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        corsConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        
        corsConfig.setExposedHeaders(List.of(
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}