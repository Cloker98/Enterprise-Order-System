package com.enterprise.product.infrastructure.config;

import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.infrastructure.config.logging.CorrelationIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for custom converters, formatters, and interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final CorrelationIdInterceptor correlationIdInterceptor;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToProductCategoryConverter());
  }

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

  /**
   * Converter for String to ProductCategory enum.
   * Handles case-insensitive conversion and provides better error messages.
   */
  public static class StringToProductCategoryConverter 
      implements Converter<String, ProductCategory> {

    @Override
    public ProductCategory convert(String source) {
      if (source == null || source.trim().isEmpty()) {
        return null;
      }

      try {
        return ProductCategory.valueOf(source.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid product category: '%s'. Valid values are: %s",
                source, java.util.Arrays.toString(ProductCategory.values())));
      }
    }
  }
}