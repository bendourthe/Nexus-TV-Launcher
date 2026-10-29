import urllib.request
import json
import urllib.parse

HEADERS = {"User-Agent": "CleanTVLauncher/1.0"}

def get_page_images(wiki, page):
    api = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(page)}&prop=images&format=json"
    req = urllib.request.Request(api, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=8) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{page}] Images:")
                for img in p.get("images", []):
                    print(f"  {img['title']}")
    except Exception as e:
        print(f"Error {page}: {e}")

get_page_images("interstellarfilm", "Joseph Cooper")
