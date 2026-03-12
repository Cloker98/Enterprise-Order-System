package com.enterprise.order.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * JPA Entity for OrderItem persistence.
 *
 * <p>This is the infrastructure layer representation of the OrderItem domain model.
 * It handles the mapping between the domain model and the database schema.
 */
@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderJpaEntity order;

  @Column(name = "product_id", nullable = false, length = 100)
  private String productId;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice;

  /**
   * Default constructor for JPA.
   */
  protected OrderItemJpaEntity() {
    // Required by JPA
  }

  /**
   * Constructor for creating new OrderItemJpaEntity.
   *
   * @param productId the product ID
   * @param productName the product name
   * @param quantity the quantity
   * @param unitPrice the unit price
   * @param totalPrice the total price
   */
  public OrderItemJpaEntity(String productId, String productName, Integer quantity,
                           BigDecimal unitPrice, BigDecimal totalPrice) {
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.totalPrice = totalPrice;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OrderJpaEntity getOrder() {
    return order;
  }

  public void setOrder(OrderJpaEntity order) {
    this.order = order;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    OrderItemJpaEntity that = (OrderItemJpaEntity) obj;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "OrderItemJpaEntity{"
        + "id=" + id
        + ", productId='" + productId + '\''
        + ", productName='" + productName + '\''
        + ", quantity=" + quantity
        + ", unitPrice=" + unitPrice
        + ", totalPrice=" + totalPrice
        + '}';
  }
}