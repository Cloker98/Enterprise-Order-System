package com.enterprise.product.infrastructure.config;

import com.enterprise.product.infrastructure.config.logging.CorrelationIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final CorrelationIdInterceptor correlationIdInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(correlationIdInterceptor)
        .addPathPatterns("/api/**")  // Apply to all API endpoints
        .excludePathPatterns(
            "/actuator/**",          // Exclude health checks
            "/swagger-ui/**",        // Exclude Swagger UI
            "/v3/api-docs/**"        // Exclude OpenAPI docs
        );
  }
}