import os
import sqlite3
from urllib.parse import urlsplit

from abstr2.context import ContextMap

class index:
    def __init__(self, index_name, update_start, update_end, pct_complete):
        self.index_name = index_name
        self.update_start = update_start
        self.update_end = update_end
        self.pct_complete = pct_complete

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
    _host_sql(contextmap, index_name, sql)
    return contextmap


def _host_sql(contextmap, index_name, sql):
    contextmap.host_cursor.execute(sql, (index_name,))
    contextmap.host_connection.commit()


def stop_scan_index(contextmap: ContextMap, index_name ):
    sql = f"UPDATE indices SET UPDATE_STOP = datetime(), PCT_COMPLETE=100 WHERE index_name = ?"
    _host_sql(contextmap, index_name, sql)
    return contextmap

def update_scan_index_progress(contextmap: ContextMap, index_name, pct):
    sql = f"UPDATE indices SET UPDATE_STOP = null, PCT_COMPLETE=? WHERE index_name = ?"
    contextmap.host_cursor.execute(sql, (pct, index_name,))
    contextmap.host_connection.commit()
    return contextmap

def get_indices(contextmap: ContextMap):
    res = contextmap.host_cursor.execute("SELECT * from indices")
    results = res.fetchall()
    converted = map(lambda result: index(result[0], result[1], result[2], result[3]), results)
    return list(converted)

def init_hosts_index(contextmap: ContextMap):
    contextmap.host_cursor.execute(
        "CREATE TABLE IF NOT EXISTS indices(index_name varchar, UPDATE_START timestamp, UPDATE_STOP timestamp, PCT_COMPLETE DOUBLE, UNIQUE(index_name))")
    contextmap.host_cursor.execute("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))")


def load_hosts(contextmap: ContextMap) -> ContextMap:
    dirname = os.path.dirname(os.path.realpath(__file__))
    directory = f"{dirname}/../data"
    hostcon = sqlite3.connect(f"{directory}/hosts.db")
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

