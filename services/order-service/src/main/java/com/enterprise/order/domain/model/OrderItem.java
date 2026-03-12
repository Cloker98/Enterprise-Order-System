package com.enterprise.order.domain.model;

import java.util.Objects;

/**
 * Entity representing an item within an order.
 *
 * <p>Contains product information, quantity, and pricing details.
 * Business logic includes total calculation and quantity updates.
 */
public class OrderItem {

  private static final int MAX_PRODUCT_NAME_LENGTH = 255;

  private final ProductId productId;
  private final String productName;
  private int quantity;
  private final Money unitPrice;
  private Money totalPrice;

  /**
   * Private constructor for creating OrderItem instances.
   *
   * @param productId the product identifier
   * @param productName the product name
   * @param quantity the quantity ordered
   * @param unitPrice the unit price
   */
  private OrderItem(ProductId productId, String productName, int quantity, Money unitPrice) {
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.totalPrice = calculateTotal();
  }

  /**
   * Factory method to create OrderItem with validation.
   *
   * @param productId the product identifier
   * @param productName the product name
   * @param quantity the quantity ordered
   * @param unitPrice the unit price
   * @return new OrderItem instance
   * @throws IllegalArgumentException if any parameter is invalid
   */
  public static OrderItem create(ProductId productId, String productName, 
                                int quantity, Money unitPrice) {
    validateCreate(productId, productName, quantity, unitPrice);
    return new OrderItem(productId, productName, quantity, unitPrice);
  }

  /**
   * Validates parameters for OrderItem creation.
   */
  private static void validateCreate(ProductId productId, String productName, 
                                   int quantity, Money unitPrice) {
    if (productId == null) {
      throw new IllegalArgumentException("ProductId cannot be null");
    }
    if (productName == null || productName.trim().isEmpty()) {
      throw new IllegalArgumentException("Product name cannot be null or empty");
    }
    if (productName.length() > MAX_PRODUCT_NAME_LENGTH) {
      throw new IllegalArgumentException("Product name cannot exceed 255 characters");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    if (unitPrice == null) {
      throw new IllegalArgumentException("Unit price cannot be null");
    }
  }

  /**
   * Calculates the total price (quantity × unit price).
   *
   * @return total price
   */
  public Money calculateTotal() {
    return unitPrice.multiply(quantity);
  }

  /**
   * Updates the quantity and recalculates total price.
   *
   * @param newQuantity the new quantity
   * @throws IllegalArgumentException if quantity is invalid
   */
  public void updateQuantity(int newQuantity) {
    if (newQuantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    this.quantity = newQuantity;
    this.totalPrice = calculateTotal();
  }

  // Getters
  public ProductId getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public int getQuantity() {
    return quantity;
  }

  public Money getUnitPrice() {
    return unitPrice;
  }

  public Money getTotalPrice() {
    return totalPrice;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    OrderItem orderItem = (OrderItem) obj;
    return Objects.equals(productId, orderItem.productId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId);
  }

  @Override
  public String toString() {
    return "OrderItem{"
        + "productId=" + productId
        + ", productName='" + productName + '\''
        + ", quantity=" + quantity
        + ", unitPrice=" + unitPrice
        + ", totalPrice=" + totalPrice
        + '}';
  }
}