from urllib.parse import urljoin
import urllib.robotparser
import time
import sqlite3

import requests

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
# TODO use head to avoid

# Query to database.
# TODO convert text to specific filter.



# SQLlite -> URLInput -(Anchors)> AnchorFilter -(Anchors)> Feedback to URLInput
#          -(Content)> ContentFilter -(URL + context)> Print the context


def is_in_db(url, cur):
    res = cur.execute(f"SELECT count(1) from site where url = '{url}'")
    count = res.fetchone()
    return count[0] > 0


def is_in_db_etag(url, etag, cur):
    res = cur.execute(f"SELECT count(1) from site where url = '{url}' and etag = '{etag}'")
    count = res.fetchone()
    return count[0] > 0


def etag_head(url):
    response = requests.head(url)
    tag = response.headers.get("ETag")
    if tag is None:
        return None
    return tag.replace("\"", "")


def add_to_db(url, text, cur, etag):
    sql = f"DELETE from site where url = ?"
    cur.execute(sql, (url,))
    sql = f"INSERT INTO site(url, etag, text) values(?, ?, ?)"
    cur.execute(sql, (url, etag, text))
    con.commit()


def crawl(url, filters, visited, rp, urlfilter, cursor):
    urls = [url]
    for url in urls:
        tag = etag_head(url)
        in_database = is_in_db_etag(url, tag, cursor)
        if in_database:
            continue

        if not urlfilter.matches(url):
            #print(f"skipped {url}")
            visited.add(url)
            urls.remove(url)
            continue
        if not rp.can_fetch("snail", url):
            #print(f"skipped {url}")
            urls.remove(url)
            continue
        try:
            #print(f"processing {url}")
            urls.remove(url)
            (wordlist, anchorlist, etag) = process(url)
            add_to_db(url, ",".join(map(str, set(wordlist))), cur, etag)
            for filter in filters:
                if filter.matches(wordlist):
                    print(f"{filter.context(wordlist)} {url}")
            visited.add(url)
            for anchor in anchorlist:
                full_url = urljoin(url, anchor)
                if full_url not in visited and full_url not in urls:
                    urls.append(full_url)
        except RuntimeError as error:
            print(error)
            #print(f"skipped {url}")
            visited.add(url)
        robot_delay(rp)
        if len(visited) % 10 == 1:
            urlsL = len(urls)
            visitedL = len(visited)
            print(f"count: todo {urlsL} vs visited {visitedL}")
    return visited


def robot_delay(rp):
    delay = rp.crawl_delay("snail")
    if delay is None:
        delay = 1
    time.sleep(delay)

con = sqlite3.connect("websites.db")
cur = con.cursor()
cur.execute("CREATE TABLE IF NOT EXISTS site(URL, etag, text)")

rp = urllib.robotparser.RobotFileParser()
rp.set_url("https://www.vrt.be/robots.txt")
rp.read()

print(crawl("https://www.vrt.be/vrtnws/nl", [MustContainInDocument("Poetin")], set(), rp, URLFilter("https://www.vrt.be/vrtnws/nl", ["podcasts", "#main-content"]), cur))
