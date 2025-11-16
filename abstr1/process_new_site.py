from urllib.parse import urljoin, urlsplit, urlunsplit
import urllib.robotparser

import requests

from abstr2.hosts import add_to_unknown_hosts, load_hosts, create_host_specific_index, is_etag_in_index
from abstr2.robots import do_robot_delay, load_robots_parser
from abstr2.site_processing import etag_head
from abstr2.urls import does_not_need_be_indexed
from snail_pipes.URLInput import process
from snail_pipes.url_filters import URLFilter
from abstr2.context import ContextMap

#TODO no literals in here, only abstract workflow.


# SQLlite -> URLInput -(Anchors)> AnchorFilter -(Anchors)> Feedback to URLInput
#          -(Content)> ContentFilter -(URL + context)> Print the context


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


def crawl(url, visited, urlfilter, contextmap: ContextMap):
    cached = 0
    processed = 0
    contextmap.url_filter = urlfilter
    cursor = contextmap.index_cursor
    con = contextmap.index_connection
    urls = [url]
    for url in urls:
        contextmap.current_url = url
        if does_not_need_be_indexed(contextmap):
            add_to_unknown_hosts(contextmap)
            visited.add(url)
            urls.remove(url)
            continue
        try:
            tag = etag_head(url)
            in_database = is_etag_in_index(url, tag, cursor)
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
        do_robot_delay(contextmap)
        if len(visited) % 10 == 1:
            urlsL = len(urls)
            visitedL = len(visited)
            print(f"count: todo {urlsL} vs visited {visitedL}, cache {cached} processed {processed}")
    return visited


def is_supported_site(contextmap: ContextMap):
    return contextmap.current_url.startswith("https://") or contextmap.current_url.startswith("http://")


def create_index_for_hostname(hostname, contextmap: ContextMap, starturl=None):
    contextmap.current_host = hostname
    contextmap = create_host_specific_index(contextmap)
    contextmap = load_hosts(contextmap)
    contextmap = load_robots_parser(contextmap)
    if starturl == None:
        starturl = f"https://{hostname}"

    print(crawl(starturl, set(), URLFilter(f"https://{hostname}", []), contextmap))

contextmap = ContextMap()
#create_db("nl.wikipedia.org", "https://nl.wikipedia.org/wiki/Hoofdpagina")
create_index_for_hostname("lite.cnn.com", contextmap)
#create_db("www.demorgen.be")
#create_db("nos.nl")
#create_db("rtl.nl")

