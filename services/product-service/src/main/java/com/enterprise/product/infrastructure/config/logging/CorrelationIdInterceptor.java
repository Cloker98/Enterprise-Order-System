package com.enterprise.product.infrastructure.config.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to add correlation ID to all HTTP requests.
 *
 * <p>This interceptor ensures that every request has a unique correlation ID
 * that can be used to trace the request through logs and across services.
 *
 * <p>The correlation ID is:
 * - Extracted from X-Correlation-ID header if present
 * - Generated as UUID if not present
 * - Added to MDC for logging
 * - Added to response headers for client tracking
 */
@Component
@Slf4j
public class CorrelationIdInterceptor implements HandlerInterceptor {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_MDC_KEY = "correlationId";
  private static final String USER_ID_HEADER = "X-User-ID";
  private static final String USER_ID_MDC_KEY = "userId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
      Object handler) {
    // Extract or generate correlation ID
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.trim().isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    // Extract user ID if present
    String userId = request.getHeader(USER_ID_HEADER);

    // Add to MDC for logging
    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    if (userId != null && !userId.trim().isEmpty()) {
      MDC.put(USER_ID_MDC_KEY, userId);
    }

    // Add to response headers
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    // Log request start
    log.info("Request started: {} {} - Correlation ID: {}", 
        request.getMethod(), request.getRequestURI(), correlationId);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
      Object handler, Exception ex) {
    
    String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
    
    // Log request completion
    if (ex != null) {
      log.error("Request failed: {} {} - Status: {} - Correlation ID: {} - Error: {}", 
          request.getMethod(), request.getRequestURI(), response.getStatus(), 
          correlationId, ex.getMessage());
    } else {
      log.info("Request completed: {} {} - Status: {} - Correlation ID: {}", 
          request.getMethod(), request.getRequestURI(), response.getStatus(), correlationId);
    }

    // Clean up MDC
    MDC.clear();
  }
}