package com.enterprise.product.domain.model;

import com.enterprise.product.domain.exception.InsufficientStockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Aggregate Root.
 *
 * <p>Represents a product in the catalog with business logic for stock management.
 *
 * <p>This is a PURE domain class - NO framework annotations (@Entity, @Table,
 * etc).
 * JPA mapping is done separately in infrastructure layer.
 *
 * <p>Immutability: ID, SKU, and createdAt are immutable after creation.
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
public class Product {

  private final ProductId id;
  private String name;
  private String description;
  private Money price;
  private int stockQuantity;
  private final String sku;
  private ProductCategory category;
  private ProductStatus status;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Private constructor for creating new products.
   *
   * @param name          product name
   * @param description   product description
   * @param price         product price
   * @param stockQuantity initial stock quantity
   * @param sku           stock keeping unit (unique identifier)
   * @param category      product category
   */
  private Product(
      String name,
      String description,
      Money price,
      int stockQuantity,
      String sku,
      ProductCategory category) {

    validateCreate(name, price, stockQuantity, sku);

    this.id = ProductId.generate();
    this.name = name;
    this.description = description;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.sku = sku;
    this.category = category;
    this.status = ProductStatus.ACTIVE;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Private constructor for reconstituting from database using ProductSnapshot.
   * This avoids S107 code smell (too many parameters).
   *
   * @param snapshot snapshot containing all product data
   */
  private Product(ProductSnapshot snapshot) {
    this.id = snapshot.id();
    this.name = snapshot.name();
    this.description = snapshot.description();
    this.price = snapshot.price();
    this.stockQuantity = snapshot.stockQuantity();
    this.sku = snapshot.sku();
    this.category = snapshot.category();
    this.status = snapshot.status();
    this.createdAt = snapshot.createdAt();
    this.updatedAt = snapshot.updatedAt();
  }

  /**
   * Factory method to create a new product.
   *
   * @param name          product name (required, max 200 chars)
   * @param description   product description (max 1000 chars)
   * @param price         product price (must be > 0)
   * @param stockQuantity initial stock (must be >= 0)
   * @param sku           stock keeping unit (required, unique, alphanumeric)
   * @param category      product category
   * @return new Product instance with ACTIVE status
   * @throws IllegalArgumentException if validation fails
   */
  public static Product create(
      String name,
      String description,
      BigDecimal price,
      int stockQuantity,
      String sku,
      ProductCategory category) {

    Money money = Money.brl(price);
    return new Product(name, description, money, stockQuantity, sku, category);
  }

  /**
   * Factory method to reconstitute product from persistence.
   *
   * <p>Used by infrastructure layer to rebuild domain objects from database.
   * Uses ProductSnapshot to avoid S107 code smell (too many parameters).
   *
   * @param snapshot snapshot containing all product data
   * @return reconstituted Product instance
   */
  public static Product reconstitute(ProductSnapshot snapshot) {
    return new Product(snapshot);
  }

  /**
   * Creates a snapshot of the current product state.
   *
   * <p>Used by infrastructure layer to persist domain objects.
   *
   * @return ProductSnapshot containing current state
   */
  public ProductSnapshot toSnapshot() {
    return new ProductSnapshot(
        this.id,
        this.name,
        this.description,
        this.price,
        this.stockQuantity,
        this.sku,
        this.category,
        this.status,
        this.createdAt,
        this.updatedAt);
  }

  /**
   * Decreases stock quantity (e.g., when order is placed).
   *
   * <p>Domain invariant: stock cannot become negative.
   *
   * @param quantity amount to decrease (must be > 0)
   * @throws IllegalArgumentException   if quantity <= 0
   * @throws InsufficientStockException if insufficient stock available
   */
  public void decreaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException(
          "Quantity must be greater than 0, but was: " + quantity);
    }

    if (quantity > this.stockQuantity) {
      throw new InsufficientStockException(
          String.format(
              "Insufficient stock. Available: %d, Requested: %d",
              this.stockQuantity,
              quantity));
    }

    this.stockQuantity -= quantity;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Increases stock quantity (e.g., replenishment).
   *
   * @param quantity amount to increase (must be > 0)
   * @throws IllegalArgumentException if quantity <= 0
   */
  public void increaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException(
          "Quantity must be greater than 0, but was: " + quantity);
    }

    this.stockQuantity += quantity;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Updates product information.
   *
   * <p>Note: Cannot update id, sku, or createdAt (immutable).
   *
   * @param name        new product name
   * @param description new description
   * @param price       new price
   * @param category    new category
   * @throws IllegalArgumentException if validation fails
   */
  public void update(String name, String description, Money price, ProductCategory category) {
    validateUpdate(name, price);

    this.name = name;
    this.description = description;
    this.price = price;
    this.category = category;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Deactivates the product (soft delete).
   *
   * <p>Sets status to INACTIVE but preserves the record.
   */
  public void deactivate() {
    this.status = ProductStatus.INACTIVE;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Checks if product is active.
   *
   * @return true if status is ACTIVE
   */
  public boolean isActive() {
    return this.status == ProductStatus.ACTIVE;
  }

  /**
   * Checks if product has sufficient stock.
   *
   * @param quantity quantity to check
   * @return true if stock >= quantity
   */
  public boolean hasStock(int quantity) {
    return this.stockQuantity >= quantity;
  }

  // Validation methods

  private void validateCreate(String name, Money price, int stockQuantity, String sku) {
    validateName(name);
    validatePrice(price);
    validateStockQuantity(stockQuantity);
    validateSku(sku);
  }

  private void validateUpdate(String name, Money price) {
    validateName(name);
    validatePrice(price);
  }

  private void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (name.length() > 200) {
      throw new IllegalArgumentException(
          "Name cannot exceed 200 characters, but was: " + name.length());
    }
  }

  private void validatePrice(Money price) {
    if (price == null) {
      throw new IllegalArgumentException("Price cannot be null");
    }
    // Money value object already validates that amount > 0
  }

  private void validateStockQuantity(int stockQuantity) {
    if (stockQuantity < 0) {
      throw new IllegalArgumentException(
          "Stock quantity cannot be negative, but was: " + stockQuantity);
    }
  }

  private void validateSku(String sku) {
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU cannot be null or blank");
    }
    if (sku.length() > 50) {
      throw new IllegalArgumentException(
          "SKU cannot exceed 50 characters, but was: " + sku.length());
    }
    if (!sku.matches("^[a-zA-Z0-9-]+$")) {
      throw new IllegalArgumentException(
          "SKU must be alphanumeric with optional hyphens, but was: " + sku);
    }
  }

  // Getters (no setters - controlled mutability via domain methods)

  public ProductId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Money getPrice() {
    return price;
  }

  public int getStockQuantity() {
    return stockQuantity;
  }

  public String getSku() {
    return sku;
  }

  public ProductCategory getCategory() {
    return category;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}