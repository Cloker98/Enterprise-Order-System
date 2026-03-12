package com.enterprise.order.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity for Order persistence.
 *
 * <p>This is the infrastructure layer representation of the Order domain model.
 * It handles the mapping between the domain model and the database schema.
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

  @Id
  @Column(name = "id", nullable = false, length = 36)
  private String id;

  @Column(name = "customer_id", nullable = false, length = 100)
  private String customerId;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, 
             fetch = FetchType.LAZY, orphanRemoval = true)
  private List<OrderItemJpaEntity> items = new ArrayList<>();

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OrderStatusJpa status;

  @Column(name = "cancellation_reason", length = 500)
  private String cancellationReason;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version
  @Column(name = "version")
  private Long version;

  /**
   * Default constructor for JPA.
   */
  protected OrderJpaEntity() {
    // Required by JPA
  }

  /**
   * Constructor for creating new OrderJpaEntity.
   *
   * @param id the order ID
   * @param customerId the customer ID
   * @param totalAmount the total amount
   * @param status the order status
   * @param cancellationReason the cancellation reason
   * @param createdAt the creation timestamp
   * @param updatedAt the update timestamp
   */
  public OrderJpaEntity(String id, String customerId, BigDecimal totalAmount,
                       OrderStatusJpa status, String cancellationReason,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.customerId = customerId;
    this.totalAmount = totalAmount;
    this.status = status;
    this.cancellationReason = cancellationReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Adds an order item to this order.
   *
   * @param item the order item to add
   */
  public void addItem(OrderItemJpaEntity item) {
    items.add(item);
    item.setOrder(this);
  }

  /**
   * Removes an order item from this order.
   *
   * @param item the order item to remove
   */
  public void removeItem(OrderItemJpaEntity item) {
    items.remove(item);
    item.setOrder(null);
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public List<OrderItemJpaEntity> getItems() {
    return items;
  }

  public void setItems(List<OrderItemJpaEntity> items) {
    this.items = items;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public OrderStatusJpa getStatus() {
    return status;
  }

  public void setStatus(OrderStatusJpa status) {
    this.status = status;
  }

  public String getCancellationReason() {
    return cancellationReason;
  }

  public void setCancellationReason(String cancellationReason) {
    this.cancellationReason = cancellationReason;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    OrderJpaEntity that = (OrderJpaEntity) obj;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "OrderJpaEntity{"
        + "id='" + id + '\''
        + ", customerId='" + customerId + '\''
        + ", itemCount=" + items.size()
        + ", totalAmount=" + totalAmount
        + ", status=" + status
        + ", createdAt=" + createdAt
        + '}';
  }

  /**
   * JPA enum for Order Status.
   */
  public enum OrderStatusJpa {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
  }
}