import requests
from bs4 import BeautifulSoup
import urllib.parse

class Filter:
    def __init__(self, required_words, forbidden_words):
        self.forbidden_words = forbidden_words
        self.required_words = required_words

    def matches(self, contents):
        for required_word in self.required_words:
            if required_word not in contents:
                return False

        for forbidden_word in self.forbidden_words:
            if forbidden_word in contents:
                return False

        return True

def search_website(url, text_filter, url_filter, debug = True, visited_sites = []):
    for anchor in get_anchors_for_site(url):
        if anchor in visited_sites:
            continue
        if not url_filter.matches(anchor):
            if debug:
                print("Url did not match: " + anchor)
            continue

        match = get_caption_for_site(anchor, text_filter)
        if match:
            print("+" + anchor)
        else:
            if debug:
                print("-" + anchor)

        visited_sites.append(anchor)

        for visited_site in search_website(anchor, text_filter, url_filter, debug, visited_sites):
            visited_sites.append(visited_site)
    return visited_sites

def get_anchors_for_site(url):
    # TODO filter out robots.txt
    response = requests.get(url)
    if response.status_code == 200:
        soup = BeautifulSoup(response.text, 'html.parser')
        for a in soup.find_all('a', href=True):
            link = a['href']
            if "http" not in link:
                yield urllib.parse.urljoin(url, a['href'])
            else:
                yield a['href']

def get_caption_for_site(url, filter):
    response = requests.get(url)
    if response.status_code == 200:
        soup = BeautifulSoup(response.text, 'html.parser')
        all_text = soup.get_text(strip=True, separator='\n')
        return filter.matches(all_text)

search_website("https://www.vrt.be/vrtnws/nl", Filter(["Ninove"], []), Filter(["2024"], ["?linkId"]), False)
