import requests


def etag_head(url):
    response = requests.head(url)
    tag = response.headers.get("ETag")
    if tag is None:
        return None
    return tag.replace("\"", "")

