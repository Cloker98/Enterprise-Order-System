# Debug URL test
param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ApiBase = "$BaseUrl/api/v1/products"

Write-Host "ApiBase: $ApiBase"

# Fixed URL construction
$url1 = $ApiBase + "?size=2"
$url2 = $ApiBase + "?category=ELECTRONICS"
$url3 = $ApiBase + "?name=iPhone"

Write-Host "Test URL 1: $url1"
Write-Host "Test URL 2: $url2"
Write-Host "Test URL 3: $url3"

# Test actual HTTP calls
try {
    Write-Host "`nTesting URL construction..."
    
    Write-Host "URL 1: $url1"
    $response1 = Invoke-WebRequest -Uri $url1 -Method GET -UseBasicParsing
    Write-Host "Status 1: $($response1.StatusCode)"
    
    Write-Host "URL 2: $url2"
    $response2 = Invoke-WebRequest -Uri $url2 -Method GET -UseBasicParsing
    Write-Host "Status 2: $($response2.StatusCode)"
    
    Write-Host "URL 3: $url3"
    $response3 = Invoke-WebRequest -Uri $url3 -Method GET -UseBasicParsing
    Write-Host "Status 3: $($response3.StatusCode)"
    
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "URL that failed: $($_.Exception.Response.ResponseUri)"
}