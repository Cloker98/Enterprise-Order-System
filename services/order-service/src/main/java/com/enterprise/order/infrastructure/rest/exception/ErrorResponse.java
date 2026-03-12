package com.enterprise.order.infrastructure.rest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response for REST API.
 *
 * <p>Provides consistent error information across all endpoints
 * including timestamp, status, error details, and request path.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    List<String> details,
    String path
) {

  /**
   * Creates a builder for ErrorResponse.
   *
   * @return a new ErrorResponse builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for ErrorResponse.
   */
  public static class Builder {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> details;
    private String path;

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp
     * @return this builder
     */
    public Builder timestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param status the HTTP status code
     * @return this builder
     */
    public Builder status(int status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the error type.
     *
     * @param error the error type
     * @return this builder
     */
    public Builder error(String error) {
      this.error = error;
      return this;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     * @return this builder
     */
    public Builder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets the error details.
     *
     * @param details the error details
     * @return this builder
     */
    public Builder details(List<String> details) {
      this.details = details;
      return this;
    }

    /**
     * Sets the request path.
     *
     * @param path the request path
     * @return this builder
     */
    public Builder path(String path) {
      this.path = path;
      return this;
    }

    /**
     * Builds the ErrorResponse.
     *
     * @return the built ErrorResponse
     */
    public ErrorResponse build() {
      return new ErrorResponse(timestamp, status, error, message, details, path);
    }
  }
}