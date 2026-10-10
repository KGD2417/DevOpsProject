$wc = New-Object System.Net.WebClient
$wc.DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/maven-3.9.6-bin.jar', 'C:\Users\KGD546\Documents\DevOpsProject\maven-3.9.6-bin.jar')
Write-Host "Maven downloaded successfully"