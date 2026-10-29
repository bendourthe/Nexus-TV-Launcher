import os
import io
import math
import json
import urllib.request
import urllib.parse
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

def get_wiki_summary_image(title):
    url = f"https://en.wikipedia.org/api/rest_v1/page/summary/{urllib.parse.quote(title)}"
    req = urllib.request.Request(url, headers={"User-Agent": "CleanLauncher/1.0 (bdour@cleanlauncher.internal)"})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        data = json.loads(resp.read())
        thumb = data.get("thumbnail", {}).get("source")
        if thumb:
            return thumb, "https://en.wikipedia.org/"
    except Exception as e:
        print(f"  [Wiki Summary Error {title}]: {e}")
    return None, None

def get_fandom_image(wiki, title):
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&format=json&pithumbsize=600"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0", "Referer": f"https://{wiki}.fandom.com/"})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        if pages:
            page = list(pages.values())[0]
            thumb = page.get("thumbnail", {}).get("source")
            if thumb:
                return thumb, f"https://{wiki}.fandom.com/"
    except Exception as e:
        print(f"  [Fandom Error {wiki}/{title}]: {e}")
    return None, None

def save_cropped(img_bytes, filename, crop_mode="face"):
    try:
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
        elif crop_mode == "car":
            if w > h:
                crop_size = int(h * 0.92)
                left = (w - crop_size) // 2
                top = int(h * 0.04)
            else:
                crop_size = int(w * 0.92)
                left = (w - crop_size) // 2
                top = (h - crop_size) // 2
        else: # center
            crop_size = min(w, h)
            left = (w - crop_size) // 2
            top = (h - crop_size) // 2

        cropped = img.crop((left, top, left + crop_size, top + crop_size))
        final = cropped.resize((320, 320), PIL.Image.LANCZOS)
        out_path = os.path.join(DRAWABLE_DIR, filename)
        final.save(out_path, format="PNG", optimize=True)
        print(f"SUCCESS: {filename}")
        return True
    except Exception as e:
        print(f"ERROR cropping {filename}: {e}")
        return False

# Procedural high-end 3D abstract creators
def generate_synthwave():
    img = PIL.Image.new("RGB", (320, 320), (12, 10, 24))
    draw = PIL.ImageDraw.Draw(img)
    # Sun
    sun_cx, sun_cy, sun_r = 160, 140, 65
    for r in range(sun_r, 0, -1):
        ratio = r / sun_r
        color = (int(255 * (1 - ratio*0.2)), int(80 + 140 * (1 - ratio)), int(40 * ratio))
        draw.ellipse([sun_cx - r, sun_cy - r, sun_cx + r, sun_cy + r], fill=color)
    # Horizon lines across sun
    for y in range(130, 200, 10):
        h = max(2, (y - 120) // 8)
        draw.rectangle([sun_cx - sun_r - 10, y, sun_cx + sun_r + 10, y + h], fill=(12, 10, 24))
    # Glowing neon grid floor
    vp_x, vp_y = 160, 150
    for i in range(-12, 13):
        bot_x = 160 + i * 28
        draw.line([(vp_x, vp_y), (bot_x, 320)], fill=(0, 230, 255), width=2)
    for y in [165, 175, 190, 210, 238, 274, 318]:
        draw.line([(0, y), (320, y)], fill=(255, 0, 160), width=2)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_synthwave.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_synthwave.png (procedural)")

def generate_chrome_sphere():
    img = PIL.Image.new("RGB", (320, 320), (10, 12, 16))
    cx, cy, r = 160, 160, 120
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            dx = (x - cx) / r
            dy = (y - cy) / r
            d2 = dx*dx + dy*dy
            if d2 <= 1.0:
                dz = math.sqrt(1.0 - d2)
                # reflect vector
                ref_y = dy * 0.8
                ref_x = dx * 0.8
                # Studio lighting simulation
                horiz = math.sin(ref_y * 4.0) * 0.5 + 0.5
                spec1 = max(0.0, -dx * 0.5 - dy * 0.5 + dz * 0.7) ** 16
                spec2 = max(0.0, dx * 0.6 + dy * 0.3 + dz * 0.6) ** 8
                rim = (1.0 - dz) ** 3
                
                red = int(min(255, 30 + horiz * 150 + spec1 * 255 + spec2 * 80 + rim * 120))
                green = int(min(255, 40 + horiz * 170 + spec1 * 255 + spec2 * 110 + rim * 160))
                blue = int(min(255, 60 + horiz * 200 + spec1 * 255 + spec2 * 150 + rim * 220))
                pixels[x, y] = (red, green, blue)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_chromesphere.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_chromesphere.png (procedural)")

def generate_liquid_gold():
    img = PIL.Image.new("RGB", (320, 320), (8, 9, 12))
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            nx = x / 320.0
            ny = y / 320.0
            wave1 = math.sin(nx * 8.0 + math.cos(ny * 6.0) * 3.0)
            wave2 = math.cos(ny * 7.0 + math.sin(nx * 5.0) * 2.5)
            pattern = (wave1 + wave2) * 0.5
            if pattern > 0.15:
                depth = min(1.0, (pattern - 0.15) * 2.5)
                # Rich molten gold gradient
                r = int(min(255, 200 * depth + 55))
                g = int(min(255, 150 * depth + 25))
                b = int(min(255, 20 * depth + 5))
                pixels[x, y] = (r, g, b)
            else:
                # Black marble vein
                v = int(max(0, 15 + pattern * 30))
                pixels[x, y] = (v, v + 2, v + 4)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_goldblack.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_goldblack.png (procedural)")

def generate_iridescent():
    img = PIL.Image.new("RGB", (320, 320), (10, 10, 14))
    cx, cy, r = 160, 160, 125
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            dx = (x - cx) / r
            dy = (y - cy) / r
            d2 = dx*dx + dy*dy
            if d2 <= 1.0:
                dz = math.sqrt(1.0 - d2)
                fresnel = 1.0 - dz
                phase = fresnel * 6.0 + dy * 2.0
                r_col = math.sin(phase) * 0.5 + 0.5
                g_col = math.sin(phase + 2.094) * 0.5 + 0.5
                b_col = math.sin(phase + 4.188) * 0.5 + 0.5
                spec = max(0.0, -dx*0.6 - dy*0.6 + dz*0.5) ** 24
                
                red = int(min(255, r_col * 200 * fresnel + spec * 255 + 20))
                green = int(min(255, g_col * 220 * fresnel + spec * 255 + 20))
                blue = int(min(255, b_col * 255 * fresnel + spec * 255 + 40))
                pixels[x, y] = (red, green, blue)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_iridescent.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_iridescent.png (procedural)")

def generate_quantum():
    img = PIL.Image.new("RGB", (320, 320), (8, 6, 16))
    cx, cy = 160, 160
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            angle = math.atan2(dy, dx)
            spiral = math.sin(angle * 3.0 - dist * 0.08) * 0.5 + 0.5
            glow = max(0.0, 1.0 - dist / 150.0)
            
            r = int(min(255, (spiral * 180 + 50) * glow + (120 if dist < 25 else 0)))
            g = int(min(255, (spiral * 40 + 20) * glow + (220 if dist < 25 else 0)))
            b = int(min(255, (spiral * 255 + 80) * glow + (255 if dist < 25 else 0)))
            pixels[x, y] = (r, g, b)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_quantum.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_quantum.png (procedural)")

def generate_cyberpunk():
    img = PIL.Image.new("RGB", (320, 320), (6, 8, 14))
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            nx = x / 320.0
            ny = y / 320.0
            w1 = math.sin(nx * 12.0 + ny * 6.0)
            w2 = math.cos(ny * 14.0 - nx * 8.0)
            strand = abs(w1 + w2)
            if strand < 0.25:
                intensity = 1.0 - (strand / 0.25)
                r = int(min(255, 255 * intensity * ny))
                g = int(min(255, 230 * intensity * (1.0 - ny)))
                b = int(min(255, 255 * intensity))
                pixels[x, y] = (r, g, b)
            else:
                pixels[x, y] = (8, 10, 18)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_cyberpunk.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_cyberpunk.png (procedural)")

def generate_silk():
    img = PIL.Image.new("RGB", (320, 320), (12, 10, 16))
    pixels = img.load()
    for y in range(320):
        for x in range(320):
            nx = x / 320.0
            ny = y / 320.0
            curve = math.sin(nx * 5.0 + math.cos(ny * 4.0) * 2.0)
            dist_to_center = abs(ny - 0.5 + curve * 0.25)
            if dist_to_center < 0.18:
                sheen = math.sin(nx * 10.0 + ny * 8.0) * 0.5 + 0.5
                shade = 1.0 - (dist_to_center / 0.18)
                r = int(min(255, (160 + sheen * 95) * shade))
                g = int(min(255, (40 + sheen * 60) * shade))
                b = int(min(255, (120 + sheen * 135) * shade))
                pixels[x, y] = (r, g, b)
            else:
                pixels[x, y] = (12, 10, 18)
    img.save(os.path.join(DRAWABLE_DIR, "ic_avatar_abstract_silk.png"), format="PNG")
    print("SUCCESS: ic_avatar_abstract_silk.png (procedural)")

ITEMS_TO_UPDATE = [
    # Landscapes:
    ("ic_avatar_land_aurora.png", "wiki", "Aurora", "center"),
    ("ic_avatar_land_amalfi.png", "wiki", "Amalfi_Coast", "center"),
    ("ic_avatar_land_bali.png", "wiki", "Ubud", "center"),

    # Animals:
    ("ic_avatar_animal_bear.png", "wiki", "Polar_bear", "face"),
    ("ic_avatar_animal_fox.png", "wiki", "Red_fox", "face"),
    ("ic_avatar_animal_eagle.png", "wiki", "Bald_eagle", "face"),
    ("ic_avatar_animal_owl.png", "wiki", "Barn_owl", "face"),
    ("ic_avatar_animal_panda.png", "wiki", "Giant_panda", "face"),
    ("ic_avatar_animal_gorilla.png", "wiki", "Western_gorilla", "face"),

    # Pets:
    ("ic_avatar_pet_retriever.png", "wiki", "Golden_Retriever", "face"),
    ("ic_avatar_pet_frenchie.png", "wiki", "French_Bulldog", "face"),
    ("ic_avatar_pet_husky.png", "wiki", "Siberian_Husky", "face"),
    ("ic_avatar_pet_germanshepherd.png", "wiki", "German_Shepherd", "face"),
    ("ic_avatar_pet_corgi.png", "wiki", "Pembroke_Welsh_Corgi", "face"),
    ("ic_avatar_pet_mainecoon.png", "wiki", "Maine_Coon", "face"),
    ("ic_avatar_pet_britishshorthair.png", "wiki", "British_Shorthair", "face"),
    ("ic_avatar_pet_bengal.png", "wiki", "Bengal_cat", "face"),
    ("ic_avatar_pet_hamster.png", "wiki", "Golden_hamster", "face"),
    ("ic_avatar_pet_cockatiel.png", "wiki", "Cockatiel", "face"),

    # Music Artists:
    ("ic_avatar_music_daftpunk.png", "wiki", "Daft_Punk", "face"),
    ("ic_avatar_music_weeknd.png", "wiki", "The_Weeknd", "face"),
    ("ic_avatar_music_taylorswift.png", "wiki", "Taylor_Swift", "face"),
    ("ic_avatar_music_billie.png", "wiki", "Billie_Eilish", "face"),
    ("ic_avatar_music_eminem.png", "wiki", "Eminem", "face"),
    ("ic_avatar_music_freddie.png", "wiki", "Freddie_Mercury", "face"),
    ("ic_avatar_music_bowie.png", "wiki", "David_Bowie", "face"),
    ("ic_avatar_music_jimi.png", "wiki", "Jimi_Hendrix", "face"),
    ("ic_avatar_music_michael.png", "wiki", "Michael_Jackson", "face"),
    ("ic_avatar_music_drake.png", "wiki", "Drake_(musician)", "face"),

    # Star Wars:
    ("ic_avatar_sw_vader.png", "fandom_starwars", "Darth_Vader's_armor", "face"),
    ("ic_avatar_sw_bobafett.png", "fandom_starwars", "Boba_Fett", "face"),
    ("ic_avatar_sw_obiwan.png", "fandom_starwars", "Obi-Wan_Kenobi", "face"),

    # Abstract (real minerals):
    ("ic_avatar_abstract_amethyst.png", "wiki", "Amethyst", "center"),
    ("ic_avatar_abstract_kintsugi.png", "wiki", "Kintsugi", "center"),
    ("ic_avatar_abstract_prism.png", "wiki", "Dispersive_prism", "center"),
]

def main():
    print("=== FETCHING REAL PORTRAITS ===")
    for filename, kind, target, crop_mode in ITEMS_TO_UPDATE:
        print(f"Fetching {filename} via {kind}:{target}...")
        url = None
        ref = None
        if kind == "wiki":
            url, ref = get_wiki_summary_image(target)
        elif kind.startswith("fandom_"):
            wiki = kind.split("_")[1]
            url, ref = get_fandom_image(wiki, target)

        if not url:
            print(f"  FAILED URL for {filename}")
            continue

        headers = {"User-Agent": "Mozilla/5.0"}
        if ref:
            headers["Referer"] = ref
        try:
            raw = urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=12).read()
            save_cropped(raw, filename, crop_mode)
        except Exception as e:
            print(f"  FAILED download for {filename}: {e}")

    print("\n=== GENERATING PROCEDURAL 3D ABSTRACTS ===")
    generate_synthwave()
    generate_chrome_sphere()
    generate_liquid_gold()
    generate_iridescent()
    generate_quantum()
    generate_cyberpunk()
    generate_silk()
    print("ALL DONE!")

if __name__ == "__main__":
    main()
