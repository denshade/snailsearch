from urllib.parse import urljoin
import urllib.robotparser
import time
import sqlite3

import requests

from snail_pipes.URLInput import process
from snail_pipes.url_filters import URLFilter


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


def add_to_db(url, text, cur, etag, con):
    sql = f"DELETE from site where url = ?"
    cur.execute(sql, (url,))
    sql = f"INSERT INTO site(url, etag, text) values(?, ?, ?)"
    cur.execute(sql, (url, etag, text))
    con.commit()


def get_urls(cur):
    res = cur.execute(f"SELECT url from site")
    results = res.fetchall()
    return [r[0] for r in results]


def crawl_database(url, visited, rp, urlfilter, cursor, con):
    cached = 0
    processed = 0
    urls = [url]
    for url in get_urls(cursor):
        if url.startswith("mailto:"):
            continue
        if not urlfilter.matches(url):
            # print(f"skipped {url}")
            visited.add(url)
            urls.remove(url)
            print(f"|{url}")
        if not rp.can_fetch("snail", url):
            # print(f"skipped {url}")
            urls.remove(url)
            continue
        tag = etag_head(url)
        already_indexed = is_in_db_etag(url, tag, cursor)
        if already_indexed:
            if url in urls:
                urls.remove(url)
            visited.add(url)
            cached += 1
            continue
        try:
            # print(f"processing {url}")
            urls.remove(url)
            (wordlist, anchorlist, etag) = process(url)
            processed += 1
            add_to_db(url, ",".join(map(str, set(wordlist))), cursor, etag, con)
            visited.add(url)
            for anchor in anchorlist:
                full_url = urljoin(url, anchor)
                if full_url not in visited and full_url not in urls:
                    urls.append(full_url)
        except:
            # print(f"skipped {url}")
            visited.add(url)
        robot_delay(rp)
        if len(visited) % 10 == 1:
            urlsL = len(urls)
            visitedL = len(visited)
            print(f"count: todo {urlsL} vs visited {visitedL}, cache {cached} processed {processed}")
    return visited


def robot_delay(rp):
    delay = rp.crawl_delay("snail")
    if delay is None:
        delay = 1
    time.sleep(delay)


def create_db(url):
    con = sqlite3.connect(f"{url}.db")
    cur = con.cursor()
    rp = urllib.robotparser.RobotFileParser()
    rp.set_url(f"https://{url}/robots.txt")
    rp.read()

    print(crawl_database(f"https://{url}/", set(), rp,
                         URLFilter(f"https://{url}", []), cur, con))


# create_db("lite.cnn.com")
create_db("www.demorgen.be")
create_db("nos.nl")
create_db("rtl.nl")
