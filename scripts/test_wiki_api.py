import urllib.request
import json
import urllib.parse
import PIL.Image
import io

def fetch_wiki_thumb(api_base, title):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Referer": api_base.split("/api.php")[0] + "/"
    }
    url = f"{api_base}?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&pithumbsize=1000&format=json"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=8) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                if "thumbnail" in p:
                    thumb_url = p["thumbnail"]["source"]
                    # Fetch actual image
                    img_req = urllib.request.Request(thumb_url, headers=headers)
                    with urllib.request.urlopen(img_req, timeout=10) as ir:
                        img = PIL.Image.open(io.BytesIO(ir.read()))
                        print(f"[{title}] SUCCESS! {img.size} from {thumb_url[:70]}...")
                        return img
    except Exception as e:
        print(f"[{title}] Failed: {e}")
    return None

fetch_wiki_thumb("https://marvelcinematicuniverse.fandom.com/api.php", "Steve Rogers")
fetch_wiki_thumb("https://dccu.fandom.com/api.php", "Clark Kent")
fetch_wiki_thumb("https://harrypotter.fandom.com/api.php", "Harry Potter")
fetch_wiki_thumb("https://gameofthrones.fandom.com/api.php", "Jon Snow")
fetch_wiki_thumb("https://en.wikipedia.org/w/api.php", "Albert Einstein")
