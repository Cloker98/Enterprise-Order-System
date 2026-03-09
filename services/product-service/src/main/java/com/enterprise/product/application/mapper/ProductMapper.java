package com.enterprise.product.application.mapper;

import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Product ↔ DTO conversion.
 *
 * <p>Converts between domain model and API DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

  /**
   * Converts domain Product to ProductResponse DTO.
   *
   * @param product the domain product
   * @return ProductResponse DTO
   */
  @Mapping(target = "id", expression = "java(product.getId().value().toString())")
  @Mapping(target = "price", expression = "java(product.getPrice().amount())")
  ProductResponse toResponse(Product product);
}
