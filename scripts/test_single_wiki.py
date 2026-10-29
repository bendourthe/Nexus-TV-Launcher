import urllib.request
import json
import urllib.parse
import time

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: support@cleantv.org)"
}

def test_single(api_base, title):
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

test_single("https://harrypotter.fandom.com/api.php", "Ronald Weasley")

pop_titles = [
    "Vito Corleone", "Barbie (film)", "Oppenheimer (film)", "Marilyn Monroe", "Elvis Presley",
    "Indiana Jones (character)", "Wednesday Addams", "Willy Wonka", "Pete Mitchell (Top Gun)", "Ghostbusters"
]
for t in pop_titles:
    time.sleep(0.4)
    test_single("https://en.wikipedia.org/w/api.php", t)
