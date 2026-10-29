import urllib.request
import urllib.parse
import json

HEADERS = {"User-Agent": "CleanLauncher/1.0 (audit@example.com)"}

def test_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrnamespace=6&gsrlimit=3&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    resp = json.loads(urllib.request.urlopen(req, timeout=8).read())
    pages = resp.get("query", {}).get("pages", {})
    results = []
    for p in pages.values():
        title = p.get("title")
        thumb = p.get("imageinfo", [{}])[0].get("thumburl")
        results.append((title, thumb))
    return results

def test_fandom(wiki, title):
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&format=json&pithumbsize=600"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0", "Referer": f"https://{wiki}.fandom.com/"})
    resp = json.loads(urllib.request.urlopen(req, timeout=8).read())
    pages = resp.get("query", {}).get("pages", {})
    if pages:
        p = list(pages.values())[0]
        thumb = p.get("thumbnail", {}).get("source")
        return thumb
    return None

print("Testing Commons Bear:", test_commons("Ursus maritimus head portrait"))
print("Testing Commons Fox:", test_commons("Vulpes vulpes head portrait"))
print("Testing Commons Eagle:", test_commons("Haliaeetus leucocephalus head portrait"))
print("Testing Commons Tiger:", test_commons("Panthera tigris head portrait"))
print("Testing Fandom Star Wars Vader:", test_fandom("starwars", "Darth_Vader"))
print("Testing Fandom Star Wars Picard (fix Luke):", test_fandom("starwars", "Luke_Skywalker"))
print("Testing Fandom Marvel Iron Man:", test_fandom("marvelcinematicuniverse", "Iron_Man"))
