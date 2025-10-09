import re

# Regular expression pattern for URLs
url_pattern = re.compile(
    r'https?://[^\s]+'
)

# Function to parse URLs from a large file
def parse_urls(input_file, output_file):
    urls = []
    with open(input_file, 'r', encoding='utf-16', errors='ignore') as file:
        for line in file:
            for urlLine in url_pattern.findall(line):
                urls.append(urlLine)

    # Write the URLs to the new output file
    with open(output_file, 'w', encoding='utf-8') as output:
        for url in urls:
            output.write(url + '\n')

# Example usage
input_file = 'Z:\\wiki\\urls.txt'   # Input file containing text and URLs
output_file = 'extracted_urls.txt'  # Output file to write URLs to

parse_urls(input_file, output_file)
