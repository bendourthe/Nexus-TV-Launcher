import os
import io
import time
import urllib.request
import urllib.parse
import json
import PIL.Image
import PIL.ImageDraw
import PIL.ImageFilter

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
USER_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\.user_uploaded"
BUILD_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build")
os.makedirs(DRAWABLE_DIR, exist_ok=True)
os.makedirs(BUILD_DIR, exist_ok=True)

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

def fetch_image_from_url(url, retries=3):
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = resp.read()
                return PIL.Image.open(io.BytesIO(data))
        except Exception as e:
            time.sleep(1)
            if attempt == retries - 1:
                print(f"Failed to fetch {url}: {e}")
                return None

def compose_head_and_bust(img, target_w=255, top_pad=22, canvas_bg=(20, 23, 31, 255), crop_box=None):
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
    w, h = img.size
    min_dim = min(w, h)
    cx = (w - min_dim) // 2
    cy = (h - min_dim) // 2
    sq = img.crop((cx, cy, cx + min_dim, cy + min_dim))
    sq = sq.resize((320, 320), PIL.Image.LANCZOS).convert("RGBA")
    return sq

print("Helper functions initialized.")
