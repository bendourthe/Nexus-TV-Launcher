import os
import io
import time
import urllib.request
import urllib.parse
import json
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
USER_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\.user_uploaded"
BUILD_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build")
TEST_DIR = os.path.join(BUILD_DIR, "test_renders")
os.makedirs(DRAWABLE_DIR, exist_ok=True)
os.makedirs(BUILD_DIR, exist_ok=True)
os.makedirs(TEST_DIR, exist_ok=True)

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

def fetch_img(url, retries=3):
    if not url:
        return None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=15) as resp:
                return PIL.Image.open(io.BytesIO(resp.read()))
        except Exception as e:
            time.sleep(1)
            if attempt == retries - 1:
                print(f"Failed to fetch {url[:70]}...: {e}")
                return None

def compose_character_bust(img, canvas_bg=(20, 23, 31, 255), target_w=255, top_pad=22, crop_box=None):
    if crop_box:
        img = img.crop(crop_box)
    img = img.convert("RGBA")
    w, h = img.size
    scale = target_w / w
    target_h = int(h * scale)
    scaled = img.resize((target_w, target_h), PIL.Image.LANCZOS)
    
    canvas = PIL.Image.new("RGBA", (320, 320), canvas_bg)
    paste_x = (320 - target_w) // 2
    paste_y = top_pad
    canvas.paste(scaled, (paste_x, paste_y), mask=scaled if scaled.mode == 'RGBA' else None)
    return canvas

def compose_environment_photo(img, crop_box=None):
    if crop_box:
        img = img.crop(crop_box)
    img = img.convert("RGBA")
    w, h = img.size
    min_dim = min(w, h)
    cx = (w - min_dim) // 2
    cy = (h - min_dim) // 2
    sq = img.crop((cx, cy, cx + min_dim, cy + min_dim))
    sq = sq.resize((320, 320), PIL.Image.LANCZOS)
    return sq

print("Processing Landmarks and Cities...")

LANDMARKS = {
    "landmark_eiffel": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg/1280px-Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg", None),
    "landmark_tajmahal": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/Taj_Mahal_%28Edited%29.jpeg/1280px-Taj_Mahal_%28Edited%29.jpeg", None),
    "landmark_pyramids": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/Great_Pyramid_of_Giza_-_Pyramid_of_Khufu.jpg/1280px-Great_Pyramid_of_Giza_-_Pyramid_of_Khufu.jpg", None),
    "landmark_cntower": ("https://upload.wikimedia.org/wikipedia/commons/a/a6/CN_Tower%2C_Toronto%2C_Ontario_%2829969151776%29.jpg", None),
    "landmark_liberty": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Front_view_of_Statue_of_Liberty_%28cropped%29.jpg/1280px-Front_view_of_Statue_of_Liberty_%28cropped%29.jpg", None),
    "landmark_colosseum": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Colosseo_2020.jpg/1280px-Colosseo_2020.jpg", None),
    "landmark_bigben": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/0/05/Elizabeth_Tower_and_the_north_front_of_the_Palace_of_Westminster%2C_London.jpg/1280px-Elizabeth_Tower_and_the_north_front_of_the_Palace_of_Westminster%2C_London.jpg", None),
    "landmark_goldengate": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/Golden_Gate_Bridge_as_seen_from_Battery_East.jpg/1280px-Golden_Gate_Bridge_as_seen_from_Battery_East.jpg", None),
    "landmark_operahouse": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/Sydney_Opera_House_2026.jpg/1280px-Sydney_Opera_House_2026.jpg", None),
    "landmark_machupicchu": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/Machu_Picchu%2C_2023_%28012%29.jpg/1280px-Machu_Picchu%2C_2023_%28012%29.jpg", None),
}

for item_id, (url, crop) in LANDMARKS.items():
    img = fetch_img(url)
    if img:
        composed = compose_environment_photo(img, crop)
        out_path = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
        composed.save(out_path, format="PNG", optimize=True)
        print(f"Saved {item_id}")

CITIES = {
    "city_nyc": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7a/View_of_Empire_State_Building_from_Rockefeller_Center_New_York_City_dllu_%28cropped%29.jpg/1280px-View_of_Empire_State_Building_from_Rockefeller_Center_New_York_City_dllu_%28cropped%29.jpg", None),
    "city_tokyo": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Skyscrapers_of_Shinjuku_2009_January.jpg/1280px-Skyscrapers_of_Shinjuku_2009_January.jpg", None),
    "city_paris": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/La_Tour_Eiffel_vue_de_la_Tour_Saint-Jacques%2C_Paris_ao%C3%BBt_2014_%282%29.jpg/1280px-La_Tour_Eiffel_vue_de_la_Tour_Saint-Jacques%2C_Paris_ao%C3%BBt_2014_%282%29.jpg", None),
    "city_london": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/London_Skyline_%28125508655%29.jpeg/1280px-London_Skyline_%28125508655%29.jpeg", None),
    "city_dubai": ("https://thumb.wikimedia.org/wikipedia/en/thumb/c/c7/Burj_Khalifa_2021.jpg/1280px-Burj_Khalifa_2021.jpg", None),
    "city_rome": ("https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Rome_skyline_and_Sant%27Ivo.jpg/1280px-Rome_skyline_and_Sant%27Ivo.jpg", None),
    "city_hongkong": ("https://upload.wikimedia.org/wikipedia/commons/2/23/Hong_Kong_Skyline_Restitch_-_Dec_2007.jpg", None),
    "city_sanfrancisco": ("https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/San_Francisco_skyline_of_April_2017_seen_from_Treasure_Island.jpg/1280px-San_Francisco_skyline_of_April_2017_seen_from_Treasure_Island.jpg", None),
    "city_singapore": ("https://upload.wikimedia.org/wikipedia/commons/3/37/Singapore_Skyline_Panorama.jpg", None),
    "city_chicago": ("https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Chicago_River_ferry_b.jpg/1280px-Chicago_River_ferry_b.jpg", None),
}

for item_id, (url, crop) in CITIES.items():
    img = fetch_img(url)
    if img:
        composed = compose_environment_photo(img, crop)
        out_path = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
        composed.save(out_path, format="PNG", optimize=True)
        print(f"Saved {item_id}")

print("Landmarks and Cities complete.")
