import os
import io
import json
import urllib.request
import urllib.parse
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

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
        with urllib.request.urlopen(req, timeout=10) as resp:
            return PIL.Image.open(io.BytesIO(resp.read())).convert("RGBA")
    except Exception as e:
        print(f"Error fetching {url[:60]}: {e}")
        return None

def fetch_wiki_thumb(api_base, title):
    headers = dict(HEADERS)
    headers["Referer"] = api_base.split("/api.php")[0] + "/"
    url = f"{api_base}?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&pithumbsize=1000&format=json"
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=8) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                if "thumbnail" in p:
                    return fetch_image(p["thumbnail"]["source"], referer=headers["Referer"])
    except Exception as e:
        print(f"Failed wiki thumb for {title}: {e}")
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
    canvas.paste(scaled, (paste_x, top_pad), mask=scaled if scaled.mode == 'RGBA' else None)
    return canvas

def save_avatar(item_id, img):
    out = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
    img.save(out, format="PNG", optimize=True)
    print(f"Saved ic_avatar_{item_id}.png")

# 1. Peacemaker in polished chrome helmet
img_peace = fetch_wiki_thumb("https://dcextendeduniverse.fandom.com/api.php", "Peacemaker") or \
            fetch_wiki_thumb("https://dc.fandom.com/api.php", "Christopher Smith (DC Extended Universe)")
if img_peace:
    pw, ph = img_peace.size
    crop = img_peace.crop((int(pw * 0.1), int(ph * 0.02), int(pw * 0.9), int(ph * 0.65)))
    save_avatar("dc_peacemaker", compose_bust(crop, target_w=255, top_pad=20))

# 2. Cooper in Interstellar white astronaut suit
img_coop = fetch_wiki_thumb("https://interstellarfilm.fandom.com/api.php", "Joseph Cooper")
if img_coop:
    cw, ch = img_coop.size
    crop = img_coop.crop((int(cw * 0.05), int(ch * 0.02), int(cw * 0.95), int(ch * 0.70)))
    save_avatar("scifi_cooper", compose_bust(crop, target_w=255, top_pad=20))

# 3. Link in Champion's Tunic
img_link = fetch_wiki_thumb("https://zelda.fandom.com/api.php", "Link")
if img_link:
    lw, lh = img_link.size
    crop = img_link.crop((int(lw * 0.15), int(lh * 0.02), int(lw * 0.85), int(lh * 0.55)))
    save_avatar("game_link", compose_bust(crop, target_w=255, top_pad=20))

# 4. Willy Wonka - Gene Wilder
img_wonka = fetch_wiki_thumb("https://roalddahl.fandom.com/api.php", "Willy Wonka")
if img_wonka:
    kw, kh = img_wonka.size
    crop = img_wonka.crop((int(kw * 0.05), int(kh * 0.02), int(kw * 0.95), int(kh * 0.75)))
    save_avatar("pop_wonka", compose_bust(crop, target_w=255, top_pad=20))

print("Completed.")
