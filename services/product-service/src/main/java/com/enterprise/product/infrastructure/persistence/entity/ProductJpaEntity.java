package com.enterprise.product.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for Product persistence.
 *
 * <p>This is ONLY for database mapping - NOT used in domain or API layers.
 * Domain layer uses Product.java, API layer uses DTOs.
 *
 * <p>Mapping between ProductJpaEntity and Product is done by ProductJpaMapper.
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_sku", columnList = "sku", unique = true),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJpaEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @Column(nullable = false, unique = true, length = 50)
  private String sku;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * JPA lifecycle callback - sets timestamps before persist.
   */
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (updatedAt == null) {
      updatedAt = LocalDateTime.now();
    }
  }

  /**
   * JPA lifecycle callback - updates timestamp before update.
   */
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
