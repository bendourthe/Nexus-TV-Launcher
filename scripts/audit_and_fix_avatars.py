import os
import io
import urllib.request
import urllib.parse
import json
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

def fetch_commons_image(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrnamespace=6&gsrlimit=1&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "CleanLauncher/1.0 (audit@example.com)"})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        if pages:
            page = list(pages.values())[0]
            infos = page.get("imageinfo", [])
            if infos:
                return infos[0].get("thumburl") or infos[0].get("url")
    except Exception as e:
        print(f"Error fetching {query}: {e}")
    return None

def crop_and_save(img_bytes, filename, crop_mode="face"):
    img = PIL.Image.open(io.BytesIO(img_bytes)).convert("RGBA")
    w, h = img.size
    if crop_mode == "face":
        if h > w:
            crop_size = w
            left = 0
            top = int(0.08 * (h - crop_size))
        else:
            crop_size = h
            left = (w - crop_size) // 2
            top = 0
    else:
        crop_size = min(w, h)
        left = (w - crop_size) // 2
        top = (h - crop_size) // 2

    cropped = img.crop((left, top, left + crop_size, top + crop_size))
    final = cropped.resize((320, 320), PIL.Image.LANCZOS)
    out_path = os.path.join(DRAWABLE_DIR, filename)
    final.save(out_path, format="PNG", optimize=True)
    print(f"Saved {filename} ({w}x{h} -> 320x320)")

# Specifically fix Lion:
lion_url = fetch_commons_image("Lion male portrait Serengeti")
if lion_url:
    print("Lion URL:", lion_url)
    req = urllib.request.Request(lion_url, headers={"User-Agent": "Mozilla/5.0"})
    raw = urllib.request.urlopen(req).read()
    crop_and_save(raw, "ic_avatar_animal_lion.png", "face")
