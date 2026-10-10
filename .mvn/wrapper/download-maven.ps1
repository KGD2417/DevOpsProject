$client = New-Object System.Net.WebClient
# Try different Maven wrapper locations
$urls = @(
    'https://repo.maven.apache.org/maven2/org/travis/maven/wrapper/0.5/maven-wrapper-0.5.jar'
    'https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.9.6/maven-3.9.6.bin.jar'
    'https://maven.mirrors.hoobly.com/org/apache/maven/maven-embedder/3.9.6/maven-embedder-3.9.6.jar'
)
foreach ($url in $urls) {
    try {
        Write-Host "Trying: $url"
        $client.DownloadFile($url, 'C:\Users\KGD546\Documents\DevOpsProject\.mvn\wrapper\maven-wrapper.jar')
        Write-Host "Success!"
        break
    } catch {
        Write-Host "Failed: $url"
    }
}