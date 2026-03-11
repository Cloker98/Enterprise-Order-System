package com.enterprise.product.infrastructure.persistence.mapper;

import com.enterprise.product.domain.model.Money;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.model.ProductSnapshot;
import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Product ↔ ProductJpaEntity conversion.
 *
 * <p>Converts between domain model and JPA entity.
 * Generated implementation is ProductJpaMapperImpl.java
 */
@Mapper(componentModel = "spring")
public interface ProductJpaMapper {

  /**
   * Converts domain Product to JPA entity.
   *
   * @param product the domain product
   * @return JPA entity
   */
  @Mapping(target = "id", expression = "java(product.getId().value())")
  @Mapping(target = "price", expression = "java(product.getPrice().amount())")
  @Mapping(target = "category", source = "category")
  @Mapping(target = "status", source = "status")
  ProductJpaEntity toEntity(Product product);

  /**
   * Converts JPA entity to domain Product.
   *
   * @param entity the JPA entity
   * @return domain Product
   */
  default Product toDomain(ProductJpaEntity entity) {
    ProductSnapshot snapshot = new ProductSnapshot(
        new ProductId(entity.getId()),
        entity.getName(),
        entity.getDescription(),
        Money.brl(entity.getPrice()), // Convert BigDecimal to Money
        entity.getStockQuantity(),
        entity.getSku(),
        entity.getCategory(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
    return Product.reconstitute(snapshot);
  }
}