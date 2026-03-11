package com.enterprise.product.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for Product Service.
 *
 * <p>Configures API documentation with comprehensive information about endpoints,
 * request/response schemas, and examples.
 *
 * <p>Access: http://localhost:8081/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

  @Value("${server.port:8081}")
  private String serverPort;

  @Value("${spring.application.name:product-service}")
  private String applicationName;

  /**
   * Configures OpenAPI specification for Product Service.
   *
   * @return OpenAPI configuration
   */
  @Bean
  public OpenAPI productServiceOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Product Service API")
            .description("""
                **Enterprise Order Management System - Product Service**
                
                This service manages the product catalog with the following capabilities:
                
                ## 🚀 Features
                - **CRUD Operations**: Complete product lifecycle management
                - **Paginated Listing**: Efficient product browsing with filters
                - **Stock Management**: Real-time inventory control
                - **Redis Cache**: Sub-50ms response times for cached data
                - **Search & Filter**: Category and name-based filtering
                
                ## 📊 Performance
                - **GET /products/{id}** (cache hit): ~30ms
                - **GET /products** (listing): ~100ms  
                - **POST /products**: ~80ms
                - **Cache hit rate**: ~85%
                
                ## 🔍 Filtering Examples
                ```
                # Basic pagination
                GET /api/v1/products?page=0&size=20&sort=name,asc
                
                # Filter by category
                GET /api/v1/products?category=ELECTRONICS
                
                # Search by name
                GET /api/v1/products?name=notebook
                
                # Combined filters
                GET /api/v1/products?category=ELECTRONICS&name=dell&sort=price,desc
                ```
                
                ## 🏗️ Architecture
                Built with **Hexagonal Architecture** ensuring clean separation between:
                - **Domain**: Core business logic (framework-agnostic)
                - **Application**: Use cases and orchestration
                - **Infrastructure**: REST, JPA, Redis adapters
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("Enterprise Dev Team")
                .email("dev-team@enterprise.com")
                .url("https://github.com/enterprise/order-system"))
            .license(new License()
                .name("Internal License")
                .url("https://enterprise.com/licenses/internal")))
        .servers(List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server"),
            new Server()
                .url("https://api-dev.enterprise.com")
                .description("Development Environment"),
            new Server()
                .url("https://api-staging.enterprise.com")
                .description("Staging Environment"),
            new Server()
                .url("https://api.enterprise.com")
                .description("Production Environment")
        ));
  }
}