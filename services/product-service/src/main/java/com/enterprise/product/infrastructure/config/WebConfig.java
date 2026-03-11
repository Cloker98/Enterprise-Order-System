package com.enterprise.product.infrastructure.config;

import com.enterprise.product.domain.model.ProductCategory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for custom converters and formatters.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToProductCategoryConverter());
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