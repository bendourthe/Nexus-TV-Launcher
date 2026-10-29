import os
import io
import json
import urllib.request
import urllib.parse
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

def get_fandom_thumb(wiki, page_title):
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(page_title)}&prop=pageimages&format=json&pithumbsize=600"
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
        print(f"  [Fandom Error {wiki}/{page_title}]: {e}")
    return None, None

def search_commons_file(query):
    # Search Wikimedia Commons for best high-res photograph
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrnamespace=6&gsrlimit=8&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "CleanLauncher/1.0 (bdour@cleanlauncher.internal)"})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        for p in pages.values():
            title = p.get("title", "").lower()
            if any(skip in title for skip in ["stamp", "coin", "map", "chart", "diagram", "book", "text", "drawing", "illustration", "infographic"]):
                continue
            infos = p.get("imageinfo", [])
            if infos:
                url_candidate = infos[0].get("thumburl") or infos[0].get("url")
                if url_candidate:
                    return url_candidate, "https://commons.wikimedia.org/"
    except Exception as e:
        print(f"  [Commons Search Error {query}]: {e}")
    return None, None

def download_and_crop(url, referer, filename, crop_mode="face"):
    headers = {"User-Agent": "Mozilla/5.0"}
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    try:
        raw = urllib.request.urlopen(req, timeout=12).read()
        img = PIL.Image.open(io.BytesIO(raw)).convert("RGBA")
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
        print(f"SAVED: {filename} from {url[:70]}")
        return True
    except Exception as e:
        print(f"ERROR processing {filename}: {e}")
        return False

# Specifically fix known gaps:
FIXES = [
    # DC Bane
    ("dc_bane.png", "fandom", "batman", "Bane (Nolanverse)", "face"),
    # Kaiju MUTO
    ("kaiju_muto.png", "fandom", "godzilla", "Jinshin-Mushi", "face"),
    # Sci-Fi Chani
    ("scifi_chani.png", "fandom", "dune", "Chani Kynes/2021 film", "face"),

    # Landscapes
    ("land_aurora.png", "commons", None, "Aurora borealis over Tromso fjord green", "center"),
    ("land_yosemite.png", "commons", None, "Tunnel View Yosemite National Park sunset", "center"),
    ("land_amalfi.png", "commons", None, "Positano view Amalfi coast colorful houses", "center"),
    ("land_patagonia.png", "commons", None, "Cuernos del Paine Lake Pehoe Patagonia", "center"),
    ("land_bali.png", "commons", None, "Tegalalang rice terrace Ubud Bali sunrise palm", "center"),

    # Space
    ("space_andromeda.png", "commons", None, "Andromeda Galaxy M31 Spitzer", "center"),
    ("space_ringnebula.png", "commons", None, "Ring Nebula Messier 57 Hubble", "center"),

    # Animals
    ("animal_bear.png", "commons", None, "Ursus maritimus head portrait Svalbard", "face"),
    ("animal_fox.png", "commons", None, "Red Fox Portrait Vulpes vulpes head", "face"),
    ("animal_eagle.png", "commons", None, "Bald Eagle Portrait Haliaeetus leucocephalus head", "face"),
    ("animal_owl.png", "commons", None, "Tyto alba head portrait feathers", "face"),
    ("animal_panda.png", "commons", None, "Giant panda Ailuropoda melanoleuca portrait bamboo", "face"),
    ("animal_gorilla.png", "commons", None, "Mountain gorilla Gorilla beringei head portrait", "face"),

    # Pets
    ("pet_frenchie.png", "commons", None, "French Bulldog dog portrait fawn", "face"),
    ("pet_husky.png", "commons", None, "Siberian Husky dog head portrait blue eyes", "face"),
    ("pet_germanshepherd.png", "commons", None, "German Shepherd Dog portrait alert ears", "face"),
    ("pet_corgi.png", "commons", None, "Pembroke Welsh Corgi portrait smiling face", "face"),
    ("pet_mainecoon.png", "commons", None, "Maine Coon cat portrait tufted ears", "face"),
    ("pet_britishshorthair.png", "commons", None, "British Shorthair blue cat round face", "face"),
    ("pet_bengal.png", "commons", None, "Bengal cat portrait leopard rosettes green eyes", "face"),
    ("pet_hamster.png", "commons", None, "Mesocricetus auratus golden hamster portrait", "face"),

    # Cars
    ("car_aston.png", "commons", None, "Aston Martin Valkyrie front Geneva Motor Show", "car"),

    # Music
    ("music_daftpunk.png", "commons", None, "Daft Punk Grammy 2014 white helmets", "face"),
    ("music_weeknd.png", "commons", None, "The Weeknd Cannes 2023 head portrait", "face"),
    ("music_taylorswift.png", "commons", None, "Taylor Swift at the 2023 MTV Video Music Awards", "face"),
    ("music_billie.png", "commons", None, "Billie Eilish 2019 by Glenn Francis", "face"),
    ("music_eminem.png", "commons", None, "Eminem Concert for Valor head portrait", "face"),
    ("music_freddie.png", "commons", None, "Freddie Mercury performing in 1984 Queen", "face"),
    ("music_bowie.png", "commons", None, "David Bowie 1974 head portrait", "face"),
    ("music_jimi.png", "commons", None, "Jimi Hendrix 1967 head portrait", "face"),
    ("music_drake.png", "commons", None, "Drake at The Cheesecake Factory 2019", "face"),

    # Abstract
    ("abstract_synthwave.png", "commons", None, "Synthwave retro 80s neon grid sun", "center"),
    ("abstract_silk.png", "commons", None, "Silk ribbon fabric waves flowing", "center"),
    ("abstract_cyberpunk.png", "commons", None, "Fiber optic glowing cyan magenta strands", "center"),
]

for item in FIXES:
    filename, kind, wiki_or_none, query_or_title, crop = item
    if kind == "fandom":
        url, ref = get_fandom_thumb(wiki_or_none, query_or_title)
    else:
        url, ref = search_commons_file(query_or_title)
        
    if url:
        download_and_crop(url, ref, f"ic_avatar_{filename}", crop)
    else:
        print(f"FAILED search for {filename} ({query_or_title})")
