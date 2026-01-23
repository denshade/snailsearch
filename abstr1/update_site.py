from urllib.parse import urljoin

from abstr1.hosts import get_hosts
from abstr2.hosts import add_to_unknown_hosts, load_hosts, create_host_specific_index, start_scan_index, \
    stop_scan_index, update_scan_index_progress
from abstr2.index import add_to_index, is_etag_in_index, get_urls
from abstr2.logs import log_processing
from abstr2.robots import do_robot_delay, load_robots_parser
from abstr2.site_processing import etag_head
from abstr2.urls import needs_be_indexed
from snail_pipes.URLInput import process
from snail_pipes.url_filters import URLFilter
from abstr2.context import ContextMap


def crawl(host, contextmap: ContextMap):
    cached = 0
    processed = 0
    visited = 0
    urls_to_process = [host]
    start_scan_index(contextmap, host)
    all_urls = get_urls(contextmap)
    print(f"processing {host}")
    for url in all_urls:
        contextmap.current_url = url
        update_scan_index_progress(contextmap, host, visited * 100 / len(all_urls))
        visited += 1
        if not needs_be_indexed(contextmap):
            add_to_unknown_hosts(contextmap)
            try:
                urls_to_process.remove(url)
            except:
                continue
            continue
        tag = etag_head(url)
        in_database = is_etag_in_index(url, tag, contextmap)
        if in_database:
            try:
                urls_to_process.remove(url)
            except:
                continue
            cached += 1
            continue
        process_result = process(url)
        add_to_index(contextmap, process_result)
        for anchor in process_result.get_anchor_urls(url):
            if anchor not in urls_to_process:
                urls_to_process.append(anchor)
        processed += 1
        do_robot_delay(contextmap)
        log_processing(visited, urls_to_process, cached, processed)
    stop_scan_index(contextmap, host)
    return visited


def create_index_for_hostname(hostname, contextmap: ContextMap, starturl=None):
    contextmap.current_host = hostname
    contextmap = create_host_specific_index(contextmap)
    contextmap = load_hosts(contextmap)
    contextmap = load_robots_parser(contextmap)
    if starturl == None:
        starturl = f"https://{hostname}"
    contextmap.url_filter = URLFilter(f"https://{hostname}", [])
    print(crawl(starturl, contextmap))

for host in get_hosts():
    contextmap = ContextMap()
    create_index_for_hostname(host.replace(".db", ""), contextmap)

