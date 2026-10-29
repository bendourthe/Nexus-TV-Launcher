import urllib.request
import json
import urllib.parse
import io
import PIL.Image

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0",
    "Referer": "https://bladerunner.fandom.com/"
}

api = "https://bladerunner.fandom.com/api.php?action=query&titles=File:Deckard.jpg&prop=imageinfo&iiprop=url&format=json"
req = urllib.request.Request(api, headers=HEADERS)
with urllib.request.urlopen(req, timeout=8) as r:
    data = json.loads(r.read())
    pages = data.get("query", {}).get("pages", {})
    for pid, p in pages.items():
        url = p['imageinfo'][0]['url']
        print(f"Deckard URL: {url}")
        img_req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(img_req, timeout=10) as ir:
            img = PIL.Image.open(io.BytesIO(ir.read())).convert("RGBA")
            print(f"Deckard image size: {img.size}")
            # Center square crop
            w, h = img.size
            min_dim = min(w, h)
            sq = img.crop(((w - min_dim)//2, 0, (w - min_dim)//2 + min_dim, min_dim)).resize((320, 320), PIL.Image.LANCZOS)
            sq.save("launcher/app/src/main/res/drawable/ic_avatar_scifi_deckard.png")
            print("Saved real Harrison Ford Deckard!")
