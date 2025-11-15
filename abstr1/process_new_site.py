from urllib.parse import urljoin, urlsplit, urlunsplit
import urllib.robotparser
import time
import sqlite3

import requests

from snail_pipes.URLInput import process
from snail_pipes.url_filters import URLFilter
from abstr2.context import ContextMap




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


def add_to_hosts(url, cur, con):
    sql = f"INSERT OR IGNORE INTO host(url) values(?)"
    cur.execute(sql, (url,))
    con.commit()


def crawl(url, visited, rp, urlfilter, cursor, con, hostcursor, hostcon, contextmap):
    cached = 0
    processed = 0
    urls = [url]
    for url in urls:
        contextmap.current_url = url
        if not (is_supported_site(url)):
            continue
        if not urlfilter.matches(url):
            split_url = urlsplit(url)
            clean_url = f"{split_url.scheme}://{split_url.netloc}"
            print(f"skipped {clean_url}")
            add_to_hosts(clean_url, hostcursor, hostcon)
            visited.add(url)
            urls.remove(url)
            continue
        if rp is not None and not rp.can_fetch("snail", url):
            #print(f"skipped {url}")
            urls.remove(url)
            continue
        try:
            tag = etag_head(url)
            in_database = is_in_db_etag(url, tag, cursor)
            if in_database:
                urls.remove(url)
                visited.add(url)
                cached += 1
                continue
            #print(f"processing {url}")
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
            #print(f"skipped {url}")
            visited.add(url)
        robot_delay(rp)
        if len(visited) % 10 == 1:
            urlsL = len(urls)
            visitedL = len(visited)
            print(f"count: todo {urlsL} vs visited {visitedL}, cache {cached} processed {processed}")
    return visited


def is_supported_site(contextmap: ContextMap):
    return contextmap.current_url.startswith("https://") or contextmap.current_url.startswith("http://")


def robot_delay(rp):
    delay = rp.crawl_delay("snail")
    if delay is None:
        delay = 1
    time.sleep(delay)


def create_db(hostname, contextmap: ContextMap, starturl=None):
    con = sqlite3.connect(f"../data/{hostname}.db")
    cur = con.cursor()
    cur.execute("CREATE TABLE IF NOT EXISTS site(URL, etag, text)")

    hostcon = sqlite3.connect(f"../data/hosts.db")
    hostcur = hostcon.cursor()
    hostcur.execute("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))")

    rp = urllib.robotparser.RobotFileParser()
    try:
        with urllib.request.urlopen(urllib.request.Request(f"https://{hostname}/robots.txt",
                                                       headers={'User-Agent': 'Python'})) as response:
            rp.parse(response.read().decode("utf-8").splitlines())
    except:
        print("error load robots.txt")
        rp = None
    if starturl == None:
        starturl = f"https://{hostname}"

    print(crawl(starturl, set(), rp,
                URLFilter(f"https://{hostname}", []), cur, con, hostcur, hostcon, contextmap))

contextmap = ContextMap()
#create_db("nl.wikipedia.org", "https://nl.wikipedia.org/wiki/Hoofdpagina")
create_db("lite.cnn.com", contextmap)
#create_db("www.demorgen.be")
#create_db("nos.nl")
#create_db("rtl.nl")

