from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup
import re


def process(url):
    try:
        headers = {
            'User-Agent': 'Python',
        }
        response = requests.get(url, headers=headers)
        source_code = response.text
        etag = response.headers.get("ETag")
        if etag is not None:
            etag = etag.replace("\"", "")
        soup = BeautifulSoup(source_code, 'html.parser')

        text = soup.get_text(separator=' ')
        wordlist = re.split('[^a-zA-Z]', text.lower())

        # --- Create an anchor list ---
        anchorlist = []
        for a in soup.find_all('a', href=True):
            anchorlist.append(a['href'])
        return ProcessResult(wordlist, anchorlist, etag)
    except:
        return ProcessResult([], [], "")

class ProcessResult:
    def __init__(self, wordlist, anchorlist, etag):
        self.wordlist = wordlist
        self.anchorlist = anchorlist
        self.etag = etag

    def get_anchor_urls(self, base_url):
        urls_to_process = []
        for anchor in self.anchorlist:
            full_url = urljoin(base_url, anchor)
            urls_to_process.append(full_url)
        return urls_to_process