$uri = "http://localhost:10086/api/files"
$filePath = "./test-upload.txt"
$fileName = Split-Path $filePath -Leaf

# Read file content
$fileContent = Get-Content $filePath -Raw

# Generate boundary
$boundary = [System.Guid]::NewGuid().ToString()

# Create body
$body = @"
--$boundary
Content-Disposition: form-data; name="file"; filename="$fileName"
Content-Type: text/plain

$fileContent
--$boundary--
"@

# Send request
Invoke-WebRequest -Uri $uri -Method Post -Body $body -ContentType "multipart/form-data; boundary=$boundary" -UseBasicParsing