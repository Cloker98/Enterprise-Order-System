package com.enterprise.order.infrastructure.rest.controller;

import com.enterprise.order.application.dto.request.CancelOrderRequest;
import com.enterprise.order.application.dto.request.CreateOrderRequest;
import com.enterprise.order.application.dto.request.ListOrdersRequest;
import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.dto.response.OrderSummaryResponse;
import com.enterprise.order.application.usecase.CancelOrderUseCase;
import com.enterprise.order.application.usecase.CreateOrderUseCase;
import com.enterprise.order.application.usecase.GetOrderUseCase;
import com.enterprise.order.application.usecase.ListOrdersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Order operations.
 *
 * <p>Provides HTTP endpoints for order management including:
 * - Creating orders
 * - Retrieving orders
 * - Listing orders with pagination and filtering
 * - Cancelling orders
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {

  private static final Logger log = LoggerFactory.getLogger(OrderController.class);

  private final CreateOrderUseCase createOrderUseCase;
  private final GetOrderUseCase getOrderUseCase;
  private final ListOrdersUseCase listOrdersUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;

  /**
   * Constructor for OrderController.
   *
   * @param createOrderUseCase the create order use case
   * @param getOrderUseCase the get order use case
   * @param listOrdersUseCase the list orders use case
   * @param cancelOrderUseCase the cancel order use case
   */
  public OrderController(CreateOrderUseCase createOrderUseCase,
                        GetOrderUseCase getOrderUseCase,
                        ListOrdersUseCase listOrdersUseCase,
                        CancelOrderUseCase cancelOrderUseCase) {
    this.createOrderUseCase = createOrderUseCase;
    this.getOrderUseCase = getOrderUseCase;
    this.listOrdersUseCase = listOrdersUseCase;
    this.cancelOrderUseCase = cancelOrderUseCase;
  }

  /**
   * Creates a new order.
   *
   * @param request the order creation request
   * @return the created order response
   */
  @PostMapping
  @Operation(summary = "Create a new order", 
             description = "Creates a new order with the specified items and customer information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Order created successfully",
                   content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "409", description = "Insufficient stock"),
      @ApiResponse(responseCode = "503", description = "Product service unavailable")
  })
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<OrderResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request) {
    
    log.info("Creating order for customer: {}", request.customerId());
    
    OrderResponse response = createOrderUseCase.execute(request);
    
    log.info("Order created successfully: {}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves an order by ID.
   *
   * @param orderId the order ID
   * @return the order response
   */
  @GetMapping("/{orderId}")
  @Operation(summary = "Get order by ID", 
             description = "Retrieves an order by its unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order found",
                   content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize("hasRole('ADMIN') or "
                + "@orderSecurityService.canAccessOrder(#orderId, authentication.name)")
  public ResponseEntity<OrderResponse> getOrder(
      @Parameter(description = "Order ID", required = true)
      @PathVariable String orderId) {
    
    log.info("Retrieving order: {}", orderId);
    
    OrderResponse response = getOrderUseCase.execute(orderId);
    
    return ResponseEntity.ok(response);
  }

  /**
   * Lists orders with pagination and filtering.
   *
   * @param customerId the customer ID (required for non-admin users)
   * @param status the order status filter (optional)
   * @param startDate the start date filter (optional)
   * @param endDate the end date filter (optional)
   * @param page the page number (0-based)
   * @param size the page size
   * @return paginated list of order summaries
   */
  @GetMapping
  @Operation(summary = "List orders", 
             description = "Lists orders with pagination and optional filtering")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize("hasRole('ADMIN') or "
                + "@orderSecurityService.canAccessCustomerOrders(#customerId, authentication.name)")
  public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
      @Parameter(description = "Customer ID", required = true)
      @RequestParam String customerId,
      
      @Parameter(description = "Order status filter")
      @RequestParam(required = false) String status,
      
      @Parameter(description = "Start date filter (ISO format)")
      @RequestParam(required = false) 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      
      @Parameter(description = "End date filter (ISO format)")
      @RequestParam(required = false) 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      
      @Parameter(description = "Page number (0-based)")
      @RequestParam(defaultValue = "0") @Min(0) int page,
      
      @Parameter(description = "Page size (1-100)")
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    
    log.info("Listing orders for customer: {} with filters - status: {}, dates: {} to {}", 
             customerId, status, startDate, endDate);
    
    ListOrdersRequest request = new ListOrdersRequest(
        customerId, status, startDate, endDate, page, size);
    
    Page<OrderSummaryResponse> response = listOrdersUseCase.execute(request);
    
    log.debug("Found {} orders for customer: {}", response.getTotalElements(), customerId);
    return ResponseEntity.ok(response);
  }

  /**
   * Cancels an order.
   *
   * @param orderId the order ID
   * @param request the cancellation request
   * @return no content response
   */
  @DeleteMapping("/{orderId}")
  @Operation(summary = "Cancel order", 
             description = "Cancels an order and compensates stock")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
      @ApiResponse(responseCode = "400", 
                   description = "Invalid request or order cannot be cancelled"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize("hasRole('ADMIN') or "
                + "@orderSecurityService.canCancelOrder(#orderId, authentication.name)")
  public ResponseEntity<Void> cancelOrder(
      @Parameter(description = "Order ID", required = true)
      @PathVariable String orderId,
      
      @Valid @RequestBody CancelOrderRequest request) {
    
    log.info("Cancelling order: {} with reason: {}", orderId, request.reason());
    
    // Ensure the path variable matches the request body
    CancelOrderRequest validatedRequest = CancelOrderRequest.of(orderId, request.reason());
    
    cancelOrderUseCase.execute(validatedRequest);
    
    log.info("Order cancelled successfully: {}", orderId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Health check endpoint for the order controller.
   *
   * @return health status
   */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Checks if the order service is healthy")
  @ApiResponse(responseCode = "200", description = "Service is healthy")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Order service is healthy");
  }
}