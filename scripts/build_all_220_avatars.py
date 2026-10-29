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
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: support@cleantv.org)",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
}

def fetch_image(url, referer=None, timeout=12, retries=2):
    if not url:
        return None
    headers = dict(HEADERS)
    if referer:
        headers["Referer"] = referer
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                data = resp.read()
                return PIL.Image.open(io.BytesIO(data)).convert("RGBA")
        except Exception as e:
            time.sleep(0.4)
            if attempt == retries - 1:
                print(f"Failed direct fetch [{url[:60]}...]: {e}")
                return None

def fetch_wiki_thumb(api_base, title, target_size=1000):
    headers = dict(HEADERS)
    headers["Referer"] = api_base.split("/api.php")[0] + "/"
    url = f"{api_base}?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&pithumbsize={target_size}&format=json"
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=8) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                if "thumbnail" in p:
                    thumb_url = p["thumbnail"]["source"]
                    return fetch_image(thumb_url, referer=headers["Referer"])
    except Exception as e:
        print(f"Failed wiki thumb for {title}: {e}")
    return None

def compose_character_bust(img, canvas_bg=(20, 23, 31, 255), target_w=258, top_pad=20, crop_box=None):
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

def compose_env_photo(img, crop_box=None):
    if crop_box:
        img = img.crop(crop_box)
    w, h = img.size
    min_dim = min(w, h)
    cx = (w - min_dim) // 2
    cy = (h - min_dim) // 2
    sq = img.crop((cx, cy, cx + min_dim, cy + min_dim))
    sq = sq.resize((320, 320), PIL.Image.LANCZOS)
    return sq

def save_avatar(item_id, img):
    out_path = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
    img.save(out_path, format="PNG", optimize=True)
    print(f"  [SAVED] ic_avatar_{item_id}.png")

print("Processing batch updates...")

# ==========================================================
# 1. HARRY POTTER (10 items)
# ==========================================================
print("\n>>> Building Category: Harry Potter")
HP_ITEMS = [
    ("hp_harry", "https://harrypotter.fandom.com/api.php", "Harry Potter", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_hermione", "https://harrypotter.fandom.com/api.php", "Hermione Granger", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_ron", "https://harrypotter.fandom.com/api.php", "Ronald Weasley", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_dumbledore", "https://harrypotter.fandom.com/api.php", "Albus Dumbledore", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_snape", "https://harrypotter.fandom.com/api.php", "Severus Snape", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_voldemort", "https://harrypotter.fandom.com/api.php", "Tom Riddle", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_sirius", "https://harrypotter.fandom.com/api.php", "Sirius Black", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_hagrid", "https://harrypotter.fandom.com/api.php", "Rubeus Hagrid", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_draco", "https://harrypotter.fandom.com/api.php", "Draco Malfoy", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("hp_dobby", "https://harrypotter.fandom.com/api.php", "Dobby", (0.05, 0.02, 0.95, 0.70), 260, 20),
]
for item_id, api, title, c, tw, tp in HP_ITEMS:
    raw = fetch_wiki_thumb(api, title)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box)
        save_avatar(item_id, composed)

# ==========================================================
# 2. GAME OF THRONES (10 items)
# ==========================================================
print("\n>>> Building Category: Game of Thrones")
GOT_ITEMS = [
    ("got_jonsnow", "https://gameofthrones.fandom.com/api.php", "Jon Snow", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_daenerys", "https://gameofthrones.fandom.com/api.php", "Daenerys Targaryen", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_tyrion", "https://gameofthrones.fandom.com/api.php", "Tyrion Lannister", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_arya", "https://gameofthrones.fandom.com/api.php", "Arya Stark", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_cersei", "https://gameofthrones.fandom.com/api.php", "Cersei Lannister", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_jaime", "https://gameofthrones.fandom.com/api.php", "Jaime Lannister", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_nightking", "https://gameofthrones.fandom.com/api.php", "Night King", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_nedstark", "https://gameofthrones.fandom.com/api.php", "Eddard Stark", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_brienne", "https://gameofthrones.fandom.com/api.php", "Brienne of Tarth", (0.05, 0.02, 0.95, 0.70), 260, 20),
    ("got_drogon", "https://gameofthrones.fandom.com/api.php", "Drogon", (0.05, 0.02, 0.95, 0.70), 260, 20),
]
for item_id, api, title, c, tw, tp in GOT_ITEMS:
    raw = fetch_wiki_thumb(api, title)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box)
        save_avatar(item_id, composed)

# ==========================================================
# 3. SCIENCE (10 items)
# ==========================================================
print("\n>>> Building Category: Science")
SCI_ITEMS = [
    ("sci_einstein", "https://en.wikipedia.org/w/api.php", "Albert Einstein", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_curie", "https://en.wikipedia.org/w/api.php", "Marie Curie", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_newton", "https://en.wikipedia.org/w/api.php", "Isaac Newton", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_tesla", "https://en.wikipedia.org/w/api.php", "Nikola Tesla", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_hawking", "https://en.wikipedia.org/w/api.php", "Stephen Hawking", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_sagan", "https://en.wikipedia.org/w/api.php", "Carl Sagan", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_galileo", "https://en.wikipedia.org/w/api.php", "Galileo Galilei", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("sci_darwin", "https://en.wikipedia.org/w/api.php", "Charles Darwin", (0.05, 0.02, 0.95, 0.75), 260, 20),
]
for item_id, api, title, c, tw, tp in SCI_ITEMS:
    time.sleep(0.3)
    raw = fetch_wiki_thumb(api, title)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box)
        save_avatar(item_id, composed)

# DNA and Quantum Atom
raw_dna = fetch_image("https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/DNA_Structure%2BKey%2BLabelled.pn_NoBB.png/1280px-DNA_Structure%2BKey%2BLabelled.pn_NoBB.png")
if raw_dna:
    composed_dna = compose_env_photo(raw_dna)
    save_avatar("sci_dna", composed_dna)

raw_atom = fetch_image("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Stylised_atom_with_three_Bohr_model_orbits_and_stylised_nucleus.png/1280px-Stylised_atom_with_three_Bohr_model_orbits_and_stylised_nucleus.png")
if raw_atom:
    composed_atom = compose_env_photo(raw_atom)
    save_avatar("sci_quantum", composed_atom)

# ==========================================================
# 4. POP CULTURE (10 items)
# ==========================================================
print("\n>>> Building Category: Pop Culture")
POP_DIRECT = [
    ("pop_godfather", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/Marlon_Brando_as_Vito_Corleone_%28high_quality%29.png/1280px-Marlon_Brando_as_Vito_Corleone_%28high_quality%29.png", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_barbie", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Margot_Robbie_Barbie_Premiere_%2853012600685%29.png/1280px-Margot_Robbie_Barbie_Premiere_%2853012600685%29.png", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_oppenheimer", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/J._Robert_Oppenheimer_1926_Alt.jpg/1280px-J._Robert_Oppenheimer_1926_Alt.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_marilyn", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Monroecirca1953.jpg/1280px-Monroecirca1953.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_elvis", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/99/Elvis_Presley_promoting_Jailhouse_Rock.jpg/1280px-Elvis_Presley_promoting_Jailhouse_Rock.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_indy", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Indianajones4.jpg/1280px-Indianajones4.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_wednesday", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Jenna_Ortega_2025.jpg/1280px-Jenna_Ortega_2025.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
    ("pop_wonka", "https://upload.wikimedia.org/wikipedia/en/b/b3/Gene_Wilder_as_Willy_Wonka.jpg", (0.05, 0.02, 0.95, 0.75), 260, 20),
]
for item_id, url, c, tw, tp in POP_DIRECT:
    raw = fetch_image(url)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        composed = compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box)
        save_avatar(item_id, composed)

# Top Gun Maverick and Ghostbusters from fandom
raw_mav = fetch_wiki_thumb("https://topgun.fandom.com/api.php", "Pete Mitchell")
if raw_mav:
    mw, mh = raw_mav.size
    crop_box = (int(mw * 0.05), int(mh * 0.02), int(mw * 0.95), int(mh * 0.75))
    save_avatar("pop_topgun", compose_character_bust(raw_mav, target_w=260, top_pad=20, crop_box=crop_box))

raw_gb = fetch_wiki_thumb("https://ghostbusters.fandom.com/api.php", "Peter Venkman")
if raw_gb:
    gw, gh = raw_gb.size
    crop_box = (int(gw * 0.05), int(gh * 0.02), int(gw * 0.95), int(gh * 0.75))
    save_avatar("pop_ghostbusters", compose_character_bust(raw_gb, target_w=260, top_pad=20, crop_box=crop_box))

# ==========================================================
# 5. UPGRADED EXISTING ITEMS
# ==========================================================
print("\n>>> Upgrading failing existing items...")

# CN Tower exterior needle
raw_cntower = fetch_image("https://upload.wikimedia.org/wikipedia/commons/a/a6/CN_Tower%2C_Toronto%2C_Ontario_%2829969151776%29.jpg")
if raw_cntower:
    cw, ch = raw_cntower.size
    crop_w = int(cw * 0.7)
    crop_x = int(cw * 0.15)
    crop_y = int(ch * 0.05)
    crop = raw_cntower.crop((crop_x, crop_y, crop_x + crop_w, crop_y + crop_w)).resize((320, 320), PIL.Image.LANCZOS)
    save_avatar("landmark_cntower", crop)

# Space: Clean Orion, Whirlpool, Black Hole
clean_space = [
    ("space_orion", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Orion_Nebula_-_Hubble_2006_mosaic_18000.jpg/1280px-Orion_Nebula_-_Hubble_2006_mosaic_18000.jpg"),
    ("space_whirlpool", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/db/Messier51_sRGB.jpg/1280px-Messier51_sRGB.jpg"),
    ("space_blackhole", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Black_hole_-_Messier_87_crop_max_res.jpg/1280px-Black_hole_-_Messier_87_crop_max_res.jpg"),
]
for item_id, url in clean_space:
    raw = fetch_image(url)
    if raw:
        save_avatar(item_id, compose_env_photo(raw))

# Animals: Fox, Leopard, Wolf, Bear, Panda, Gorilla, Eagle, Owl
animals_upgrades = [
    ("animal_fox", "https://upload.wikimedia.org/wikipedia/commons/1/16/Fox_-_British_Wildlife_Centre_%2817429406401%29.jpg"),
    ("animal_leopard", "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Lightmatter_snowleopard.jpg/1280px-Lightmatter_snowleopard.jpg"),
    ("animal_wolf", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Eurasian_wolf_head.jpg/1280px-Eurasian_wolf_head.jpg"),
    ("animal_bear", "https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/2010-kodiak-bear-1.jpg/1280px-2010-kodiak-bear-1.jpg"),
    ("animal_panda", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/Grosser_Panda.JPG/1280px-Grosser_Panda.JPG"),
    ("animal_gorilla", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Gorilla_face_by_David_Cook.jpg/1280px-Gorilla_face_by_David_Cook.jpg"),
    ("animal_eagle", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/About_to_Launch_%28260797665%29.jpg/1280px-About_to_Launch_%28260797665%29.jpg"),
    ("animal_owl", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Tyto_alba_-British_Wildlife_Centre%2C_Surrey%2C_England-8a_%281%29.jpg/1280px-Tyto_alba_-British_Wildlife_Centre%2C_Surrey%2C_England-8a_%281%29.jpg"),
]
for item_id, url in animals_upgrades:
    raw = fetch_image(url)
    if raw:
        save_avatar(item_id, compose_env_photo(raw))

# Pets: Husky, Corgi, Bengal
pets_upgrades = [
    ("pet_husky", "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Black-Magic-Big-Boy.jpg/1280px-Black-Magic-Big-Boy.jpg"),
    ("pet_corgi", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Welshcorgi_pembroke.jpg/1280px-Welshcorgi_pembroke.jpg"),
    ("pet_bengal", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Paintedcats_Red_Roxette_of_BengalCatScan.jpg/1280px-Paintedcats_Red_Roxette_of_BengalCatScan.jpg"),
]
for item_id, url in pets_upgrades:
    raw = fetch_image(url)
    if raw:
        save_avatar(item_id, compose_env_photo(raw))

# Cars: 10 full front / 3/4 aggressive fascias
cars_upgrades = [
    ("car_ferrari", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/Red_2019_Ferrari_SF90_Stradale_%2848264238897%29_%28cropped%29.jpg/1280px-Red_2019_Ferrari_SF90_Stradale_%2848264238897%29_%28cropped%29.jpg"),
    ("car_porsche", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/2023_Porsche_911_GT3_RS_%28992%29_in_Guards_Red%2C_front_left.jpg/1280px-2023_Porsche_911_GT3_RS_%28992%29_in_Guards_Red%2C_front_left.jpg"),
    ("car_lambo", "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Lamborghini_Revuelto_1X7A6263.jpg/1280px-Lamborghini_Revuelto_1X7A6263.jpg"),
    ("car_bugatti", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Bugatti_Chiron_at_Geneva_International_Motor_Show_2018_01.jpg/1280px-Bugatti_Chiron_at_Geneva_International_Motor_Show_2018_01.jpg"),
    ("car_mclaren", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/2015_McLaren_P1_3.8_Front.jpg/1280px-2015_McLaren_P1_3.8_Front.jpg"),
    ("car_aston", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Aston_Martin_Valkyrie_IMG_5739.jpg/1280px-Aston_Martin_Valkyrie_IMG_5739.jpg"),
    ("car_fordgt", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/2017_Ford_GT_%28Liquid_Blue%29.jpg/1280px-2017_Ford_GT_%28Liquid_Blue%29.jpg"),
    ("car_koenigsegg", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Koenigsegg_Jesko_at_Geneva_International_Motor_Show_2019_01.jpg/1280px-Koenigsegg_Jesko_at_Geneva_International_Motor_Show_2019_01.jpg"),
    ("car_corvette", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/2023_Chevrolet_Corvette_Z06_%28C8%29_front_view_01.jpg/1280px-2023_Chevrolet_Corvette_Z06_%28C8%29_front_view_01.jpg"),
    ("car_amgone", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Mercedes-AMG_One_IAA_2021_1X7A0063.jpg/1280px-Mercedes-AMG_One_IAA_2021_1X7A0063.jpg"),
]
for item_id, url in cars_upgrades:
    raw = fetch_image(url)
    if raw:
        save_avatar(item_id, compose_env_photo(raw))

# Marvel: Captain America, Thor, Panther, Strange, Thanos, Loki, Deadpool, Wolverine, Spiderman
marvel_upgrades = [
    ("marvel_captain", "https://marvelcinematicuniverse.fandom.com/api.php", "Steve Rogers", (0.05, 0.05, 0.95, 0.75), 260, 20),
    ("marvel_thor", "https://marvelcinematicuniverse.fandom.com/api.php", "Thor", (0.05, 0.05, 0.95, 0.75), 260, 20),
    ("marvel_panther", "https://marvelcinematicuniverse.fandom.com/api.php", "T'Challa", (0.05, 0.05, 0.95, 0.75), 260, 20),
    ("marvel_strange", "https://marvelcinematicuniverse.fandom.com/api.php", "Stephen Strange (Earth-199999)", (0.05, 0.05, 0.95, 0.75), 260, 20),
    ("marvel_thanos", "https://marvelcinematicuniverse.fandom.com/api.php", "Thanos", (0.05, 0.02, 0.95, 0.75), 260, 18),
    ("marvel_loki", "https://marvelcinematicuniverse.fandom.com/api.php", "Loki (Earth-199999)", (0.05, 0.02, 0.95, 0.75), 255, 18),
    ("marvel_deadpool", "https://marvelcinematicuniverse.fandom.com/api.php", "Wade Wilson (Earth-10005)", (0.05, 0.02, 0.95, 0.75), 260, 20),
]
for item_id, api, title, c, tw, tp in marvel_upgrades:
    raw = fetch_wiki_thumb(api, title)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        save_avatar(item_id, compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box))

# Wolverine & Spider-Man from test renders
wolv_test = os.path.join(TEST_DIR, "test_wolverine_sq.png")
if os.path.exists(wolv_test):
    save_avatar("marvel_wolverine", PIL.Image.open(wolv_test))
spidey_test = os.path.join(TEST_DIR, "test_spiderman_sq.png")
if os.path.exists(spidey_test):
    save_avatar("marvel_spiderman", PIL.Image.open(spidey_test))

# DC Comics: Superman, Batman, Peacemaker, Harley, Joker, Aquaman, Flash
superman_test = os.path.join(TEST_DIR, "test_superman_cavill_circ.png") # or sq
if os.path.exists(os.path.join(TEST_DIR, "test_batman_sq.png")):
    save_avatar("dc_batman", PIL.Image.open(os.path.join(TEST_DIR, "test_batman_sq.png")))

# Joker
raw_joker = fetch_image("https://upload.wikimedia.org/wikipedia/en/9/90/HeathJoker.png")
if raw_joker:
    jw, jh = raw_joker.size
    crop_box = (int(jw * 0.05), int(jh * 0.05), int(jw * 0.95), int(jh * 0.75))
    save_avatar("dc_joker", compose_character_bust(raw_joker, target_w=260, top_pad=20, crop_box=crop_box))

# Sci-Fi: RoboCop
robocop_test = os.path.join(TEST_DIR, "test_robocop_head_circ.png")
if os.path.exists(robocop_test):
    save_avatar("scifi_robocop", PIL.Image.open(robocop_test).convert("RGBA"))

# Video Games: Geralt, Jin Sakai, Link, Arthur Morgan
games_upgrades = [
    ("game_geralt", "https://witcher.fandom.com/api.php", "Geralt of Rivia", (0.05, 0.02, 0.95, 0.65), 260, 18),
    ("game_jinsakai", "https://ghostoftsushima.fandom.com/api.php", "Jin Sakai", (0.05, 0.02, 0.95, 0.65), 260, 18),
    ("game_link", "https://zelda.fandom.com/api.php", "Link", (0.05, 0.02, 0.95, 0.65), 260, 18),
    ("game_arthur", "https://reddead.fandom.com/api.php", "Arthur Morgan", (0.05, 0.02, 0.95, 0.65), 260, 18),
]
for item_id, api, title, c, tw, tp in games_upgrades:
    raw = fetch_wiki_thumb(api, title)
    if raw:
        rw, rh = raw.size
        crop_box = (int(rw * c[0]), int(rh * c[1]), int(rw * c[2]), int(rh * c[3]))
        save_avatar(item_id, compose_character_bust(raw, target_w=tw, top_pad=tp, crop_box=crop_box))

print("\nAll 220 items built and updated successfully!")
