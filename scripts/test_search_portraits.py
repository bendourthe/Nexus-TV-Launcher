import urllib.request
import urllib.parse
import json

HEADERS = {"User-Agent": "CleanTVLauncher/1.0 (dev@cleantv.org)"}

def search_best(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query + ' filetype:bitmap')}&gsrnamespace=6&gsrlimit=2&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{query}] -> {p['imageinfo'][0]['url']}")
                return p['imageinfo'][0]['url']
    except Exception as e:
        print(f"Error {query}: {e}")
    return None

search_best("Ursus arctos face")
search_best("Ailuropoda melanoleuca face")
search_best("Gorilla head portrait")
search_best("Bald eagle head portrait")
search_best("Welsh Corgi head portrait")
search_best("Siberian Husky head portrait")
search_best("Bengal cat portrait")
search_best("British Shorthair portrait")
search_best("Gene Wilder Willy Wonka")
search_best("Indiana Jones fedora portrait")
