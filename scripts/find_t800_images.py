import urllib.request
import json
import urllib.parse

HEADERS = {"User-Agent": "CleanTVLauncher/1.0"}
api = "https://terminator.fandom.com/api.php?action=query&titles=T-800&prop=images&format=json"
req = urllib.request.Request(api, headers=HEADERS)
with urllib.request.urlopen(req, timeout=8) as r:
    data = json.loads(r.read())
    pages = data.get("query", {}).get("pages", {})
    for pid, p in pages.items():
        for img in p.get("images", []):
            print(f"  {img['title']}")
