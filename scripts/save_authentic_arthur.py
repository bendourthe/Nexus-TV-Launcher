import urllib.request
import io
import PIL.Image

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0",
    "Referer": "https://reddeadredemption.fandom.com/"
}

url = "https://static.wikia.nocookie.net/reddeadredemption/images/9/93/Arthur_Morgan_RDR2.png/revision/latest"
req = urllib.request.Request(url, headers=HEADERS)
with urllib.request.urlopen(req, timeout=12) as resp:
    img = PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")
    w, h = img.size
    print(f"Arthur size: {w}x{h}")
    # Crop on head & hat
    crop = img.crop((int(w * 0.15), 0, int(w * 0.85), int(h * 0.45)))
    cw, ch = crop.size
    scale = 260 / cw
    scaled = crop.resize((260, int(ch * scale)), PIL.Image.LANCZOS)
    canvas = PIL.Image.new("RGBA", (320, 320), (22, 24, 30, 255))
    canvas.paste(scaled, ((320 - 260)//2, 20), mask=scaled if scaled.mode == 'RGBA' else None)
    canvas.save("launcher/app/src/main/res/drawable/ic_avatar_game_arthur.png")
    print("Saved authentic Arthur Morgan RDR2 render!")
