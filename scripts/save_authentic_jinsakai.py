import urllib.request
import io
import PIL.Image

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0",
    "Referer": "https://ghostoftsushima.fandom.com/"
}

url = "https://static.wikia.nocookie.net/ghostoftsushima/images/1/17/Jin_Sakai_as_The_Ghost.png/revision/latest"
req = urllib.request.Request(url, headers=HEADERS)
with urllib.request.urlopen(req, timeout=12) as resp:
    img = PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")
    w, h = img.size
    print(f"Jin size: {w}x{h}")
    # Crop on head & mask
    min_dim = min(w, h)
    crop = img.crop(((w - min_dim)//2, 0, (w - min_dim)//2 + min_dim, min_dim))
    scaled = crop.resize((320, 320), PIL.Image.LANCZOS)
    scaled.save("launcher/app/src/main/res/drawable/ic_avatar_game_jinsakai.png")
    print("Saved Jin Sakai as The Ghost!")
