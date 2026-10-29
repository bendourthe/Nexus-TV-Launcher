import os
import io
import urllib.request
import PIL.Image
import PIL.ImageDraw

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build", "test_renders")
os.makedirs(OUT_DIR, exist_ok=True)

def fetch_img(url):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=15) as resp:
        return PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")

def compose_character_bust(img, canvas_bg=(20, 23, 31, 255), target_w=260, top_pad=22, crop_box=None):
    if crop_box:
        img = img.crop(crop_box)
    w, h = img.size
    scale = target_w / w
    target_h = int(h * scale)
    scaled = img.resize((target_w, target_h), PIL.Image.LANCZOS)
    
    canvas = PIL.Image.new("RGBA", (320, 320), canvas_bg)
    paste_x = (320 - target_w) // 2
    paste_y = top_pad
    canvas.paste(scaled, (paste_x, paste_y), mask=scaled if scaled.mode == 'RGBA' else None)
    
    # Generate circular preview
    mask = PIL.Image.new("L", (320, 320), 0)
    draw = PIL.ImageDraw.Draw(mask)
    draw.ellipse((0, 0, 320, 320), fill=255)
    circ = PIL.Image.new("RGBA", (320, 320), (0, 0, 0, 0))
    circ.paste(canvas, (0, 0), mask=mask)
    draw_c = PIL.ImageDraw.Draw(circ)
    draw_c.ellipse((2, 2, 318, 318), outline=(255, 255, 255, 80), width=4)
    return canvas, circ

# Test Spider-Man
spidey_url = "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/4/41/Spider-Man_Infobox.jpg/revision/latest"
spidey_raw = fetch_img(spidey_url)
sw, sh = spidey_raw.size
# crop head and bust
spidey_crop = spidey_raw.crop((int(sw * 0.1), int(sh * 0.05), int(sw * 0.9), int(sh * 0.75)))
canvas, circ = compose_character_bust(spidey_crop, target_w=260, top_pad=20)
canvas.save(os.path.join(OUT_DIR, "test_spiderman_sq.png"))
circ.save(os.path.join(OUT_DIR, "test_spiderman_circ.png"))
print("Saved Spider-Man test.")

# Test Wolverine
wolv_url = "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/7/7c/Wolverine_Infobox.png/revision/latest"
wolv_raw = fetch_img(wolv_url)
ww, wh = wolv_raw.size
wolv_crop = wolv_raw.crop((0, 0, ww, int(wh * 0.6)))
canvas, circ = compose_character_bust(wolv_crop, target_w=270, top_pad=18)
canvas.save(os.path.join(OUT_DIR, "test_wolverine_sq.png"))
circ.save(os.path.join(OUT_DIR, "test_wolverine_circ.png"))
print("Saved Wolverine test.")

# Test Batman
bat_url = "https://static.wikia.nocookie.net/dccu/images/2/2e/Batman_-_Justice_League_-_promo.jpg/revision/latest"
bat_raw = fetch_img(bat_url)
bw, bh = bat_raw.size
bat_crop = bat_raw.crop((int(bw * 0.15), int(bh * 0.05), int(bw * 0.85), int(bh * 0.65)))
canvas, circ = compose_character_bust(bat_crop, target_w=260, top_pad=22)
canvas.save(os.path.join(OUT_DIR, "test_batman_sq.png"))
circ.save(os.path.join(OUT_DIR, "test_batman_circ.png"))
print("Saved Batman test.")
