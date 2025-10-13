from urllib.parse import urljoin
import urllib.robotparser
import time

from snail_pipes.URLInput import process
from snail_pipes.document_filters import MustContainInDocument
from snail_pipes.url_filters import URLFilter


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



# URLInput -(Anchors)> AnchorFilter -(Anchors)> Feedback to URLInput
#          -(Content)> ContentFilter -(URL + context)> Print the context



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
