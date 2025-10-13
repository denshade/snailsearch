import requests
from bs4 import BeautifulSoup
def process(url):

    source_code = requests.get(url).text
    soup = BeautifulSoup(source_code, 'html.parser')

    text = soup.get_text(separator=' ')
    wordlist = text.split()

    # --- Create an anchor list ---
    anchorlist = []
    for a in soup.find_all('a', href=True):
        anchorlist.append(a['href'])

    return (wordlist, anchorlist)