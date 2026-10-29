import urllib.request
import json
import urllib.parse

HEADERS = {"User-Agent": "CleanTVLauncher/1.0"}

def get_image_url(wiki, title):
    api = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(title)}&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(api, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=8) as r:
        data = json.loads(r.read())
        pages = data.get("query", {}).get("pages", {})
        for pid, p in pages.items():
            print(f"{title} -> {p['imageinfo'][0]['url']}")

get_image_url("interstellarfilm", "File:Josephcooper1.jpg")
get_image_url("interstellarfilm", "File:Josephcooper2.jpg")
