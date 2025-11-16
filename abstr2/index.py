from abstr2.context import ContextMap
from snail_pipes.URLInput import ProcessResult


def add_to_index(contextmap: ContextMap, process_result: ProcessResult):
    text = ",".join(map(str, set(process_result.wordlist)))
    etag = process_result.etag
    sql = f"DELETE from site where url = ?"
    contextmap.index_cursor.execute(sql, (contextmap.current_url,))
    sql = f"INSERT INTO site(url, etag, text) values(?, ?, ?)"
    contextmap.index_cursor.execute(sql, (contextmap.current_url, etag, text))
    contextmap.index_connection.commit()


def is_etag_in_index(url, etag, contextmap: ContextMap):
    res = contextmap.index_cursor.execute(f"SELECT count(1) from site where url = '{url}' and etag = '{etag}'")
    count = res.fetchone()
    return count[0] > 0


def get_urls(contextmap):
    res = contextmap.index_cursor.execute(f"SELECT url from site")
    results = res.fetchall()
    return [r[0] for r in results]
