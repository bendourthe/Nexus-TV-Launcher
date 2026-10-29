import urllib.request
import json
import urllib.parse
import io
import PIL.Image

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0",
    "Referer": "https://ghostoftsushima.fandom.com/"
}

api = "https://ghostoftsushima.fandom.com/api.php?action=query&generator=search&gsrsearch=Jin+Sakai&gsrnamespace=6&gsrlimit=5&prop=imageinfo&iiprop=url&format=json"
req = urllib.request.Request(api, headers=HEADERS)
with urllib.request.urlopen(req, timeout=8) as r:
    data = json.loads(r.read())
    pages = data.get("query", {}).get("pages", {})
    for pid, p in pages.items():
        print(f"{p['title']} -> {p['imageinfo'][0]['url']}")
