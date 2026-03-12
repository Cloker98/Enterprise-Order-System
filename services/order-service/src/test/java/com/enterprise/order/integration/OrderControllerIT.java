package com.enterprise.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.enterprise.order.application.dto.request.CreateOrderRequest;
import com.enterprise.order.application.dto.request.OrderItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for OrderController.
 *
 * <p>Tests the complete flow from HTTP request to database persistence
 * using real infrastructure components via Testcontainers.
 */
@AutoConfigureWebMvc
@Transactional
@DisplayName("OrderController Integration Tests")
class OrderControllerIT extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("Should create order successfully with valid request")
  void createOrder_WhenValidRequest_ShouldReturn201() throws Exception {
    // Given
    CreateOrderRequest request = new CreateOrderRequest(
        "customer-123",
        List.of(
            new OrderItemRequest("product-1", 2),
            new OrderItemRequest("product-2", 1)
        )
    );

    // When & Then
    mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").value("customer-123"))
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("Should return 400 when request is invalid")
  void createOrder_WhenInvalidRequest_ShouldReturn400() throws Exception {
    // Given - request with null customer ID
    CreateOrderRequest request = new CreateOrderRequest(
        null,
        List.of(new OrderItemRequest("product-1", 2))
    );

    // When & Then
    mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when items list is empty")
  void createOrder_WhenEmptyItems_ShouldReturn400() throws Exception {
    // Given
    CreateOrderRequest request = new CreateOrderRequest("customer-123", List.of());

    // When & Then
    mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should get order by ID successfully")
  void getOrder_WhenOrderExists_ShouldReturn200() throws Exception {
    // Given - Create an order first
    CreateOrderRequest createRequest = new CreateOrderRequest(
        "customer-123",
        List.of(new OrderItemRequest("product-1", 1))
    );

    String createResponse = mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Extract order ID from response
    String orderId = objectMapper.readTree(createResponse).get("id").asText();

    // When & Then
    mockMvc.perform(get("/orders/{orderId}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId))
        .andExpect(jsonPath("$.customerId").value("customer-123"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("Should return 404 when order does not exist")
  void getOrder_WhenOrderNotFound_ShouldReturn404() throws Exception {
    // Given
    String nonExistentOrderId = "550e8400-e29b-41d4-a716-446655440000";

    // When & Then
    mockMvc.perform(get("/orders/{orderId}", nonExistentOrderId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should list orders with pagination")
  void listOrders_WhenValidRequest_ShouldReturnPaginatedResult() throws Exception {
    // Given - Create some orders first
    String customerId = "customer-456";
    
    for (int i = 0; i < 3; i++) {
      CreateOrderRequest request = new CreateOrderRequest(
          customerId,
          List.of(new OrderItemRequest("product-" + i, 1))
      );
      
      mockMvc.perform(post("/orders")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }

    // When & Then
    mockMvc.perform(get("/orders")
            .param("customerId", customerId)
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("Should return health check status")
  void health_ShouldReturn200() throws Exception {
    // When & Then
    mockMvc.perform(get("/orders/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Order service is healthy"));
  }
}