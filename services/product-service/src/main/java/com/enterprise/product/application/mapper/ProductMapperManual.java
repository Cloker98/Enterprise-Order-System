package com.enterprise.product.application.mapper;

import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.domain.model.Product;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Manual implementation of ProductMapper to avoid MapStruct issues.
 * This is a temporary solution while we debug the MapStruct ClassNotFoundException.
 */
@Component
@Primary
public class ProductMapperManual implements ProductMapper {

  @Override
  public ProductResponse toResponse(Product product) {
    if (product == null) {
      return null;
    }

    return new ProductResponse(
        product.getId().value().toString(),
        product.getName(),
        product.getDescription(),
        product.getPrice().amount(),
        product.getStockQuantity(),
        product.getSku(),
        product.getCategory(),
        product.getStatus(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}