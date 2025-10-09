# Define the directory path, file pattern, and output file
$directory = "Z:\wiki\output_pages\"
$outputFile = "Z:\wiki\urls.txt"
$files = Get-ChildItem -Path $directory -Filter "*.txt"

# Clear the output file if it exists, or create it
Out-File -FilePath $outputFile -Force

# Loop through each file
foreach ($file in $files) {
    # Print the current file being processed
    Write-Host "Processing file: $($file.Name)"

    # Get the file content
    $content = Get-Content -Path $file.FullName

    # Search for lines containing "https://" or "http://"
    foreach ($line in $content) {
        if ($line -match "https?://") {
            # Append the found URL and the file name to the output file
            $output = "Found in $($file.Name): $line"
            Add-Content -Path $outputFile -Value $output
        }
    }
}
