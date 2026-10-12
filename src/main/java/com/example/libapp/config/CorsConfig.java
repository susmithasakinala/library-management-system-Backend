package com.example.libapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Frontend (Netlify) and backend (Render) are on different websites,
// so the browser blocks the calls unless the backend allows that website.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // On Render set CORS_ORIGIN to your Netlify link. If not set, every website is allowed.
    @Value("${CORS_ORIGIN:*}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
