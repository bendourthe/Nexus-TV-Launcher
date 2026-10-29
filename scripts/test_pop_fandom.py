import urllib.request
import json
import urllib.parse
import time

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: support@cleantv.org)"
}

def test_wiki(api_base, title):
    url = f"{api_base}?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&pithumbsize=800&format=json"
    headers = dict(HEADERS)
    headers["Referer"] = api_base.split("/api.php")[0] + "/"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                print(f"[{title}] -> {p.get('thumbnail', {}).get('source')}")
    except Exception as e:
        print(f"[{title}] Error: {e}")

test_wiki("https://barbiemovies.fandom.com/api.php", "Barbie (2023)")
test_wiki("https://addamsfamily.fandom.com/api.php", "Wednesday Addams (Netflix)")
test_wiki("https://topgun.fandom.com/api.php", "Pete Mitchell")
test_wiki("https://ghostbusters.fandom.com/api.php", "Peter Venkman")
test_wiki("https://roalddahl.fandom.com/api.php", "Willy Wonka")
