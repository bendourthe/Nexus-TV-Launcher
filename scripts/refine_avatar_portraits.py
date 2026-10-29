import os
import io
import time
import urllib.request
import urllib.parse
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

REFINEMENTS = [
    # Animals (remove white circle borders)
    ("ic_avatar_animal_leopard.png", "https://upload.wikimedia.org/wikipedia/commons/a/a5/Irbis4.JPG", "face"),
    ("ic_avatar_animal_wolf.png", "https://upload.wikimedia.org/wikipedia/commons/6/64/Arctic_Wolf_in_Aulavik_National_Park_04_%28cropped%29.jpg", "face"),
    # Cars (front 3/4 aggressive angles instead of rear views)
    ("ic_avatar_car_ferrari.png", "https://upload.wikimedia.org/wikipedia/commons/1/13/Red_2019_Ferrari_SF90_Stradale_%2848264238897%29_%28cropped%29.jpg", "center"),
    ("ic_avatar_car_aston.png", "https://upload.wikimedia.org/wikipedia/commons/e/ec/Aston_Martin_Valkyrie_Verification_Prototype_001_Genf_2019_1Y7A5569.jpg", "center"),
    # Lord of the Rings (Peter Jackson movies iconic face portraits instead of illustrations)
    ("ic_avatar_lotr_aragorn.png", "https://upload.wikimedia.org/wikipedia/en/3/35/Aragorn300ppx.png", "face"),
    ("ic_avatar_lotr_gandalf.png", "https://upload.wikimedia.org/wikipedia/en/e/e9/Gandalf600ppx.jpg", "face"),
    ("ic_avatar_lotr_legolas.png", "https://upload.wikimedia.org/wikipedia/en/2/2b/Legolas600ppx.jpg", "face"),
    ("ic_avatar_lotr_gimli.png", "https://upload.wikimedia.org/wikipedia/en/f/f0/Gimli_With_Axe.jpg", "face"),
    ("ic_avatar_lotr_gollum.png", "https://upload.wikimedia.org/wikipedia/en/e/e0/Gollum.PNG", "face"),
    ("ic_avatar_lotr_witchking.png", "https://static.wikia.nocookie.net/lotr/images/9/9f/FF-Witch-King-port.png/revision/latest", "face"),
    # Sci-Fi (Paul Atreides close-up face from 2021/2024 Dune film)
    ("ic_avatar_scifi_paul.png", "https://static.wikia.nocookie.net/dune/images/0/03/Dune3_Paul_Textless.jpeg/revision/latest", "face"),
    # Anime (Official modern anime face art instead of fan art or manga b&w)
    ("ic_avatar_anime_naruto.png", "https://static.wikia.nocookie.net/naruto/images/d/d6/Naruto_Part_I.png/revision/latest", "face"),
    ("ic_avatar_anime_ichigo.png", "https://static.wikia.nocookie.net/bleach/images/4/41/NBFHIchigo_profile.png/revision/latest", "face"),
    ("ic_avatar_anime_eren.png", "https://static.wikia.nocookie.net/shingekinokyojin/images/a/a1/Eren_Jaeger_%28Anime%29_character_image.png/revision/latest", "face"),
    ("ic_avatar_anime_levi.png", "https://static.wikia.nocookie.net/shingekinokyojin/images/b/b1/Levi_Ackermann_%28Anime%29_character_image.png/revision/latest", "face"),
]

def crop_and_save(img_bytes, filename, crop_mode="face"):
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
    elif crop_mode == "center":
        crop_size = min(w, h)
        left = (w - crop_size) // 2
        top = (h - crop_size) // 2
    else:
        crop_size = min(w, h)
        left = (w - crop_size) // 2
        top = int(0.15 * (h - crop_size))

    cropped = img.crop((left, top, left + crop_size, top + crop_size))
    final = cropped.resize((320, 320), PIL.Image.LANCZOS)
    out_path = os.path.join(DRAWABLE_DIR, filename)
    final.save(out_path, format="PNG", optimize=True)
    print(f"Refined {filename} ({w}x{h} -> 320x320)")

for filename, url, crop_mode in REFINEMENTS:
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        raw = urllib.request.urlopen(req, timeout=12).read()
        crop_and_save(raw, filename, crop_mode)
        time.sleep(0.3)
    except Exception as e:
        print(f"Error refining {filename} from {url}: {e}")
