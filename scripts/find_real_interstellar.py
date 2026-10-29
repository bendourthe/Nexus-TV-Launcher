import urllib.request
import json
import urllib.parse

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0"
}

def search_fandom(wiki, query):
    api = f"https://{wiki}.fandom.com/api.php?action=query&generator=allimages&gailimit=10&gaifrom={urllib.parse.quote(query)}&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(api, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=8) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{wiki}] {p['title']} -> {p['imageinfo'][0]['url']}")
    except Exception as e:
        print(f"Error {wiki}: {e}")

search_fandom("interstellarfilm", "Cooper")
search_fandom("interstellarfilm", "Spacesuit")
