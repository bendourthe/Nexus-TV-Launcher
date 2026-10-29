import urllib.request
import urllib.parse
import json

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: dev@cleantv.org)"
}

def search_files(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrnamespace=6&gsrlimit=5&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{query}] {p['title']} -> {p['imageinfo'][0]['url']}")
    except Exception as e:
        print(f"Error {query}: {e}")

search_files("CN Tower Toronto needle exterior")
search_files("Gene Wilder Willy Wonka 1971")
search_files("Red fox face close-up")
search_files("Siberian husky face close-up")
