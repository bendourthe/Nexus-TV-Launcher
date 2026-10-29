import os
import io
import time
import urllib.request
import urllib.parse
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
}

def fetch_image(url, referer=None):
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=12) as resp:
        return PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")

def compose_bust(img, canvas_bg=(20, 23, 31, 255), target_w=258, top_pad=20, crop_box=None):
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
    return canvas

def compose_env(img, crop_box=None):
    if crop_box:
        img = img.crop(crop_box)
    w, h = img.size
    min_dim = min(w, h)
    cx = (w - min_dim) // 2
    cy = (h - min_dim) // 2
    sq = img.crop((cx, cy, cx + min_dim, cy + min_dim))
    sq = sq.resize((320, 320), PIL.Image.LANCZOS)
    return sq

# 1. Quantum Atom
url_atom = "https://upload.wikimedia.org/wikipedia/commons/1/1f/Stylised_atom_with_three_Bohr_model_orbits_and_stylised_nucleus.png"
img_atom = fetch_image(url_atom)
if img_atom:
    comp_atom = compose_env(img_atom)
    comp_atom.save(os.path.join(DRAWABLE_DIR, "ic_avatar_sci_quantum.png"), format="PNG")
    print("Saved sci_quantum")

time.sleep(0.5)

# 2. Barbie (Margot Robbie)
url_barbie = "https://upload.wikimedia.org/wikipedia/commons/3/3b/Margot_Robbie_Barbie_Premiere_%2853012600685%29.png"
img_barbie = fetch_image(url_barbie)
if img_barbie:
    bw, bh = img_barbie.size
    crop_box = (int(bw * 0.1), int(bh * 0.05), int(bw * 0.9), int(bh * 0.70))
    comp_barbie = compose_bust(img_barbie, target_w=260, top_pad=20, crop_box=crop_box)
    comp_barbie.save(os.path.join(DRAWABLE_DIR, "ic_avatar_pop_barbie.png"), format="PNG")
    print("Saved pop_barbie")

time.sleep(0.5)

# 3. Oppenheimer
url_opp = "https://upload.wikimedia.org/wikipedia/commons/3/31/J._Robert_Oppenheimer_1926_Alt.jpg"
img_opp = fetch_image(url_opp)
if img_opp:
    ow, oh = img_opp.size
    crop_box = (int(ow * 0.05), int(oh * 0.02), int(ow * 0.95), int(oh * 0.75))
    comp_opp = compose_bust(img_opp, target_w=260, top_pad=20, crop_box=crop_box)
    comp_opp.save(os.path.join(DRAWABLE_DIR, "ic_avatar_pop_oppenheimer.png"), format="PNG")
    print("Saved pop_oppenheimer")

time.sleep(0.5)

# 4. Wednesday Addams
url_wed = "https://upload.wikimedia.org/wikipedia/commons/d/de/Jenna_Ortega_2025.jpg"
img_wed = fetch_image(url_wed)
if img_wed:
    ww, wh = img_wed.size
    crop_box = (int(ww * 0.05), int(wh * 0.02), int(ww * 0.95), int(wh * 0.75))
    comp_wed = compose_bust(img_wed, target_w=260, top_pad=20, crop_box=crop_box)
    comp_wed.save(os.path.join(DRAWABLE_DIR, "ic_avatar_pop_wednesday.png"), format="PNG")
    print("Saved pop_wednesday")

time.sleep(0.5)

# 5. Willy Wonka from Fandom
url_wonka = "https://static.wikia.nocookie.net/roalddahl/images/d/d4/1964_Willy_Wonka_from_Charlie_And_The_Chocolate_Factory.jpg/revision/latest"
img_wonka = fetch_image(url_wonka, referer="https://roalddahl.fandom.com/")
if img_wonka:
    kw, kh = img_wonka.size
    crop_box = (int(kw * 0.05), int(kh * 0.02), int(kw * 0.95), int(kh * 0.75))
    comp_wonka = compose_bust(img_wonka, target_w=260, top_pad=20, crop_box=crop_box)
    comp_wonka.save(os.path.join(DRAWABLE_DIR, "ic_avatar_pop_wonka.png"), format="PNG")
    print("Saved pop_wonka")

print("Remaining 5 completed.")
