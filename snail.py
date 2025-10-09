from urllib.parse import urljoin

# Snail searches through one base url, with the following possible filters:
# MUST contain
# MUST NOT contain
# MUST have exactly
# MUST have all in sentence/document
# support:
# TODO MUST contain in sentence
# TODO MUST NOT contain in sentence
# TODO MUST NOT contain in document
# TODO MUST have exactly in sentence
# TODO MUST have exactly in document
# TODO MUST have all in sentence/document
# TODO text sentences from document.
# TODO take robots into account.
# TODO loop through all anchors.
# TODO add context to filter.
# TODO convert text to specific filter.

import requests
from bs4 import BeautifulSoup
from collections import Counter

class MustContainInDocument:
    def __init__(self, contain_text):
        self.contain_text = contain_text

    def matches(self, document):
        return self.contain_text in document


def process(url):

    source_code = requests.get(url).text
    soup = BeautifulSoup(source_code, 'html.parser')

    text = soup.get_text(separator=' ')
    wordlist = text.split()

    # --- Create an anchor list ---
    anchorlist = []
    for a in soup.find_all('a', href=True):
        anchorlist.append(a['href'])

    return (wordlist, anchorlist)


def crawl(url, filters, visited):
    (wordlist, anchorlist) = process(url)
    for filter in filters:
        if filter.matches(wordlist):
            print(url)
    visited.append(url)
    for anchor in anchorlist:
        full_url = urljoin(url, anchor)
        if full_url not in visited:
            visited.extend(crawl(full_url, filters, visited))

    return visited


print(crawl("https://www.vrt.be/vrtnws/nl", [MustContainInDocument("Poetin")], list()))
