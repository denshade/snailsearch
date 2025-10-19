import requests
from bs4 import BeautifulSoup
def process(url):

    response = requests.get(url)
    source_code = response.text
    etag = response.headers.get("ETag")
    soup = BeautifulSoup(source_code, 'html.parser')

    text = soup.get_text(separator=' ')
    wordlist = text.split()

    # --- Create an anchor list ---
    anchorlist = []
    for a in soup.find_all('a', href=True):
        anchorlist.append(a['href'])

    return (wordlist, anchorlist, etag)