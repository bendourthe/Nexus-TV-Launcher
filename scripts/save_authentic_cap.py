import urllib.request
import io
import PIL.Image

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0",
    "Referer": "https://marvelcinematicuniverse.fandom.com/"
}

url = "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/b/b7/Steve_Rogers_Infobox.jpg/revision/latest"
req = urllib.request.Request(url, headers=HEADERS)
with urllib.request.urlopen(req, timeout=12) as resp:
    img = PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")
    w, h = img.size
    print(f"Cap size: {w}x{h}")
    # Head and tactical armor crop
    crop = img.crop((int(w * 0.05), 0, int(w * 0.95), int(h * 0.50)))
    cw, ch = crop.size
    scale = 260 / cw
    scaled = crop.resize((260, int(ch * scale)), PIL.Image.LANCZOS)
    canvas = PIL.Image.new("RGBA", (320, 320), (16, 20, 28, 255))
    canvas.paste(scaled, ((320 - 260)//2, 20), mask=scaled if scaled.mode == 'RGBA' else None)
    canvas.save("launcher/app/src/main/res/drawable/ic_avatar_marvel_captain.png")
    print("Saved authentic Steve Rogers MCU render!")
