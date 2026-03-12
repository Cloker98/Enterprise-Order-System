package com.enterprise.order.infrastructure.persistence.repository;

import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderStatus;
import com.enterprise.order.domain.repository.OrderRepository;
import com.enterprise.order.infrastructure.persistence.entity.OrderJpaEntity.OrderStatusJpa;
import com.enterprise.order.infrastructure.persistence.mapper.OrderJpaMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Implementation of OrderRepository using JPA.
 *
 * <p>This adapter implements the domain repository interface using
 * JPA entities and Spring Data repositories. It handles the mapping
 * between domain objects and JPA entities, and provides caching
 * for improved performance.
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

  private static final Logger log = LoggerFactory.getLogger(OrderRepositoryImpl.class);

  private final OrderJpaRepository orderJpaRepository;
  private final OrderJpaMapper orderJpaMapper;

  /**
   * Constructor for OrderRepositoryImpl.
   *
   * @param orderJpaRepository the JPA repository
   * @param orderJpaMapper the JPA mapper
   */
  public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository,
                            OrderJpaMapper orderJpaMapper) {
    this.orderJpaRepository = orderJpaRepository;
    this.orderJpaMapper = orderJpaMapper;
  }

  @Override
  @CacheEvict(value = "orders", key = "#order.id.value")
  public Order save(Order order) {
    log.debug("Saving order: {}", order.getId());
    
    var jpaEntity = orderJpaMapper.toJpaEntity(order);
    var savedEntity = orderJpaRepository.save(jpaEntity);
    var savedOrder = orderJpaMapper.toDomainObject(savedEntity);
    
    log.debug("Order saved successfully: {}", savedOrder.getId());
    return savedOrder;
  }

  @Override
  @Cacheable(value = "orders", key = "#orderId.value")
  public Optional<Order> findById(OrderId orderId) {
    log.debug("Finding order by ID: {}", orderId);
    
    return orderJpaRepository.findById(orderId.value())
        .map(orderJpaMapper::toDomainObject);
  }

  @Override
  public List<Order> findByCustomerId(CustomerId customerId) {
    log.debug("Finding orders by customer ID: {}", customerId);
    
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(0, 100, sort); // Default limit
    
    return orderJpaRepository.findByCustomerId(customerId.value(), pageable)
        .getContent()
        .stream()
        .map(orderJpaMapper::toDomainObject)
        .toList();
  }

  @Override
  public List<Order> findByCustomerId(CustomerId customerId, int page, int size) {
    log.debug("Finding orders by customer ID with pagination: {}, page: {}, size: {}", 
              customerId, page, size);
    
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    
    return orderJpaRepository.findByCustomerId(customerId.value(), pageable)
        .getContent()
        .stream()
        .map(orderJpaMapper::toDomainObject)
        .toList();
  }

  @Override
  public Page<Order> findByCustomerId(CustomerId customerId, Pageable pageable) {
    log.debug("Finding orders by customer ID with pagination: {}, page: {}", 
              customerId, pageable.getPageNumber());
    
    return orderJpaRepository.findByCustomerId(customerId.value(), pageable)
        .map(orderJpaMapper::toDomainObject);
  }

  @Override
  public Page<Order> findByCustomerIdWithFilters(CustomerId customerId, OrderStatus status,
                                                 LocalDateTime startDate, LocalDateTime endDate,
                                                 Pageable pageable) {
    log.debug("Finding orders by customer ID with filters: {}, status: {}, dates: {} to {}", 
              customerId, status, startDate, endDate);
    
    OrderStatusJpa jpaStatus = status != null ? mapToJpaStatus(status) : null;
    
    return orderJpaRepository.findByCustomerIdWithFilters(
        customerId.value(), jpaStatus, startDate, endDate, pageable)
        .map(orderJpaMapper::toDomainObject);
  }

  @Override
  public List<Order> findByCustomerIdAndStatus(CustomerId customerId, OrderStatus status, 
                                              int page, int size) {
    log.debug("Finding orders by customer ID and status: {}, status: {}, page: {}, size: {}", 
              customerId, status, page, size);
    
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    OrderStatusJpa jpaStatus = mapToJpaStatus(status);
    
    return orderJpaRepository.findByCustomerIdAndStatus(customerId.value(), jpaStatus, pageable)
        .getContent()
        .stream()
        .map(orderJpaMapper::toDomainObject)
        .toList();
  }

  @Override
  public List<Order> findByCustomerIdAndDateRange(CustomerId customerId, 
                                                 LocalDateTime startDate, 
                                                 LocalDateTime endDate, int page, int size) {
    log.debug("Finding orders by customer ID and date range: {}, dates: {} to {}, "
              + "page: {}, size: {}", customerId, startDate, endDate, page, size);
    
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page, size, sort);
    
    return orderJpaRepository.findByCustomerIdAndCreatedAtBetween(
        customerId.value(), startDate, endDate, pageable)
        .getContent()
        .stream()
        .map(orderJpaMapper::toDomainObject)
        .toList();
  }

  @Override
  public long countByCustomerId(CustomerId customerId) {
    log.debug("Counting orders by customer ID: {}", customerId);
    
    return orderJpaRepository.countByCustomerId(customerId.value());
  }

  @Override
  @CacheEvict(value = "orders", key = "#orderId.value")
  public void deleteById(OrderId orderId) {
    log.debug("Deleting order by ID: {}", orderId);
    
    orderJpaRepository.deleteById(orderId.value());
    
    log.debug("Order deleted successfully: {}", orderId);
  }

  @Override
  public boolean existsById(OrderId orderId) {
    log.debug("Checking if order exists by ID: {}", orderId);
    
    return orderJpaRepository.existsById(orderId.value());
  }

  /**
   * Maps domain OrderStatus to JPA OrderStatusJpa.
   *
   * @param status the domain order status
   * @return the JPA order status
   */
  private OrderStatusJpa mapToJpaStatus(OrderStatus status) {
    return switch (status) {
      case PENDING -> OrderStatusJpa.PENDING;
      case CONFIRMED -> OrderStatusJpa.CONFIRMED;
      case DELIVERED -> OrderStatusJpa.DELIVERED;
      case CANCELLED -> OrderStatusJpa.CANCELLED;
    };
  }
}