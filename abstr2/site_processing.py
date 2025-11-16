import requests


def etag_head(url):
    try:
        response = requests.head(url)
        tag = response.headers.get("ETag")
    except:
        print(f"failed to head {url}")

    if tag is None:
        return None
    return tag.replace("\"", "")

