package com.enterprise.order.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order Aggregate Root.
 *
 * <p>Represents a customer order with business logic for order management,
 * status transitions, and item management. Follows DDD principles with
 * rich domain model and encapsulated business rules.
 */
public class Order {

  private final OrderId id;
  private final CustomerId customerId;
  private final List<OrderItem> items;
  private Money totalAmount;
  private OrderStatus status;
  private String cancellationReason;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Private constructor for creating new Order instances.
   */
  private Order(CustomerId customerId, List<OrderItem> items) {
    this.id = OrderId.generate();
    this.customerId = customerId;
    this.items = new ArrayList<>(items);
    this.status = OrderStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.totalAmount = calculateTotal();
  }

  /**
   * Private constructor for reconstituting Order from snapshot.
   */
  private Order(OrderSnapshot snapshot) {
    this.id = snapshot.id();
    this.customerId = snapshot.customerId();
    this.items = new ArrayList<>(snapshot.items());
    this.totalAmount = snapshot.totalAmount();
    this.status = snapshot.status();
    this.cancellationReason = snapshot.cancellationReason();
    this.createdAt = snapshot.createdAt();
    this.updatedAt = snapshot.updatedAt();
  }

  /**
   * Factory method to create a new Order.
   *
   * @param customerId the customer identifier
   * @param items the list of order items
   * @return new Order instance
   * @throws IllegalArgumentException if parameters are invalid
   */
  public static Order create(CustomerId customerId, List<OrderItem> items) {
    validateCreate(customerId, items);
    return new Order(customerId, items);
  }

  /**
   * Factory method to reconstitute Order from snapshot.
   *
   * @param snapshot the order snapshot
   * @return reconstituted Order instance
   */
  public static Order reconstitute(OrderSnapshot snapshot) {
    return new Order(snapshot);
  }

  /**
   * Validates parameters for Order creation.
   */
  private static void validateCreate(CustomerId customerId, List<OrderItem> items) {
    if (customerId == null) {
      throw new IllegalArgumentException("CustomerId cannot be null");
    }
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Items cannot be null or empty");
    }
  }

  /**
   * Confirms the order (transitions from PENDING to CONFIRMED).
   *
   * @throws IllegalStateException if transition is not allowed
   */
  public void confirm() {
    if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
      throw new IllegalStateException(
          "Cannot transition from " + status + " to " + OrderStatus.CONFIRMED
      );
    }
    this.status = OrderStatus.CONFIRMED;
    updateTimestamp();
  }

  /**
   * Cancels the order with a reason.
   *
   * @param reason the cancellation reason
   * @throws IllegalArgumentException if reason is invalid
   * @throws IllegalStateException if transition is not allowed
   */
  public void cancel(String reason) {
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("Cancellation reason cannot be null or empty");
    }
    if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
      throw new IllegalStateException(
          "Cannot transition from " + status + " to " + OrderStatus.CANCELLED
      );
    }
    this.status = OrderStatus.CANCELLED;
    this.cancellationReason = reason;
    updateTimestamp();
  }

  /**
   * Updates the order status (for external status changes like delivery).
   *
   * @param newStatus the new status
   * @throws IllegalStateException if transition is not allowed
   */
  public void updateStatus(OrderStatus newStatus) {
    if (!status.canTransitionTo(newStatus)) {
      throw new IllegalStateException(
          "Cannot transition from " + status + " to " + newStatus
      );
    }
    this.status = newStatus;
    updateTimestamp();
  }

  /**
   * Updates the timestamp with a small delay to ensure different timestamps.
   */
  private void updateTimestamp() {
    try {
      Thread.sleep(1); // Ensure different timestamp
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Calculates the total amount of the order.
   *
   * @return total amount
   */
  public Money calculateTotal() {
    if (items.isEmpty()) {
      return Money.brl(0.01); // Minimum money value to avoid zero
    }
    
    Money total = items.get(0).getTotalPrice();
    for (int i = 1; i < items.size(); i++) {
      total = total.add(items.get(i).getTotalPrice());
    }
    return total;
  }

  /**
   * Checks if the order can be cancelled.
   *
   * @return true if order can be cancelled
   */
  public boolean canBeCancelled() {
    return status.canTransitionTo(OrderStatus.CANCELLED);
  }

  /**
   * Adds an item to the order.
   *
   * @param item the order item to add
   * @throws IllegalArgumentException if item is invalid or duplicate
   */
  public void addItem(OrderItem item) {
    if (item == null) {
      throw new IllegalArgumentException("OrderItem cannot be null");
    }
    
    // Check for duplicate product
    boolean productExists = items.stream()
        .anyMatch(existingItem -> existingItem.getProductId().equals(item.getProductId()));
    
    if (productExists) {
      throw new IllegalArgumentException(
          "Product " + item.getProductId() + " already exists in this order"
      );
    }
    
    items.add(item);
    this.totalAmount = calculateTotal();
    updateTimestamp();
  }

  /**
   * Removes an item from the order.
   *
   * @param productId the product ID to remove
   * @throws IllegalArgumentException if product not found or last item
   */
  public void removeItem(ProductId productId) {
    // First check if product exists
    boolean productExists = items.stream()
        .anyMatch(item -> item.getProductId().equals(productId));
    
    if (!productExists) {
      throw new IllegalArgumentException(
          "Product " + productId + " not found in this order"
      );
    }
    
    // Then check if it's the last item
    if (items.size() == 1) {
      throw new IllegalArgumentException("Cannot remove last item from order");
    }
    
    items.removeIf(item -> item.getProductId().equals(productId));
    this.totalAmount = calculateTotal();
    updateTimestamp();
  }

  /**
   * Creates a snapshot of the current order state.
   *
   * @return order snapshot
   */
  public OrderSnapshot toSnapshot() {
    return new OrderSnapshot(
        id, customerId, List.copyOf(items), totalAmount, status,
        cancellationReason, createdAt, updatedAt
    );
  }

  // Getters
  public OrderId getId() {
    return id;
  }

  public CustomerId getCustomerId() {
    return customerId;
  }

  public List<OrderItem> getItems() {
    return List.copyOf(items); // Return immutable copy
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public String getCancellationReason() {
    return cancellationReason;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Order order = (Order) obj;
    return Objects.equals(id, order.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Order{"
        + "id=" + id
        + ", customerId=" + customerId
        + ", itemCount=" + items.size()
        + ", totalAmount=" + totalAmount
        + ", status=" + status
        + ", createdAt=" + createdAt
        + '}';
  }
}