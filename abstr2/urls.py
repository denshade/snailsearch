from abstr2.context import ContextMap


def is_supported_site(contextmap: ContextMap):
    return contextmap.current_url.startswith("https://") or contextmap.current_url.startswith("http://")

def does_not_need_be_indexed(contextmap: ContextMap):
    url = contextmap.current_url
    urlfilter = contextmap.url_filter
    rp = contextmap.rp
    if not (is_supported_site(contextmap)):
        return False
    if not urlfilter.matches(url):
        return False
    if rp is not None and not rp.can_fetch("snail", url):
        return False
