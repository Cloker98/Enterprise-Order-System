#!/bin/bash

# =============================================================================
# Product Service API Test Suite
# =============================================================================
# 
# This script tests all endpoints of the Product Service with various scenarios:
# - Happy path scenarios
# - Edge cases
# - Error scenarios
# - Validation tests
# - Performance tests
#
# Usage: ./test-product-service.sh [base_url]
# Example: ./test-product-service.sh http://localhost:8081
# =============================================================================

set -e  # Exit on any error

# Configuration
BASE_URL="${1:-http://localhost:8081}"
API_BASE="${BASE_URL}/api/v1/products"
TEMP_DIR="./test-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="${TEMP_DIR}/test_${TIMESTAMP}.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Create temp directory
mkdir -p "$TEMP_DIR"

# =============================================================================
# Helper Functions
# =============================================================================

log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

log_info() {
    log "${BLUE}[INFO]${NC} $1"
}

log_success() {
    log "${GREEN}[PASS]${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    log "${RED}[FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

log_warning() {
    log "${YELLOW}[WARN]${NC} $1"
}

increment_test() {
    ((TOTAL_TESTS++))
}

# Test HTTP response
test_http() {
    local test_name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"
    local description="$6"
    
    increment_test
    
    log_info "Testing: $test_name"
    log_info "Description: $description"
    log_info "Request: $method $url"
    
    if [ -n "$data" ]; then
        log_info "Data: $data"
    fi
    
    # Make request and capture response
    local response_file="${TEMP_DIR}/response_${TOTAL_TESTS}.json"
    local headers_file="${TEMP_DIR}/headers_${TOTAL_TESTS}.txt"
    
    if [ -n "$data" ]; then
        http_status=$(curl -s -w "%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "Accept: application/json" \
            -d "$data" \
            -D "$headers_file" \
            -o "$response_file" \
            "$url")
    else
        http_status=$(curl -s -w "%{http_code}" \
            -X "$method" \
            -H "Accept: application/json" \
            -D "$headers_file" \
            -o "$response_file" \
            "$url")
    fi
    
    # Check status code
    if [ "$http_status" = "$expected_status" ]; then
        log_success "Status: $http_status (Expected: $expected_status)"
        
        # Show response if it's JSON
        if [ -s "$response_file" ]; then
            log_info "Response:"
            cat "$response_file" | jq '.' 2>/dev/null || cat "$response_file"
        fi
        
        # Return response file path for further processing
        echo "$response_file"
    else
        log_error "Status: $http_status (Expected: $expected_status)"
        log_error "Response:"
        cat "$response_file" 2>/dev/null || echo "No response body"
        return 1
    fi
    
    echo ""
}

# Extract field from JSON response
extract_field() {
    local file="$1"
    local field="$2"
    jq -r ".$field" "$file" 2>/dev/null || echo ""
}

# Wait for service to be ready
wait_for_service() {
    log_info "Waiting for Product Service to be ready..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
            log_success "Service is ready!"
            return 0
        fi
        
        log_info "Attempt $attempt/$max_attempts - Service not ready, waiting..."
        sleep 2
        ((attempt++))
    done
    
    log_error "Service failed to start within expected time"
    exit 1
}

# =============================================================================
# Test Data
# =============================================================================

# Valid product data
VALID_PRODUCT_1='{
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone with advanced features",
    "price": 999.99,
    "stockQuantity": 50,
    "sku": "IPHONE-15-PRO",
    "category": "ELECTRONICS"
}'

VALID_PRODUCT_2='{
    "name": "Samsung Galaxy S24",
    "description": "Premium Android smartphone",
    "price": 899.99,
    "stockQuantity": 30,
    "sku": "GALAXY-S24",
    "category": "ELECTRONICS"
}'

VALID_PRODUCT_3='{
    "name": "Nike Air Max",
    "description": "Comfortable running shoes",
    "price": 129.99,
    "stockQuantity": 100,
    "sku": "NIKE-AIR-MAX",
    "category": "CLOTHING"
}'

# Update data
UPDATE_PRODUCT='{
    "name": "iPhone 15 Pro Max",
    "description": "Updated iPhone with larger screen",
    "price": 1099.99,
    "category": "ELECTRONICS"
}'

# Stock operation
STOCK_DECREASE='{
    "quantity": 5
}'

# Invalid data for validation tests
INVALID_PRODUCT_NO_NAME='{
    "description": "Product without name",
    "price": 99.99,
    "stockQuantity": 10,
    "sku": "NO-NAME",
    "category": "ELECTRONICS"
}'

INVALID_PRODUCT_NEGATIVE_PRICE='{
    "name": "Invalid Product",
    "description": "Product with negative price",
    "price": -10.00,
    "stockQuantity": 10,
    "sku": "INVALID-PRICE",
    "category": "ELECTRONICS"
}'

INVALID_PRODUCT_INVALID_SKU='{
    "name": "Invalid SKU Product",
    "description": "Product with invalid SKU",
    "price": 99.99,
    "stockQuantity": 10,
    "sku": "INVALID@SKU!",
    "category": "ELECTRONICS"
}'

INVALID_STOCK_ZERO='{
    "quantity": 0
}'

INVALID_STOCK_NEGATIVE='{
    "quantity": -5
}'

# =============================================================================
# Test Scenarios
# =============================================================================

run_health_check() {
    log_info "=== HEALTH CHECK ==="
    
    test_http \
        "Health Check" \
        "GET" \
        "${BASE_URL}/actuator/health" \
        "" \
        "200" \
        "Verify service is healthy and ready"
}

run_create_product_tests() {
    log_info "=== CREATE PRODUCT TESTS ==="
    
    # Test 1: Create valid product
    response_file=$(test_http \
        "Create Product - Valid Data" \
        "POST" \
        "$API_BASE" \
        "$VALID_PRODUCT_1" \
        "201" \
        "Create a product with all valid fields")
    
    # Extract product ID for later tests
    PRODUCT_ID_1=$(extract_field "$response_file" "id")
    log_info "Created product ID: $PRODUCT_ID_1"
    
    # Test 2: Create another valid product
    response_file=$(test_http \
        "Create Product - Second Valid Product" \
        "POST" \
        "$API_BASE" \
        "$VALID_PRODUCT_2" \
        "201" \
        "Create another product for list testing")
    
    PRODUCT_ID_2=$(extract_field "$response_file" "id")
    log_info "Created product ID: $PRODUCT_ID_2"
    
    # Test 3: Create third product (different category)
    response_file=$(test_http \
        "Create Product - Different Category" \
        "POST" \
        "$API_BASE" \
        "$VALID_PRODUCT_3" \
        "201" \
        "Create product in different category")
    
    PRODUCT_ID_3=$(extract_field "$response_file" "id")
    log_info "Created product ID: $PRODUCT_ID_3"
    
    # Test 4: Try to create duplicate SKU
    test_http \
        "Create Product - Duplicate SKU" \
        "POST" \
        "$API_BASE" \
        "$VALID_PRODUCT_1" \
        "409" \
        "Should fail when trying to create product with existing SKU"
    
    # Test 5: Create product without name
    test_http \
        "Create Product - Missing Name" \
        "POST" \
        "$API_BASE" \
        "$INVALID_PRODUCT_NO_NAME" \
        "400" \
        "Should fail validation when name is missing"
    
    # Test 6: Create product with negative price
    test_http \
        "Create Product - Negative Price" \
        "POST" \
        "$API_BASE" \
        "$INVALID_PRODUCT_NEGATIVE_PRICE" \
        "400" \
        "Should fail validation when price is negative"
    
    # Test 7: Create product with invalid SKU
    test_http \
        "Create Product - Invalid SKU" \
        "POST" \
        "$API_BASE" \
        "$INVALID_PRODUCT_INVALID_SKU" \
        "400" \
        "Should fail validation when SKU contains invalid characters"
    
    # Test 8: Create product with empty body
    test_http \
        "Create Product - Empty Body" \
        "POST" \
        "$API_BASE" \
        "{}" \
        "400" \
        "Should fail validation when request body is empty"
    
    # Test 9: Create product with malformed JSON
    test_http \
        "Create Product - Malformed JSON" \
        "POST" \
        "$API_BASE" \
        '{"name": "Test", "price":}' \
        "400" \
        "Should fail when JSON is malformed"
}

run_get_product_tests() {
    log_info "=== GET PRODUCT TESTS ==="
    
    # Test 1: Get existing product
    test_http \
        "Get Product - Valid ID" \
        "GET" \
        "$API_BASE/$PRODUCT_ID_1" \
        "" \
        "200" \
        "Retrieve product by valid ID"
    
    # Test 2: Get non-existent product
    test_http \
        "Get Product - Non-existent ID" \
        "GET" \
        "$API_BASE/00000000-0000-0000-0000-000000000000" \
        "" \
        "404" \
        "Should return 404 for non-existent product"
    
    # Test 3: Get product with invalid UUID format
    test_http \
        "Get Product - Invalid UUID" \
        "GET" \
        "$API_BASE/invalid-uuid" \
        "" \
        "400" \
        "Should return 400 for invalid UUID format"
}

run_list_products_tests() {
    log_info "=== LIST PRODUCTS TESTS ==="
    
    # Test 1: List all products (default pagination)
    test_http \
        "List Products - Default Pagination" \
        "GET" \
        "$API_BASE" \
        "" \
        "200" \
        "List all products with default pagination"
    
    # Test 2: List products with custom page size
    test_http \
        "List Products - Custom Page Size" \
        "GET" \
        "$API_BASE?size=2" \
        "" \
        "200" \
        "List products with page size of 2"
    
    # Test 3: List products with pagination (page 2)
    test_http \
        "List Products - Second Page" \
        "GET" \
        "$API_BASE?page=1&size=2" \
        "" \
        "200" \
        "Get second page of products"
    
    # Test 4: Filter by category
    test_http \
        "List Products - Filter by Electronics" \
        "GET" \
        "$API_BASE?category=ELECTRONICS" \
        "" \
        "200" \
        "Filter products by ELECTRONICS category"
    
    # Test 5: Filter by category (different category)
    test_http \
        "List Products - Filter by Clothing" \
        "GET" \
        "$API_BASE?category=CLOTHING" \
        "" \
        "200" \
        "Filter products by CLOTHING category"
    
    # Test 6: Filter by name
    test_http \
        "List Products - Filter by Name" \
        "GET" \
        "$API_BASE?name=iPhone" \
        "" \
        "200" \
        "Filter products by name containing 'iPhone'"
    
    # Test 7: Filter by name (case insensitive)
    test_http \
        "List Products - Filter by Name (Case Insensitive)" \
        "GET" \
        "$API_BASE?name=iphone" \
        "" \
        "200" \
        "Filter products by name (case insensitive)"
    
    # Test 8: Combined filters
    test_http \
        "List Products - Combined Filters" \
        "GET" \
        "$API_BASE?category=ELECTRONICS&name=iPhone" \
        "" \
        "200" \
        "Filter by both category and name"
    
    # Test 9: Sort by name
    test_http \
        "List Products - Sort by Name" \
        "GET" \
        "$API_BASE?sort=name,asc" \
        "" \
        "200" \
        "Sort products by name ascending"
    
    # Test 10: Sort by price descending
    test_http \
        "List Products - Sort by Price Desc" \
        "GET" \
        "$API_BASE?sort=price,desc" \
        "" \
        "200" \
        "Sort products by price descending"
    
    # Test 11: Invalid category
    test_http \
        "List Products - Invalid Category" \
        "GET" \
        "$API_BASE?category=INVALID_CATEGORY" \
        "" \
        "400" \
        "Should return 400 for invalid category"
    
    # Test 12: Invalid page number
    test_http \
        "List Products - Invalid Page Number" \
        "GET" \
        "$API_BASE?page=-1" \
        "" \
        "400" \
        "Should return 400 for negative page number"
    
    # Test 13: Invalid page size
    test_http \
        "List Products - Invalid Page Size" \
        "GET" \
        "$API_BASE?size=0" \
        "" \
        "400" \
        "Should return 400 for zero page size"
}

run_update_product_tests() {
    log_info "=== UPDATE PRODUCT TESTS ==="
    
    # Test 1: Update existing product
    test_http \
        "Update Product - Valid Data" \
        "PUT" \
        "$API_BASE/$PRODUCT_ID_1" \
        "$UPDATE_PRODUCT" \
        "200" \
        "Update product with valid data"
    
    # Test 2: Update non-existent product
    test_http \
        "Update Product - Non-existent ID" \
        "PUT" \
        "$API_BASE/00000000-0000-0000-0000-000000000000" \
        "$UPDATE_PRODUCT" \
        "404" \
        "Should return 404 when updating non-existent product"
    
    # Test 3: Update with invalid data (missing name)
    test_http \
        "Update Product - Missing Name" \
        "PUT" \
        "$API_BASE/$PRODUCT_ID_1" \
        '{"description": "Updated", "price": 99.99, "category": "ELECTRONICS"}' \
        "400" \
        "Should fail validation when name is missing"
    
    # Test 4: Update with negative price
    test_http \
        "Update Product - Negative Price" \
        "PUT" \
        "$API_BASE/$PRODUCT_ID_1" \
        '{"name": "Updated", "price": -10.00, "category": "ELECTRONICS"}' \
        "400" \
        "Should fail validation when price is negative"
    
    # Test 5: Update with invalid UUID
    test_http \
        "Update Product - Invalid UUID" \
        "PUT" \
        "$API_BASE/invalid-uuid" \
        "$UPDATE_PRODUCT" \
        "400" \
        "Should return 400 for invalid UUID format"
}

run_stock_operations_tests() {
    log_info "=== STOCK OPERATIONS TESTS ==="
    
    # Test 1: Decrease stock with valid quantity
    test_http \
        "Decrease Stock - Valid Quantity" \
        "POST" \
        "$API_BASE/$PRODUCT_ID_2/decrease-stock" \
        "$STOCK_DECREASE" \
        "200" \
        "Decrease stock by valid quantity"
    
    # Test 2: Decrease stock with zero quantity
    test_http \
        "Decrease Stock - Zero Quantity" \
        "POST" \
        "$API_BASE/$PRODUCT_ID_2/decrease-stock" \
        "$INVALID_STOCK_ZERO" \
        "400" \
        "Should fail validation when quantity is zero"
    
    # Test 3: Decrease stock with negative quantity
    test_http \
        "Decrease Stock - Negative Quantity" \
        "POST" \
        "$API_BASE/$PRODUCT_ID_2/decrease-stock" \
        "$INVALID_STOCK_NEGATIVE" \
        "400" \
        "Should fail validation when quantity is negative"
    
    # Test 4: Decrease stock for non-existent product
    test_http \
        "Decrease Stock - Non-existent Product" \
        "POST" \
        "$API_BASE/00000000-0000-0000-0000-000000000000/decrease-stock" \
        "$STOCK_DECREASE" \
        "404" \
        "Should return 404 for non-existent product"
    
    # Test 5: Try to decrease more stock than available
    large_decrease='{"quantity": 1000}'
    test_http \
        "Decrease Stock - Insufficient Stock" \
        "POST" \
        "$API_BASE/$PRODUCT_ID_2/decrease-stock" \
        "$large_decrease" \
        "409" \
        "Should return 409 when trying to decrease more stock than available"
}

run_delete_product_tests() {
    log_info "=== DELETE PRODUCT TESTS ==="
    
    # Test 1: Delete existing product
    test_http \
        "Delete Product - Valid ID" \
        "DELETE" \
        "$API_BASE/$PRODUCT_ID_3" \
        "" \
        "204" \
        "Delete product (soft delete)"
    
    # Test 2: Try to get deleted product (should still exist but inactive)
    test_http \
        "Get Deleted Product" \
        "GET" \
        "$API_BASE/$PRODUCT_ID_3" \
        "" \
        "200" \
        "Deleted product should still be retrievable but marked as INACTIVE"
    
    # Test 3: Delete non-existent product
    test_http \
        "Delete Product - Non-existent ID" \
        "DELETE" \
        "$API_BASE/00000000-0000-0000-0000-000000000000" \
        "" \
        "404" \
        "Should return 404 when deleting non-existent product"
    
    # Test 4: Delete with invalid UUID
    test_http \
        "Delete Product - Invalid UUID" \
        "DELETE" \
        "$API_BASE/invalid-uuid" \
        "" \
        "400" \
        "Should return 400 for invalid UUID format"
    
    # Test 5: Try to delete already deleted product
    test_http \
        "Delete Product - Already Deleted" \
        "DELETE" \
        "$API_BASE/$PRODUCT_ID_3" \
        "" \
        "204" \
        "Should handle deletion of already deleted product gracefully"
}

run_edge_case_tests() {
    log_info "=== EDGE CASE TESTS ==="
    
    # Test 1: Very long product name (at limit)
    long_name_product='{
        "name": "'"$(printf 'A%.0s' {1..200})"'",
        "description": "Product with maximum length name",
        "price": 99.99,
        "stockQuantity": 10,
        "sku": "LONG-NAME-PRODUCT",
        "category": "OTHER"
    }'
    
    test_http \
        "Create Product - Maximum Name Length" \
        "POST" \
        "$API_BASE" \
        "$long_name_product" \
        "201" \
        "Create product with name at maximum length (200 chars)"
    
    # Test 2: Very long product name (over limit)
    too_long_name_product='{
        "name": "'"$(printf 'A%.0s' {1..201})"'",
        "description": "Product with too long name",
        "price": 99.99,
        "stockQuantity": 10,
        "sku": "TOO-LONG-NAME",
        "category": "OTHER"
    }'
    
    test_http \
        "Create Product - Name Too Long" \
        "POST" \
        "$API_BASE" \
        "$too_long_name_product" \
        "400" \
        "Should fail when name exceeds 200 characters"
    
    # Test 3: Minimum valid price
    min_price_product='{
        "name": "Minimum Price Product",
        "description": "Product with minimum valid price",
        "price": 0.01,
        "stockQuantity": 1,
        "sku": "MIN-PRICE",
        "category": "OTHER"
    }'
    
    test_http \
        "Create Product - Minimum Price" \
        "POST" \
        "$API_BASE" \
        "$min_price_product" \
        "201" \
        "Create product with minimum valid price (0.01)"
    
    # Test 4: Zero stock quantity
    zero_stock_product='{
        "name": "Zero Stock Product",
        "description": "Product with zero initial stock",
        "price": 99.99,
        "stockQuantity": 0,
        "sku": "ZERO-STOCK",
        "category": "OTHER"
    }'
    
    test_http \
        "Create Product - Zero Stock" \
        "POST" \
        "$API_BASE" \
        "$zero_stock_product" \
        "201" \
        "Create product with zero initial stock"
}

run_performance_tests() {
    log_info "=== PERFORMANCE TESTS ==="
    
    # Test 1: Create multiple products quickly
    log_info "Creating 10 products for performance testing..."
    
    for i in {1..10}; do
        perf_product='{
            "name": "Performance Test Product '"$i"'",
            "description": "Product created for performance testing",
            "price": '"$((i * 10))"'.99,
            "stockQuantity": '"$((i * 5))"',
            "sku": "PERF-TEST-'"$i"'",
            "category": "OTHER"
        }'
        
        test_http \
            "Performance Test - Create Product $i" \
            "POST" \
            "$API_BASE" \
            "$perf_product" \
            "201" \
            "Create product $i for performance testing" > /dev/null
    done
    
    # Test 2: List products with large page size
    test_http \
        "Performance Test - Large Page Size" \
        "GET" \
        "$API_BASE?size=100" \
        "" \
        "200" \
        "List products with large page size"
    
    # Test 3: Multiple concurrent requests (simulated)
    log_info "Testing concurrent requests..."
    
    # Create background requests
    for i in {1..5}; do
        curl -s "$API_BASE" > /dev/null &
    done
    
    # Wait for all background jobs to complete
    wait
    
    log_success "Concurrent requests completed"
}

# =============================================================================
# Main Test Execution
# =============================================================================

main() {
    log_info "=== PRODUCT SERVICE API TEST SUITE ==="
    log_info "Base URL: $BASE_URL"
    log_info "API Base: $API_BASE"
    log_info "Log File: $LOG_FILE"
    log_info "Timestamp: $TIMESTAMP"
    echo ""
    
    # Wait for service to be ready
    wait_for_service
    echo ""
    
    # Run all test suites
    run_health_check
    echo ""
    
    run_create_product_tests
    echo ""
    
    run_get_product_tests
    echo ""
    
    run_list_products_tests
    echo ""
    
    run_update_product_tests
    echo ""
    
    run_stock_operations_tests
    echo ""
    
    run_delete_product_tests
    echo ""
    
    run_edge_case_tests
    echo ""
    
    run_performance_tests
    echo ""
    
    # Test Summary
    log_info "=== TEST SUMMARY ==="
    log_info "Total Tests: $TOTAL_TESTS"
    log_success "Passed: $PASSED_TESTS"
    log_error "Failed: $FAILED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        log_success "🎉 ALL TESTS PASSED!"
        exit 0
    else
        log_error "❌ SOME TESTS FAILED!"
        log_info "Check the log file for details: $LOG_FILE"
        exit 1
    fi
}

# Check dependencies
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    log_warning "jq is not installed - JSON responses will not be formatted"
fi

# Run main function
main "$@"