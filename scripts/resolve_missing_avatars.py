import urllib.request
import urllib.parse
import json
import io
import os
import re
import sys
import time
import PIL.Image

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def get_fandom_thumb(wiki, page_title):
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(page_title)}&prop=pageimages&format=json&pithumbsize=600"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0", "Referer": f"https://{wiki}.fandom.com/"})
    try:
        resp = urllib.request.urlopen(req, timeout=8)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        if pages:
            page = list(pages.values())[0]
            thumb = page.get("thumbnail", {}).get("source")
            if thumb:
                return thumb, f"https://{wiki}.fandom.com/"
    except Exception as e:
        print(f"    [Fandom Error {wiki}/{page_title}]: {e}")
    return None, None

def get_bing_image(query):
    try:
        url = "https://www.bing.com/images/search?q=" + urllib.parse.quote(query) + "&first=1"
        req = urllib.request.Request(url, headers=HEADERS)
        html = urllib.request.urlopen(req, timeout=6).read().decode("utf-8", errors="ignore")
        matches = re.findall(r'murl&quot;:&quot;(http[^&]+)&quot;', html)
        return matches[:8] # return top 8 candidate URLs
    except Exception as e:
        print(f"    [Bing Error {query}]: {e}")
    return []

def download_bytes(url, referer=None):
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        return resp.read()
    except Exception as e:
        return None

def crop_and_save(img_bytes, filename, crop_mode="face"):
    try:
        img = PIL.Image.open(io.BytesIO(img_bytes)).convert("RGBA")
        w, h = img.size
        
        if crop_mode == "face":
            if h > w:
                crop_size = w
                left = 0
                top = int(0.06 * (h - crop_size))
            else:
                crop_size = h
                left = (w - crop_size) // 2
                top = 0
        elif crop_mode == "car":
            if w > h:
                crop_size = int(h * 0.90)
                left = (w - crop_size) // 2
                top = int(h * 0.08)
            else:
                crop_size = int(w * 0.90)
                left = (w - crop_size) // 2
                top = (h - crop_size) // 2
        else: # center
            crop_size = min(w, h)
            left = (w - crop_size) // 2
            top = (h - crop_size) // 2

        cropped = img.crop((left, top, left + crop_size, top + crop_size))
        final_img = cropped.resize((320, 320), PIL.Image.LANCZOS)
        out_path = os.path.join(OUT_DIR, filename)
        final_img.save(out_path, format="PNG", optimize=True)
        return True
    except Exception as e:
        print(f"    [Crop error {filename}]: {e}")
        return False

# Import items from main script
from fetch_all_realistic_avatars import AVATARS

def main():
    missing = []
    for item in AVATARS:
        path = os.path.join(OUT_DIR, item["id"])
        if not os.path.exists(path) or os.path.getsize(path) < 1000:
            missing.append(item)
            
    print(f"Total missing/failed avatars to repair: {len(missing)} / {len(AVATARS)}")
    
    fixed = 0
    for idx, item in enumerate(missing, 1):
        filename = item["id"]
        t = item["type"]
        crop = item.get("crop", "face")
        print(f"[{idx}/{len(missing)}] Repairing {filename}...")
        
        data = None
        if t == "fandom":
            thumb_url, ref = get_fandom_thumb(item["wiki"], item["title"])
            if thumb_url:
                data = download_bytes(thumb_url, referer=ref)
            if not data:
                print(f"    Fandom failed, trying Bing candidates...")
                candidates = get_bing_image(f"{item['title']} movie face portrait close up")
                for c in candidates:
                    data = download_bytes(c)
                    if data: break
        elif t in ("commons", "bing"):
            query = item.get("query", filename)
            candidates = get_bing_image(query)
            for c in candidates:
                data = download_bytes(c)
                if data: break
                
        if data and crop_and_save(data, filename, crop):
            print(f"    SUCCESS: Saved {filename}")
            fixed += 1
        else:
            print(f"    FAILED: Could not download {filename}")
            
        time.sleep(0.1)
        
    print(f"\n=== REPAIR COMPLETE: Fixed {fixed} / {len(missing)} ===")

if __name__ == "__main__":
    main()
