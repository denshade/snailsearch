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

def start_scan_index(contextmap: ContextMap, index_name ):
    contextmap.host_cursor.execute("DELETE from indices WHERE index_name = ?", (index_name,))
    sql = f"INSERT INTO indices(index_name, UPDATE_START, PCT_COMPLETE) values(?, datetime(), 0)"
    contextmap.host_cursor.execute(sql, (index_name,))
    return contextmap

def get_indices(contextmap: ContextMap):
    res = contextmap.host_cursor.execute("SELECT * from indices")
    results = res.fetchall()
    return results

def init_hosts_index(contextmap: ContextMap):
    contextmap.host_cursor.execute(
        "CREATE TABLE IF NOT EXISTS indices(index_name varchar, UPDATE_START timestamp, UPDATE_STOP timestamp, PCT_COMPLETE DOUBLE, UNIQUE(index_name))")
    contextmap.host_cursor.execute("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))")


def load_hosts(contextmap: ContextMap) -> ContextMap:
    hostcon = sqlite3.connect(f"../data/hosts.db")
    hostcur = hostcon.cursor()
    contextmap.host_connection = hostcon
    contextmap.host_cursor = hostcur
    init_hosts_index(contextmap)
    return contextmap


def create_host_specific_index(contextmap: ContextMap) -> ContextMap:
    hostname = contextmap.current_host
    con = sqlite3.connect(f"../data/{hostname}.db")
    cur = con.cursor()
    cur.execute("CREATE TABLE IF NOT EXISTS site(URL, etag, text)")
    contextmap.index_connection = con
    contextmap.index_cursor = con
    return contextmap

