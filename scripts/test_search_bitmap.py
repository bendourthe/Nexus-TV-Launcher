import urllib.request
import urllib.parse
import json

HEADERS = {"User-Agent": "CleanTVLauncher/1.0 (dev@cleantv.org)"}

def search_images(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query + ' filetype:bitmap')}&gsrnamespace=6&gsrlimit=3&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{query}] {p['title']} -> {p['imageinfo'][0]['url']}")
    except Exception as e:
        print(f"Error {query}: {e}")

search_images("CN Tower Toronto")
search_images("Vulpes vulpes face")
search_images("Canis lupus head")
search_images("Panthera uncia head")
search_images("Gene Wilder Willy Wonka")
