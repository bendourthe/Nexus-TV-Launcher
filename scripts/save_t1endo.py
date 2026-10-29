import urllib.request
import json
import urllib.parse
import io
import PIL.Image

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0",
    "Referer": "https://terminator.fandom.com/"
}

api = "https://terminator.fandom.com/api.php?action=query&titles=File:T1EndoPhoto.jpg&prop=imageinfo&iiprop=url&format=json"
req = urllib.request.Request(api, headers=HEADERS)
with urllib.request.urlopen(req, timeout=8) as r:
    data = json.loads(r.read())
    pages = data.get("query", {}).get("pages", {})
    for pid, p in pages.items():
        url = p['imageinfo'][0]['url']
        print(f"URL: {url}")
        img_req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(img_req, timeout=10) as ir:
            img = PIL.Image.open(io.BytesIO(ir.read())).convert("RGBA")
            w, h = img.size
            print(f"Size: {w}x{h}")
            # Tight crop on head
            crop = img.crop((int(w * 0.15), 0, int(w * 0.85), int(h * 0.70)))
            cw, ch = crop.size
            scale = 260 / cw
            scaled = crop.resize((260, int(ch * scale)), PIL.Image.LANCZOS)
            canvas = PIL.Image.new("RGBA", (320, 320), (14, 16, 22, 255))
            canvas.paste(scaled, ((320 - 260)//2, 20), mask=scaled if scaled.mode == 'RGBA' else None)
            canvas.save("launcher/app/src/main/res/drawable/ic_avatar_scifi_t800.png")
            print("Saved T1EndoPhoto to ic_avatar_scifi_t800.png")
