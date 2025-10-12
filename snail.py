from urllib.parse import urljoin
import urllib.robotparser
import time

# Snail searches through one base url, with the following possible filters:
# MUST contain
# MUST NOT contain
# MUST have exactly
# MUST have all in sentence/document
# TODO MUST contain in sentence
# TODO MUST NOT contain in sentence
# TODO MUST NOT contain in document
# TODO MUST have exactly in sentence
# TODO MUST have exactly in document
# TODO MUST have all in sentence/document
# TODO text sentences from document.
# TODO add context to filter.
# TODO convert text to specific filter.
# TODO stick to url base.
# TODO fix stack overflow.
# TODO introduce filter and pipes

import requests
from bs4 import BeautifulSoup


# URLInput -(Anchors)> AnchorFilter -(Anchors)> Feedback to URLInput
#          -(Content)> ContentFilter -(URL + context)> Print the context

class URLFilter:
    def __init__(self, contain_text, must_not_contain):
        self.contain_text = contain_text
        self.must_not_contain = must_not_contain

    def matches(self, url):
        for item in self.must_not_contain:
            if item in url:
                return False
        return self.contain_text in url

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


def crawl(url, filters, visited, rp, urlfilter):
    if not urlfilter.matches(url):
        print(f"skipped {url}")
        visited.add(url)
        return visited
    if not rp.can_fetch("snail", url):
        print(f"skipped {url}")
    try:
        print(f"processing {url}")
        (wordlist, anchorlist) = process(url)
    except:
        print(f"skipped {url}")
        visited.add(url)
        return visited
    delay = rp.crawl_delay("snail")
    if delay is None:
        delay = 1
    time.sleep(delay)
    for filter in filters:
        if filter.matches(wordlist):
            print(url)
    visited.add(url)
    for anchor in anchorlist:
        full_url = urljoin(url, anchor)
        if full_url not in visited:
            visited.update(crawl(full_url, filters, visited, rp, urlfilter))
    v = len(visited)
    print(f"count: {v}")
    return visited
rp = urllib.robotparser.RobotFileParser()
rp.set_url("https://www.vrt.be/robots.txt")
rp.read()

print(crawl("https://www.vrt.be/vrtnws/nl", [MustContainInDocument("Poetin")], set(), rp, URLFilter("https://www.vrt.be/vrtnws/nl", ["podcasts", "#main-content"])))
