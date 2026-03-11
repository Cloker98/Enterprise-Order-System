# =============================================================================
# Product Service API Test Suite (PowerShell)
# =============================================================================
# 
# This script tests all endpoints of the Product Service with various scenarios:
# - Happy path scenarios
# - Edge cases
# - Error scenarios
# - Validation tests
# - Performance tests
#
# Usage: .\test-product-service.ps1 [-BaseUrl "http://localhost:8081"]
# Example: .\test-product-service.ps1 -BaseUrl "http://localhost:8081"
# =============================================================================

param(
    [string]$BaseUrl = "http://localhost:8081"
)

# Configuration
$ApiBase = "$BaseUrl/api/v1/products"
$TempDir = "./test-results"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = "$TempDir/test_$Timestamp.log"

# Test counters
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

# Global variables for created product IDs
$script:ProductId1 = ""
$script:ProductId2 = ""
$script:ProductId3 = ""

# Create temp directory
if (!(Test-Path $TempDir)) {
    New-Item -ItemType Directory -Path $TempDir -Force | Out-Null
}

# =============================================================================
# Helper Functions
# =============================================================================

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $Message"
    
    Write-Host $Message -ForegroundColor $Color
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Info {
    param([string]$Message)
    Write-Log "[INFO] $Message" "Cyan"
}

function Write-Success {
    param([string]$Message)
    Write-Log "[PASS] $Message" "Green"
    $script:PassedTests++
}

function Write-Error {
    param([string]$Message)
    Write-Log "[FAIL] $Message" "Red"
    $script:FailedTests++
}

function Write-Warning {
    param([string]$Message)
    Write-Log "[WARN] $Message" "Yellow"
}

function Increment-Test {
    $script:TotalTests++
}

# Test HTTP response - Fixed version compatible with PowerShell 5.1+
function Test-Http {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Url,
        [string]$Data = "",
        [string]$ExpectedStatus,
        [string]$Description
    )
    
    Increment-Test
    
    Write-Info "Testing: $TestName"
    Write-Info "Description: $Description"
    Write-Info "Request: $Method $Url"
    
    if ($Data) {
        Write-Info "Data: $Data"
    }
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        $response = $null
        $statusCode = $null
        
        # Use different approach for PowerShell 5.1 compatibility
        if ($PSVersionTable.PSVersion.Major -ge 7) {
            # PowerShell 7+ with StatusCodeVariable
            if ($Data) {
                $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Data -StatusCodeVariable statusCode
            } else {
                $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -StatusCodeVariable statusCode
            }
        } else {
            # PowerShell 5.1 compatible approach using Invoke-WebRequest
            $webResponse = $null
            if ($Data) {
                $webResponse = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -Body $Data -UseBasicParsing
            } else {
                $webResponse = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -UseBasicParsing
            }
            
            $statusCode = [int]$webResponse.StatusCode
            
            # Parse JSON response if content type is JSON
            if ($webResponse.Content -and $webResponse.Headers["Content-Type"] -like "*application/json*") {
                try {
                    $response = $webResponse.Content | ConvertFrom-Json
                } catch {
                    $response = $webResponse.Content
                }
            } else {
                $response = $webResponse.Content
            }
        }
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Success "Status: $statusCode (Expected: $ExpectedStatus)"
            
            if ($response) {
                Write-Info "Response:"
                if ($response -is [string]) {
                    Write-Host $response
                } else {
                    $response | ConvertTo-Json -Depth 10 | Write-Host
                }
            }
            
            return $response
        } else {
            Write-Error "Status: $statusCode (Expected: $ExpectedStatus)"
            return $null
        }
    }
    catch {
        $actualStatus = $null
        
        # Extract status code from exception
        if ($_.Exception.Response) {
            $actualStatus = [int]$_.Exception.Response.StatusCode
        } elseif ($_.Exception.Message -match "(\d{3})") {
            $actualStatus = [int]$matches[1]
        }
        
        if ($actualStatus -eq $ExpectedStatus) {
            Write-Success "Status: $actualStatus (Expected: $ExpectedStatus)"
            
            if ($_.ErrorDetails.Message) {
                Write-Info "Error Response:"
                Write-Host $_.ErrorDetails.Message
            }
            
            return $null
        } else {
            Write-Error "Status: $actualStatus (Expected: $ExpectedStatus)"
            Write-Error "Error: $($_.Exception.Message)"
            
            if ($_.ErrorDetails.Message) {
                Write-Error "Response: $($_.ErrorDetails.Message)"
            }
            
            return $null
        }
    }
    
    Write-Host ""
}

# Wait for service to be ready
function Wait-ForService {
    Write-Info "Waiting for Product Service to be ready..."
    
    $maxAttempts = 30
    $attempt = 1
    
    while ($attempt -le $maxAttempts) {
        try {
            if ($PSVersionTable.PSVersion.Major -ge 7) {
                $response = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 5
            } else {
                $webResponse = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 5 -UseBasicParsing
                $response = $webResponse.Content | ConvertFrom-Json
            }
            
            if ($response) {
                Write-Success "Service is ready!"
                return
            }
        }
        catch {
            Write-Info "Attempt $attempt/$maxAttempts - Service not ready, waiting..."
            Start-Sleep -Seconds 2
            $attempt++
        }
    }
    
    Write-Error "Service failed to start within expected time"
    exit 1
}

# =============================================================================
# Test Data
# =============================================================================

$ValidProduct1 = @{
    name = "iPhone 15 Pro"
    description = "Latest iPhone with advanced features"
    price = 999.99
    stockQuantity = 50
    sku = "IPHONE-15-PRO-TEST"
    category = "ELECTRONICS"
} | ConvertTo-Json

$ValidProduct2 = @{
    name = "Samsung Galaxy S24"
    description = "Premium Android smartphone"
    price = 899.99
    stockQuantity = 30
    sku = "GALAXY-S24-TEST"
    category = "ELECTRONICS"
} | ConvertTo-Json

$ValidProduct3 = @{
    name = "Nike Air Max"
    description = "Comfortable running shoes"
    price = 129.99
    stockQuantity = 100
    sku = "NIKE-AIR-MAX-TEST"
    category = "CLOTHING"
} | ConvertTo-Json

$UpdateProduct = @{
    name = "iPhone 15 Pro Max"
    description = "Updated iPhone with larger screen"
    price = 1099.99
    category = "ELECTRONICS"
} | ConvertTo-Json

$StockDecrease = @{
    quantity = 5
} | ConvertTo-Json

$InvalidProductNoName = @{
    description = "Product without name"
    price = 99.99
    stockQuantity = 10
    sku = "NO-NAME-TEST"
    category = "ELECTRONICS"
} | ConvertTo-Json

$InvalidProductNegativePrice = @{
    name = "Invalid Product"
    description = "Product with negative price"
    price = -10.00
    stockQuantity = 10
    sku = "INVALID-PRICE-TEST"
    category = "ELECTRONICS"
} | ConvertTo-Json

# =============================================================================
# Test Scenarios
# =============================================================================

function Run-HealthCheck {
    Write-Info "=== HEALTH CHECK ==="
    
    Test-Http -TestName "Health Check" -Method "GET" -Url "$BaseUrl/actuator/health" -ExpectedStatus "200" -Description "Verify service is healthy and ready"
}

function Run-CreateProductTests {
    Write-Info "=== CREATE PRODUCT TESTS ==="
    
    # Test 1: Create valid product
    $response = Test-Http -TestName "Create Product - Valid Data" -Method "POST" -Url $ApiBase -Data $ValidProduct1 -ExpectedStatus "201" -Description "Create a product with all valid fields"
    
    if ($response -and $response.id) {
        $script:ProductId1 = $response.id
        Write-Info "Created product ID: $($script:ProductId1)"
    } else {
        Write-Warning "Failed to create first product - some tests may fail"
    }
    
    # Test 2: Create another valid product
    $response = Test-Http -TestName "Create Product - Second Valid Product" -Method "POST" -Url $ApiBase -Data $ValidProduct2 -ExpectedStatus "201" -Description "Create another product for list testing"
    
    if ($response -and $response.id) {
        $script:ProductId2 = $response.id
        Write-Info "Created product ID: $($script:ProductId2)"
    } else {
        Write-Warning "Failed to create second product - some tests may fail"
    }
    
    # Test 3: Create third product (different category)
    $response = Test-Http -TestName "Create Product - Different Category" -Method "POST" -Url $ApiBase -Data $ValidProduct3 -ExpectedStatus "201" -Description "Create product in different category"
    
    if ($response -and $response.id) {
        $script:ProductId3 = $response.id
        Write-Info "Created product ID: $($script:ProductId3)"
    } else {
        Write-Warning "Failed to create third product - some tests may fail"
    }
    
    # Test 4: Try to create duplicate SKU
    Test-Http -TestName "Create Product - Duplicate SKU" -Method "POST" -Url $ApiBase -Data $ValidProduct1 -ExpectedStatus "409" -Description "Should fail when trying to create product with existing SKU"
    
    # Test 5: Create product without name
    Test-Http -TestName "Create Product - Missing Name" -Method "POST" -Url $ApiBase -Data $InvalidProductNoName -ExpectedStatus "400" -Description "Should fail validation when name is missing"
    
    # Test 6: Create product with negative price
    Test-Http -TestName "Create Product - Negative Price" -Method "POST" -Url $ApiBase -Data $InvalidProductNegativePrice -ExpectedStatus "400" -Description "Should fail validation when price is negative"
}

function Run-GetProductTests {
    Write-Info "=== GET PRODUCT TESTS ==="
    
    # Test 1: Get existing product (only if we have a valid ID)
    if ($script:ProductId1) {
        Test-Http -TestName "Get Product - Valid ID" -Method "GET" -Url "$ApiBase/$($script:ProductId1)" -ExpectedStatus "200" -Description "Retrieve product by valid ID"
    } else {
        Write-Warning "Skipping Get Product test - no valid product ID available"
        Increment-Test
        Write-Error "Status: SKIPPED (Expected: 200) - No valid product ID"
    }
    
    # Test 2: Get non-existent product
    Test-Http -TestName "Get Product - Non-existent ID" -Method "GET" -Url "$ApiBase/00000000-0000-0000-0000-000000000000" -ExpectedStatus "404" -Description "Should return 404 for non-existent product"
    
    # Test 3: Get product with invalid UUID format
    Test-Http -TestName "Get Product - Invalid UUID" -Method "GET" -Url "$ApiBase/invalid-uuid" -ExpectedStatus "400" -Description "Should return 400 for invalid UUID format"
}

function Run-ListProductsTests {
    Write-Info "=== LIST PRODUCTS TESTS ==="
    
    # Test 1: List all products (default pagination)
    Test-Http -TestName "List Products - Default Pagination" -Method "GET" -Url $ApiBase -ExpectedStatus "200" -Description "List all products with default pagination"
    
    # Test 2: List products with custom page size - Fixed URL construction
    $url2 = $ApiBase + "?size=2"
    Test-Http -TestName "List Products - Custom Page Size" -Method "GET" -Url $url2 -ExpectedStatus "200" -Description "List products with page size of 2"
    
    # Test 3: Filter by category - Fixed URL construction
    $url3 = $ApiBase + "?category=ELECTRONICS"
    Test-Http -TestName "List Products - Filter by Electronics" -Method "GET" -Url $url3 -ExpectedStatus "200" -Description "Filter products by ELECTRONICS category"
    
    # Test 4: Filter by name - Fixed URL construction
    $url4 = $ApiBase + "?name=iPhone"
    Test-Http -TestName "List Products - Filter by Name" -Method "GET" -Url $url4 -ExpectedStatus "200" -Description "Filter products by name containing 'iPhone'"
    
    # Test 5: Combined filters - Fixed URL construction
    $url5 = $ApiBase + "?category=ELECTRONICS&name=iPhone"
    Test-Http -TestName "List Products - Combined Filters" -Method "GET" -Url $url5 -ExpectedStatus "200" -Description "Filter by both category and name"
}

function Run-UpdateProductTests {
    Write-Info "=== UPDATE PRODUCT TESTS ==="
    
    # Test 1: Update existing product (only if we have a valid ID)
    if ($script:ProductId1) {
        Test-Http -TestName "Update Product - Valid Data" -Method "PUT" -Url "$ApiBase/$($script:ProductId1)" -Data $UpdateProduct -ExpectedStatus "200" -Description "Update product with valid data"
    } else {
        Write-Warning "Skipping Update Product test - no valid product ID available"
        Increment-Test
        Write-Error "Status: SKIPPED (Expected: 200) - No valid product ID"
    }
    
    # Test 2: Update non-existent product
    Test-Http -TestName "Update Product - Non-existent ID" -Method "PUT" -Url "$ApiBase/00000000-0000-0000-0000-000000000000" -Data $UpdateProduct -ExpectedStatus "404" -Description "Should return 404 when updating non-existent product"
}

function Run-StockOperationsTests {
    Write-Info "=== STOCK OPERATIONS TESTS ==="
    
    # Test 1: Decrease stock with valid quantity (only if we have a valid ID)
    if ($script:ProductId2) {
        Test-Http -TestName "Decrease Stock - Valid Quantity" -Method "POST" -Url "$ApiBase/$($script:ProductId2)/decrease-stock" -Data $StockDecrease -ExpectedStatus "200" -Description "Decrease stock by valid quantity"
    } else {
        Write-Warning "Skipping Decrease Stock test - no valid product ID available"
        Increment-Test
        Write-Error "Status: SKIPPED (Expected: 200) - No valid product ID"
    }
    
    # Test 2: Decrease stock for non-existent product
    Test-Http -TestName "Decrease Stock - Non-existent Product" -Method "POST" -Url "$ApiBase/00000000-0000-0000-0000-000000000000/decrease-stock" -Data $StockDecrease -ExpectedStatus "404" -Description "Should return 404 for non-existent product"
}

function Run-DeleteProductTests {
    Write-Info "=== DELETE PRODUCT TESTS ==="
    
    # Test 1: Delete existing product (only if we have a valid ID)
    if ($script:ProductId3) {
        Test-Http -TestName "Delete Product - Valid ID" -Method "DELETE" -Url "$ApiBase/$($script:ProductId3)" -ExpectedStatus "204" -Description "Delete product (soft delete)"
    } else {
        Write-Warning "Skipping Delete Product test - no valid product ID available"
        Increment-Test
        Write-Error "Status: SKIPPED (Expected: 204) - No valid product ID"
    }
    
    # Test 2: Delete non-existent product
    Test-Http -TestName "Delete Product - Non-existent ID" -Method "DELETE" -Url "$ApiBase/00000000-0000-0000-0000-000000000000" -ExpectedStatus "404" -Description "Should return 404 when deleting non-existent product"
}

# =============================================================================
# Main Test Execution
# =============================================================================

function Main {
    Write-Info "=== PRODUCT SERVICE API TEST SUITE ==="
    Write-Info "Base URL: $BaseUrl"
    Write-Info "API Base: $ApiBase"
    Write-Info "Log File: $LogFile"
    Write-Info "Timestamp: $Timestamp"
    Write-Info "PowerShell Version: $($PSVersionTable.PSVersion)"
    Write-Host ""
    
    # Wait for service to be ready
    Wait-ForService
    Write-Host ""
    
    # Run all test suites
    Run-HealthCheck
    Write-Host ""
    
    Run-CreateProductTests
    Write-Host ""
    
    Run-GetProductTests
    Write-Host ""
    
    Run-ListProductsTests
    Write-Host ""
    
    Run-UpdateProductTests
    Write-Host ""
    
    Run-StockOperationsTests
    Write-Host ""
    
    Run-DeleteProductTests
    Write-Host ""
    
    # Test Summary
    Write-Info "=== TEST SUMMARY ==="
    Write-Info "Total Tests: $($script:TotalTests)"
    Write-Success "Passed: $($script:PassedTests)"
    Write-Error "Failed: $($script:FailedTests)"
    
    if ($script:FailedTests -eq 0) {
        Write-Success "🎉 ALL TESTS PASSED!"
        exit 0
    } else {
        Write-Error "❌ SOME TESTS FAILED!"
        Write-Info "Check the log file for details: $LogFile"
        exit 1
    }
}

# Run main function
Main