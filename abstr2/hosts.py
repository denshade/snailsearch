import sqlite3
from urllib.parse import urljoin, urlsplit, urlunsplit

from abstr2.context import ContextMap


def add_to_unknown_hosts(contextmap: ContextMap):
    split_url = urlsplit(contextmap.current_url)
    clean_url = f"{split_url.scheme}://{split_url.netloc}"
    print(f"skipped {clean_url}")
    add_to_hosts(clean_url, contextmap.host_cursor, contextmap.host_connection)


def add_to_hosts(host, cur, con):
    sql = f"INSERT OR IGNORE INTO host(url) values(?)"
    cur.execute(sql, (host,))
    con.commit()


def load_hosts(contextmap: ContextMap) -> ContextMap:
    hostcon = sqlite3.connect(f"../data/hosts.db")
    hostcur = hostcon.cursor()
    hostcur.execute("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))")
    contextmap.host_connection = hostcon
    contextmap.host_cursor = hostcur
    return contextmap


def create_host_specific_index(contextmap: ContextMap) -> ContextMap:
    hostname = contextmap.current_host
    con = sqlite3.connect(f"../data/{hostname}.db")
    cur = con.cursor()
    cur.execute("CREATE TABLE IF NOT EXISTS site(URL, etag, text)")
    contextmap.index_connection = con
    contextmap.index_cursor = con
    return contextmap


def is_etag_in_index(url, etag, cur):
    res = cur.execute(f"SELECT count(1) from site where url = '{url}' and etag = '{etag}'")
    count = res.fetchone()
    return count[0] > 0