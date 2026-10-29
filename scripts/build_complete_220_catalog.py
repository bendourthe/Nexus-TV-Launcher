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
os.makedirs(DRAWABLE_DIR, exist_ok=True)
os.makedirs(BUILD_DIR, exist_ok=True)

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
}

def fetch_wiki_image(api_base, title, referer=None):
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    else:
        headers["Referer"] = api_base.split("/api.php")[0] + "/"
    
    url = f"{api_base}?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&pithumbsize=1000&format=json"
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                if "thumbnail" in p:
                    thumb_url = p["thumbnail"]["source"]
                    img_req = urllib.request.Request(thumb_url, headers=headers)
                    with urllib.request.urlopen(img_req, timeout=12) as ir:
                        return PIL.Image.open(io.BytesIO(ir.read())).convert("RGBA")
    except Exception as e:
        print(f"Failed wiki fetch [{title}]: {e}")
    return None

def fetch_direct_img(url, referer=None):
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=12) as r:
            return PIL.Image.open(io.BytesIO(r.read())).convert("RGBA")
    except Exception as e:
        print(f"Failed direct fetch [{url[:60]}...]: {e}")
    return None

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

print("Setup ready.")
