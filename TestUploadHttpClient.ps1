$uri = "http://localhost:10086/api/files"
$filePath = "./test-upload.txt"
$fileName = Split-Path $filePath -Leaf

# Create HttpClient
$httpClient = New-Object System.Net.Http.HttpClient

# Create MultipartFormDataContent
$multipartContent = New-Object System.Net.Http.MultipartFormDataContent

# Add file content
$fileContent = New-Object System.Net.Http.StreamContent([System.IO.File]::OpenRead($filePath))
$fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("text/plain")
$multipartContent.Add($fileContent, "file", $fileName)

# Send request
$response = $httpClient.PostAsync($uri, $multipartContent).Result

# Get response content
$responseContent = $response.Content.ReadAsStringAsync().Result

# Output results
Write-Host "Status Code: $($response.StatusCode)"
Write-Host "Response Content: $responseContent"

# Clean up
$fileContent.Dispose()
$multipartContent.Dispose()
$httpClient.Dispose()