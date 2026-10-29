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
os.makedirs(DRAWABLE_DIR, exist_ok=True)
os.makedirs(BUILD_DIR, exist_ok=True)
os.makedirs(TEST_DIR, exist_ok=True)

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0"
}

def fetch_img(url, retries=3):
    if not url:
        return None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = resp.read()
                return PIL.Image.open(io.BytesIO(data))
        except Exception as e:
            time.sleep(1)
            if attempt == retries - 1:
                print(f"Failed to fetch {url[:75]}...: {e}")
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

print("Starting avatar upgrade downloads...")

# 1. Landmark CN Tower - exterior view
# Sourced from high-res Wikimedia photo of CN Tower against blue sky
cntower_url = "https://upload.wikimedia.org/wikipedia/commons/a/a6/CN_Tower%2C_Toronto%2C_Ontario_%2829969151776%29.jpg"
img_cntower = fetch_img(cntower_url)
if img_cntower:
    # Crop to center tower needle
    cw, ch = img_cntower.size
    # Center on top portion of tower
    crop_w = int(cw * 0.7)
    crop_x = int(cw * 0.15)
    crop_y = int(ch * 0.05)
    crop = img_cntower.crop((crop_x, crop_y, crop_x + crop_w, crop_y + crop_w))
    sq = crop.resize((320, 320), PIL.Image.LANCZOS)
    sq.save(os.path.join(DRAWABLE_DIR, "ic_avatar_landmark_cntower.png"), format="PNG", optimize=True)
    print("Upgraded ic_avatar_landmark_cntower.png (Exterior needle view)")

# 2. Marvel: Captain America, Thor, Black Panther, Doctor Strange, Thanos, Loki, Deadpool, Wolverine, Spiderman
MARVEL_SOURCES = {
    # Captain America (Steve Rogers in Endgame scale armor with helmet/shield)
    "marvel_captain": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/d/d7/CapAmerica-EndgameProfile.jpg/revision/latest",
        "crop": (0.05, 0.05, 0.95, 0.8),
        "target_w": 260,
        "top_pad": 20
    },
    # Thor (Chris Hemsworth with Stormbreaker / short hair / heroic bust)
    "marvel_thor": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/1/13/Thor-EndgameProfile.jpg/revision/latest",
        "crop": (0.05, 0.05, 0.95, 0.8),
        "target_w": 260,
        "top_pad": 20
    },
    # Black Panther (Chadwick Boseman silver vibranium suit front bust)
    "marvel_panther": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/9/99/Black_Panther_AIW_Profile.jpg/revision/latest",
        "crop": (0.05, 0.05, 0.95, 0.8),
        "target_w": 260,
        "top_pad": 20
    },
    # Doctor Strange (Benedict Cumberbatch with Cloak of Levitation and Eye of Agamotto)
    "marvel_strange": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/4/4f/Doctor_Strange_AIW_Profile.jpg/revision/latest",
        "crop": (0.05, 0.05, 0.95, 0.8),
        "target_w": 260,
        "top_pad": 20
    },
    # Thanos (Infinity War golden armor and menacing front portrait)
    "marvel_thanos": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/5/52/Thanos_Endgame_Infobox.jpg/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 18
    },
    # Loki (Horned golden helmet of mischief)
    "marvel_loki": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/b/b8/Loki_AIW_Profile.jpg/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.8),
        "target_w": 255,
        "top_pad": 18
    },
    # Deadpool (Ryan Reynolds red & black masked head)
    "marvel_deadpool": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/6/66/Deadpool_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    },
    # Wolverine (Hugh Jackman crossed claws & yellow cowl)
    "marvel_wolverine": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/7/7c/Wolverine_Infobox.png/revision/latest",
        "crop": (0.0, 0.0, 1.0, 0.62),
        "target_w": 270,
        "top_pad": 18
    },
    # Spider-Man (Masked Integrated Suit)
    "marvel_spiderman": {
        "url": "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/4/41/Spider-Man_Infobox.jpg/revision/latest",
        "crop": (0.1, 0.05, 0.9, 0.75),
        "target_w": 260,
        "top_pad": 20
    }
}

for item_id, cfg in MARVEL_SOURCES.items():
    raw = fetch_img(cfg["url"])
    if raw:
        rw, rh = raw.size
        c = cfg["crop"]
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=cfg["target_w"], top_pad=cfg["top_pad"], crop_box=crop_box)
        composed.save(os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png"), format="PNG", optimize=True)
        print(f"Upgraded ic_avatar_{item_id}.png")

# 3. DC Comics: Superman, Batman, Peacemaker, Harley Quinn, Joker, Aquaman, Flash
DC_SOURCES = {
    # Superman (Henry Cavill front bust with vibrant 'S' shield)
    "dc_superman": {
        "url": "https://static.wikia.nocookie.net/dccu/images/2/20/Superman_-_Justice_League_-_promo.jpg/revision/latest",
        "crop": (0.15, 0.05, 0.85, 0.65),
        "target_w": 260,
        "top_pad": 20
    },
    # Batman (Ben Affleck cowl with 22px headroom for ears)
    "dc_batman": {
        "url": "https://static.wikia.nocookie.net/dccu/images/2/2e/Batman_-_Justice_League_-_promo.jpg/revision/latest",
        "crop": (0.15, 0.05, 0.85, 0.65),
        "target_w": 255,
        "top_pad": 22
    },
    # Peacemaker (John Cena in polished chrome helmet and red uniform)
    "dc_peacemaker": {
        "url": "https://static.wikia.nocookie.net/dccu/images/a/a2/Peacemaker_Infobox.png/revision/latest",
        "crop": (0.1, 0.02, 0.9, 0.65),
        "target_w": 260,
        "top_pad": 20
    },
    # Harley Quinn (Margot Robbie pigtails bust with cheerful smile)
    "dc_harley": {
        "url": "https://static.wikia.nocookie.net/dccu/images/7/7b/Harley_Quinn_Infobox.png/revision/latest",
        "crop": (0.1, 0.02, 0.9, 0.68),
        "target_w": 260,
        "top_pad": 18
    },
    # Joker (Heath Ledger purple coat and iconic face paint)
    "dc_joker": {
        "url": "https://upload.wikimedia.org/wikipedia/en/9/90/HeathJoker.png",
        "crop": (0.05, 0.05, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    },
    # Aquaman (Jason Momoa golden scale armor bust)
    "dc_aquaman": {
        "url": "https://static.wikia.nocookie.net/dccu/images/c/c9/Aquaman_-_Justice_League_-_promo.jpg/revision/latest",
        "crop": (0.15, 0.05, 0.85, 0.65),
        "target_w": 260,
        "top_pad": 20
    },
    # Flash (Ezra Miller crimson cowl and lightning)
    "dc_flash": {
        "url": "https://static.wikia.nocookie.net/dccu/images/4/4e/The_Flash_-_Justice_League_-_promo.jpg/revision/latest",
        "crop": (0.15, 0.05, 0.85, 0.65),
        "target_w": 260,
        "top_pad": 20
    }
}

for item_id, cfg in DC_SOURCES.items():
    raw = fetch_img(cfg["url"])
    if raw:
        rw, rh = raw.size
        c = cfg["crop"]
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=cfg["target_w"], top_pad=cfg["top_pad"], crop_box=crop_box)
        composed.save(os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png"), format="PNG", optimize=True)
        print(f"Upgraded ic_avatar_{item_id}.png")

# 4. Sci-Fi: RoboCop, Cooper, T-800, Xenomorph, Neo, Paul Atreides
SCIFI_SOURCES = {
    # RoboCop (Peter Weller in iconic silver titanium helmet and black visor)
    "scifi_robocop": {
        "url": "https://static.wikia.nocookie.net/robocop/images/8/82/RoboCop_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.68),
        "target_w": 260,
        "top_pad": 20
    },
    # Cooper (Matthew McConaughey in Interstellar white astronaut suit)
    "scifi_cooper": {
        "url": "https://static.wikia.nocookie.net/interstellarfilm/images/3/30/Cooper_Infobox.jpg/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    },
    # T-800 (Terminator chrome skull endoskeleton with glowing red eye)
    "scifi_t800": {
        "url": "https://static.wikia.nocookie.net/terminator/images/a/ac/T-800_Endoskeleton_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    },
    # Xenomorph (Sleek elongated biomechanical head of Alien)
    "scifi_xenomorph": {
        "url": "https://static.wikia.nocookie.net/avp/images/2/23/Drone_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 265,
        "top_pad": 20
    },
    # Neo (Keanu Reeves in dark shades and iconic trenchcoat)
    "scifi_neo": {
        "url": "https://static.wikia.nocookie.net/matrix/images/b/b2/Neo_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    },
    # Paul Atreides (Timothée Chalamet in Fremen stillsuit with blue eyes)
    "scifi_paul": {
        "url": "https://static.wikia.nocookie.net/dune/images/a/a2/Paul_Atreides_Dune_2021.jpg/revision/latest",
        "crop": (0.05, 0.05, 0.95, 0.75),
        "target_w": 260,
        "top_pad": 20
    }
}

for item_id, cfg in SCIFI_SOURCES.items():
    raw = fetch_img(cfg["url"])
    if raw:
        rw, rh = raw.size
        c = cfg["crop"]
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=cfg["target_w"], top_pad=cfg["top_pad"], crop_box=crop_box)
        composed.save(os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png"), format="PNG", optimize=True)
        print(f"Upgraded ic_avatar_{item_id}.png")

# 5. Video Games: Geralt, Jin Sakai, Link, Arthur Morgan, Master Chief
GAME_SOURCES = {
    # Geralt of Rivia (CDPR The Witcher 3 game render with white hair and wolf medallion)
    "game_geralt": {
        "url": "https://static.wikia.nocookie.net/witcher/images/6/68/Tw3_Geralt_of_Rivia_new_render.png/revision/latest",
        "crop": (0.1, 0.02, 0.9, 0.65),
        "target_w": 260,
        "top_pad": 18
    },
    # Jin Sakai (Ghost of Tsushima with ghost mask and headband bust)
    "game_jinsakai": {
        "url": "https://static.wikia.nocookie.net/ghostoftsushima/images/c/c5/Jin_Sakai_Infobox.png/revision/latest",
        "crop": (0.1, 0.02, 0.9, 0.65),
        "target_w": 260,
        "top_pad": 18
    },
    # Link (Breath of the Wild Champion's Tunic heroic bust)
    "game_link": {
        "url": "https://static.wikia.nocookie.net/zelda_gamepedia_en/images/8/87/BotW_Link_Artwork.png/revision/latest",
        "crop": (0.1, 0.02, 0.9, 0.65),
        "target_w": 260,
        "top_pad": 18
    },
    # Arthur Morgan (Red Dead Redemption 2 cover portrait with hat)
    "game_arthur": {
        "url": "https://static.wikia.nocookie.net/reddeadredemption/images/1/1a/Arthur_Morgan_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.70),
        "target_w": 260,
        "top_pad": 18
    },
    # Master Chief (Halo Infinite Mjolnir helmet with gold reflective visor)
    "game_chief": {
        "url": "https://static.wikia.nocookie.net/halo/images/7/7b/HINF_MasterChief_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.70),
        "target_w": 260,
        "top_pad": 18
    }
}

for item_id, cfg in GAME_SOURCES.items():
    raw = fetch_img(cfg["url"])
    if raw:
        rw, rh = raw.size
        c = cfg["crop"]
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=cfg["target_w"], top_pad=cfg["top_pad"], crop_box=crop_box)
        composed.save(os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png"), format="PNG", optimize=True)
        print(f"Upgraded ic_avatar_{item_id}.png")

# 6. Anime: Goku, Luffy, Saitama
ANIME_SOURCES = {
    # Son Goku (Dragon Ball Super gi bust)
    "anime_goku": {
        "url": "https://static.wikia.nocookie.net/dragonball/images/e/e4/Goku_DBS_Broly_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.68),
        "target_w": 260,
        "top_pad": 18
    },
    # Monkey D. Luffy (Straw Hat smiling bust)
    "anime_luffy": {
        "url": "https://static.wikia.nocookie.net/onepiece/images/6/6d/Monkey_D._Luffy_Anime_Post_Timeskip_Infobox.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.68),
        "target_w": 260,
        "top_pad": 18
    },
    # Saitama (One Punch Man heroic bald bust)
    "anime_saitama": {
        "url": "https://static.wikia.nocookie.net/onepunchman/images/b/b3/Saitama_profile.png/revision/latest",
        "crop": (0.05, 0.02, 0.95, 0.70),
        "target_w": 260,
        "top_pad": 20
    }
}

for item_id, cfg in ANIME_SOURCES.items():
    raw = fetch_img(cfg["url"])
    if raw:
        rw, rh = raw.size
        c = cfg["crop"]
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=cfg["target_w"], top_pad=cfg["top_pad"], crop_box=crop_box)
        composed.save(os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png"), format="PNG", optimize=True)
        print(f"Upgraded ic_avatar_{item_id}.png")

print("Primary batch completed.")
