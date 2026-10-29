import urllib.request
import json
import urllib.parse
import io
import PIL.Image

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0",
    "Referer": "https://witcher.fandom.com/"
}

api = "https://witcher.fandom.com/api.php?action=query&titles=File:Tw3_Geralt_of_Rivia_new_render.png&prop=imageinfo&iiprop=url&format=json"
req = urllib.request.Request(api, headers=HEADERS)
with urllib.request.urlopen(req, timeout=8) as r:
    data = json.loads(r.read())
    pages = data.get("query", {}).get("pages", {})
    for pid, p in pages.items():
        if "imageinfo" in p:
            url = p['imageinfo'][0]['url']
            print(f"URL: {url}")
            img_req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(img_req, timeout=10) as ir:
                img = PIL.Image.open(io.BytesIO(ir.read())).convert("RGBA")
                w, h = img.size
                print(f"Size: {w}x{h}")
                # Crop on head & bust
                crop = img.crop((int(w * 0.1), 0, int(w * 0.9), int(h * 0.50)))
                cw, ch = crop.size
                scale = 260 / cw
                scaled = crop.resize((260, int(ch * scale)), PIL.Image.LANCZOS)
                canvas = PIL.Image.new("RGBA", (320, 320), (18, 20, 26, 255))
                canvas.paste(scaled, ((320 - 260)//2, 20), mask=scaled if scaled.mode == 'RGBA' else None)
                canvas.save("launcher/app/src/main/res/drawable/ic_avatar_game_geralt.png")
                print("Saved Tw3 Geralt render!")
