import os
import io
import time
import urllib.request
import urllib.parse
import json
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
BUILD_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build")
TEST_DIR = os.path.join(BUILD_DIR, "test_renders")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0 (dev@cleantv.org)",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
}

def fetch_image(url, referer=None):
    if not url:
        return None
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=12) as resp:
            return PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")
    except Exception as e:
        print(f"Error fetching {url[:60]}: {e}")
        return None

def compose_bust(img, canvas_bg=(20, 23, 31, 255), target_w=256, top_pad=22, crop_box=None):
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
    sq = img.crop((cx, cy, cx + min_dim, cy + min_dim)).resize((320, 320), PIL.Image.LANCZOS)
    return sq

def save_avatar(item_id, img):
    out = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
    img.save(out, format="PNG", optimize=True)
    print(f"Saved ic_avatar_{item_id}.png")

print("Applying visual refinements...")

# 1. Landmark CN Tower: exterior soaring needle
img_cntower = fetch_image("https://upload.wikimedia.org/wikipedia/commons/4/43/CN_Tower%2C_Toronto%2C_Canada_%28Unsplash_DJ_kOgH5u0o%29.jpg")
if img_cntower:
    cw, ch = img_cntower.size
    # Center on upper needle
    crop = img_cntower.crop((int(cw * 0.15), int(ch * 0.05), int(cw * 0.85), int(ch * 0.75))).resize((320, 320), PIL.Image.LANCZOS)
    save_avatar("landmark_cntower", crop)

# 2. Marvel: Wolverine, Spider-Man, Captain America, Thor, Panther, Strange, Deadpool
# Wolverine
wolv_circ = os.path.join(TEST_DIR, "test_wolverine_perfect_circ.png")
if os.path.exists(wolv_circ):
    save_avatar("marvel_wolverine", PIL.Image.open(wolv_circ).convert("RGBA"))
# Spider-Man
spidey_circ = os.path.join(TEST_DIR, "test_spiderman_integrated_circ.png")
if os.path.exists(spidey_circ):
    save_avatar("marvel_spiderman", PIL.Image.open(spidey_circ).convert("RGBA"))
# Superman
superman_circ = os.path.join(TEST_DIR, "test_superman_cavill_circ.png")
if os.path.exists(superman_circ):
    save_avatar("dc_superman", PIL.Image.open(superman_circ).convert("RGBA"))
# RoboCop
robocop_circ = os.path.join(TEST_DIR, "test_robocop_head_circ.png")
if os.path.exists(robocop_circ):
    save_avatar("scifi_robocop", PIL.Image.open(robocop_circ).convert("RGBA"))

# 3. Animals Close-Ups (Fox, Wolf, Leopard, Bear, Gorilla, Eagle)
animal_fixes = [
    ("animal_fox", "https://upload.wikimedia.org/wikipedia/commons/3/3f/Red_fox_face_mammal_vulpes_vulpes.jpg", None),
    ("animal_wolf", "https://upload.wikimedia.org/wikipedia/commons/a/a9/Canis_lupus_Kopf.JPG", None),
    ("animal_leopard", "https://upload.wikimedia.org/wikipedia/commons/3/3c/Snow_leopard_%28Panthera_uncia%29.JPG", None),
    ("animal_bear", "https://upload.wikimedia.org/wikipedia/commons/2/2c/Ursus_arctos%2C_Kuterevo_%28HR%29_--_2022_--_0166.jpg", None),
    ("animal_gorilla", "https://upload.wikimedia.org/wikipedia/commons/8/85/Gorilla_portrait_%28181091277%29.jpg", None),
    ("animal_eagle", "https://upload.wikimedia.org/wikipedia/commons/1/1e/Bald_Eagle_Portrait.jpg", None),
]
for item_id, url, crop in animal_fixes:
    time.sleep(0.3)
    img = fetch_image(url)
    if img:
        save_avatar(item_id, compose_env(img, crop))

# 4. Pets Close-Ups (Husky, Bengal, British Shorthair)
pet_fixes = [
    ("pet_husky", "https://upload.wikimedia.org/wikipedia/commons/5/59/Siberian_Husky_Ivan_portrait.jpg", None),
    ("pet_bengal", "https://upload.wikimedia.org/wikipedia/commons/b/ba/Bengal_Cat_Babou_portrait_from_the_right_side_of_his_body.jpg", None),
    ("pet_britishshorthair", "https://upload.wikimedia.org/wikipedia/commons/1/11/British_Shorthair._Female._18_months_old.jpg", None),
]
for item_id, url, crop in pet_fixes:
    time.sleep(0.3)
    img = fetch_image(url)
    if img:
        save_avatar(item_id, compose_env(img, crop))

# 5. Pop Culture (Barbie, Oppenheimer, Wednesday, Wonka)
# Barbie
barbie_url = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Margot_Robbie_Barbie_Premiere_%2853012600685%29.png/1280px-Margot_Robbie_Barbie_Premiere_%2853012600685%29.png"
img_barb = fetch_image(barbie_url)
if img_barb:
    bw, bh = img_barb.size
    # tight face crop on Margot Robbie
    crop_b = img_barb.crop((int(bw * 0.25), int(bh * 0.1), int(bw * 0.75), int(bh * 0.6)))
    save_avatar("pop_barbie", compose_bust(crop_b, target_w=255, top_pad=20))

# Wednesday Addams
wed_url = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Jenna_Ortega_2025.jpg/1280px-Jenna_Ortega_2025.jpg"
img_wed = fetch_image(wed_url)
if img_wed:
    ww, wh = img_wed.size
    crop_w = img_wed.crop((int(ww * 0.2), int(wh * 0.05), int(ww * 0.8), int(wh * 0.65)))
    save_avatar("pop_wednesday", compose_bust(crop_w, target_w=255, top_pad=20))

# Indiana Jones
indy_url = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Indianajones4.jpg/1280px-Indianajones4.jpg"
img_indy = fetch_image(indy_url)
if img_indy:
    iw, ih = img_indy.size
    crop_i = img_indy.crop((int(iw * 0.15), int(ih * 0.05), int(iw * 0.85), int(ih * 0.75)))
    save_avatar("pop_indy", compose_bust(crop_i, target_w=255, top_pad=20))

# 6. Harry Potter Headroom adjustment for Harry, Hermione, Ron
hp_adjust = ["hp_harry", "hp_hermione", "hp_ron"]
for hp_id in hp_adjust:
    p = os.path.join(DRAWABLE_DIR, f"ic_avatar_{hp_id}.png")
    if os.path.exists(p):
        raw = PIL.Image.open(p).convert("RGBA")
        # scale down by 15% and paste with 22px top pad
        rw, rh = raw.size
        nw, nh = int(rw * 0.85), int(rh * 0.85)
        scaled = raw.resize((nw, nh), PIL.Image.LANCZOS)
        bg = PIL.Image.new("RGBA", (320, 320), (22, 25, 33, 255))
        bg.paste(scaled, ((320 - nw) // 2, 22), mask=scaled if scaled.mode == 'RGBA' else None)
        bg.save(p, format="PNG", optimize=True)
        print(f"Adjusted headroom for {hp_id}")

print("Visual fixes applied.")
