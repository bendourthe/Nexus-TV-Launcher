import os
import io
import time
import urllib.request
import urllib.parse
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
USER_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\.user_uploaded"

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

def create_circular_preview(img_320):
    mask = PIL.Image.new("L", (320, 320), 0)
    draw = PIL.ImageDraw.Draw(mask)
    draw.ellipse((0, 0, 320, 320), fill=255)
    preview = PIL.Image.new("RGBA", (320, 320), (0, 0, 0, 0))
    preview.paste(img_320, (0, 0), mask=mask)
    # Add subtle white rim
    draw_p = PIL.ImageDraw.Draw(preview)
    draw_p.ellipse((2, 2, 318, 318), outline=(255, 255, 255, 70), width=4)
    return preview

# 1. Process Kratos from user's uploaded image
kratos_path = os.path.join(USER_DIR, "media_1792982658615.png")
kratos_raw = PIL.Image.open(kratos_path).convert("RGBA")
kw, kh = kratos_raw.size
print(f"Kratos raw: {kw}x{kh}")

# Scale Kratos down slightly so head has comfortable breathing room and fits within the circular avatar
target_w = 264
scale = target_w / kw
target_h = int(kh * scale)
kratos_scaled = kratos_raw.resize((target_w, target_h), PIL.Image.LANCZOS)

# Create 320x320 background with dark studio vignette
kratos_canvas = PIL.Image.new("RGBA", (320, 320), (22, 25, 33, 255))
# Paste Kratos with top offset = 22px (centered horizontally)
paste_x = (320 - target_w) // 2
paste_y = 22
kratos_canvas.paste(kratos_scaled, (paste_x, paste_y), mask=kratos_scaled)

kratos_out = os.path.join(DRAWABLE_DIR, "ic_avatar_game_kratos.png")
kratos_canvas.save(kratos_out, format="PNG", optimize=True)
print("Saved ic_avatar_game_kratos.png")

# 2. Process Ichigo from user's uploaded image
ichigo_path = os.path.join(USER_DIR, "media_1792982736063.png")
ichigo_raw = PIL.Image.open(ichigo_path).convert("RGBA")
iw, ih = ichigo_raw.size
print(f"Ichigo raw: {iw}x{ih}")

# Center on Ichigo's head and bust
# Head starts near top (y=10), face is centered around x=150, y=170
crop_size = min(iw, ih) # 300
crop_x = (iw - crop_size) // 2 # 0
crop_y = 10 # capture full hair down to shinigami collar
ichigo_cropped = ichigo_raw.crop((crop_x, crop_y, crop_x + crop_size, crop_y + crop_size))
# Pad top slightly by 15px so circular crop doesn't touch hair tips
ichigo_canvas = PIL.Image.new("RGBA", (320, 320), (235, 225, 175, 255))
ichigo_scaled = ichigo_cropped.resize((290, 290), PIL.Image.LANCZOS)
ichigo_canvas.paste(ichigo_scaled, (15, 15))

ichigo_out = os.path.join(DRAWABLE_DIR, "ic_avatar_anime_ichigo.png")
ichigo_canvas.save(ichigo_out, format="PNG", optimize=True)
print("Saved ic_avatar_anime_ichigo.png")

# 3. Head & Bust portraits for other characters
CHARACTER_UPDATES = [
    # Video Games
    ("ic_avatar_game_chief.png", "https://static.wikia.nocookie.net/halo/images/6/69/MP-Master-Chief-Textless-S2.jpg/revision/latest", 0.08),
    ("ic_avatar_game_cloud.png", "https://static.wikia.nocookie.net/finalfantasy/images/5/5b/Cloud_Strife_from_FFVII_Remake_promo_render.png/revision/latest", 0.05),
    ("ic_avatar_game_link.png", "https://static.wikia.nocookie.net/zelda_gamepedia_en/images/4/47/TLoZ_Series_Link_Render.png/revision/latest", 0.05),
    ("ic_avatar_game_jinsakai.png", "https://static.wikia.nocookie.net/ghostoftsushima/images/9/94/Jin_Sakai_key_art_cropped.png/revision/latest", 0.08),
    # Anime
    ("ic_avatar_anime_goku.png", "https://static.wikia.nocookie.net/dragonball/images/b/ba/Goku_anime_profile.png/revision/latest", 0.05),
    ("ic_avatar_anime_tanjiro.png", "https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/0/05/Tanjiro_anime_right_face.png/revision/latest", 0.06),
    ("ic_avatar_anime_nezuko.png", "https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/0/0e/Nezuko_anime_right_face.png/revision/latest", 0.06),
    ("ic_avatar_anime_saitama.png", "https://upload.wikimedia.org/wikipedia/en/9/98/SaitamaWikipediapage.png", 0.04),
    # Marvel & DC
    ("ic_avatar_marvel_captain.png", "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/1/1b/Captain_America_-_Age_of_Ultron_Infobox.png/revision/latest", 0.06),
    ("ic_avatar_marvel_loki.png", "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/2/28/Loki_-_Credits_%28Logo%29.png/revision/latest", 0.05),
    ("ic_avatar_dc_joker.png", "https://static.wikia.nocookie.net/batman/images/f/f9/Heath_Ledger_as_the_Joker.JPG/revision/latest", 0.05),
    # Star Wars
    ("ic_avatar_sw_bobafett.png", "https://static.wikia.nocookie.net/starwars/images/5/5e/BobaFettMain2.png/revision/latest", 0.06),
    ("ic_avatar_sw_luke.png", "https://upload.wikimedia.org/wikipedia/commons/6/67/Luke_Skywalker_-_Welcome_Banner_%28Cropped%29.jpg", 0.05),
    # Pets (focused headshots)
    ("ic_avatar_pet_husky.png", "https://upload.wikimedia.org/wikipedia/commons/8/8b/Husky_L.jpg", 0.05),
    ("ic_avatar_pet_germanshepherd.png", "https://upload.wikimedia.org/wikipedia/commons/d/d0/German_Shepherd_-_DSC_0346_%2810096362833%29.jpg", 0.05),
    ("ic_avatar_pet_britishshorthair.png", "https://upload.wikimedia.org/wikipedia/commons/2/2d/Mystica_from_British_Empire_Cattery.jpg", 0.08),
    ("ic_avatar_pet_hamster.png", "https://upload.wikimedia.org/wikipedia/commons/6/6b/Golden_hamster_front_1.jpg", 0.08),
]

for filename, url, top_ratio in CHARACTER_UPDATES:
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        raw = urllib.request.urlopen(req, timeout=12).read()
        img = PIL.Image.open(io.BytesIO(raw)).convert("RGBA")
        w, h = img.size
        crop_size = min(w, h)
        left = (w - crop_size) // 2
        top = int(top_ratio * (h - crop_size)) if h > w else 0
        cropped = img.crop((left, top, left + crop_size, top + crop_size))
        final = cropped.resize((320, 320), PIL.Image.LANCZOS)
        out_path = os.path.join(DRAWABLE_DIR, filename)
        final.save(out_path, format="PNG", optimize=True)
        print(f"Updated head/bust {filename} ({w}x{h} -> 320x320)")
        time.sleep(0.3)
    except Exception as e:
        print(f"Error updating {filename}: {e}")
