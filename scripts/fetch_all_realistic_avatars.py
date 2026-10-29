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
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(page_title)}&prop=pageimages&format=json&pithumbsize=800"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        resp = urllib.request.urlopen(req, timeout=8)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        if pages:
            page = list(pages.values())[0]
            thumb = page.get("thumbnail", {}).get("source")
            if thumb:
                clean_url = thumb.split("/revision/")[0] + "/revision/latest"
                return clean_url
    except Exception as e:
        print(f"    [Fandom Error {wiki}/{page_title}]: {e}")
    return None

def get_commons_search_thumb(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrnamespace=6&gsrlimit=1&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "CleanLauncher/1.0 (bdour@example.com)"})
    try:
        resp = urllib.request.urlopen(req, timeout=8)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        if pages:
            page = list(pages.values())[0]
            infos = page.get("imageinfo", [])
            if infos:
                return infos[0].get("thumburl") or infos[0].get("url")
    except Exception as e:
        print(f"    [Commons Search Error {query}]: {e}")
    return None

def get_bing_image(query):
    try:
        url = "https://www.bing.com/images/search?q=" + urllib.parse.quote(query) + "&first=1"
        req = urllib.request.Request(url, headers=HEADERS)
        html = urllib.request.urlopen(req, timeout=6).read().decode("utf-8", errors="ignore")
        matches = re.findall(r'murl&quot;:&quot;(http[^&]+)&quot;', html)
        if matches:
            for m in matches:
                # prefer jpg/png/webp
                if any(ext in m.lower() for ext in [".jpg", ".jpeg", ".png", ".webp"]):
                    return m
            return matches[0]
    except Exception as e:
        print(f"    [Bing Error {query}]: {e}")
    return None

def download_bytes(url):
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        resp = urllib.request.urlopen(req, timeout=12)
        return resp.read()
    except Exception as e:
        print(f"    [Download Error {url[:60]}...]: {e}")
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

# 16 Categories (10 items each = 160 items)
AVATARS = [
    # ==================== 1. LANDSCAPES ====================
    {"id": "ic_avatar_land_fuji.png", "type": "commons", "query": "Lake Kawaguchiko Sakura Mount Fuji", "crop": "center"},
    {"id": "ic_avatar_land_aurora.png", "type": "commons", "query": "Northern Lights Aurora Borealis Tromso green", "crop": "center"},
    {"id": "ic_avatar_land_yosemite.png", "type": "commons", "query": "Yosemite Valley Tunnel View autumn sunset", "crop": "center"},
    {"id": "ic_avatar_land_matterhorn.png", "type": "commons", "query": "Matterhorn Zermatt Switzerland Riffelsee reflection", "crop": "center"},
    {"id": "ic_avatar_land_amalfi.png", "type": "commons", "query": "Positano Amalfi coast panoramic view Italy", "crop": "center"},
    {"id": "ic_avatar_land_grandcanyon.png", "type": "commons", "query": "Horseshoe Bend Page Arizona sunset Colorado river", "crop": "center"},
    {"id": "ic_avatar_land_maldives.png", "type": "commons", "query": "Maldives overwater bungalows turquoise ocean", "crop": "center"},
    {"id": "ic_avatar_land_sahara.png", "type": "commons", "query": "Erg Chebbi sand dunes Sahara desert sunset", "crop": "center"},
    {"id": "ic_avatar_land_patagonia.png", "type": "commons", "query": "Torres del Paine granite horns lake Pehoe", "crop": "center"},
    {"id": "ic_avatar_land_bali.png", "type": "commons", "query": "Tegalalang rice terraces Ubud Bali sunrise", "crop": "center"},

    # ==================== 2. SPACE ====================
    {"id": "ic_avatar_space_pillars.png", "type": "commons", "query": "Pillars of creation 2014 HST WFC3-UVIS", "crop": "center"},
    {"id": "ic_avatar_space_carina.png", "type": "commons", "query": "Cosmic Cliffs in Carina Nebula JWST NIRCam", "crop": "center"},
    {"id": "ic_avatar_space_andromeda.png", "type": "commons", "query": "Andromeda Galaxy M31 Spitzer GALEX Herschel", "crop": "center"},
    {"id": "ic_avatar_space_ringnebula.png", "type": "commons", "query": "Ring Nebula Messier 57 JWST NIRCam", "crop": "center"},
    {"id": "ic_avatar_space_orion.png", "type": "commons", "query": "Messier 42 Orion Nebula Hubble 2006", "crop": "center"},
    {"id": "ic_avatar_space_crab.png", "type": "commons", "query": "Crab Nebula Messier 1 Hubble large", "crop": "center"},
    {"id": "ic_avatar_space_sombrero.png", "type": "commons", "query": "Sombrero Galaxy Messier 104 Hubble", "crop": "center"},
    {"id": "ic_avatar_space_whirlpool.png", "type": "commons", "query": "Whirlpool Galaxy Messier 51 Hubble 2005", "crop": "center"},
    {"id": "ic_avatar_space_blackhole.png", "type": "commons", "query": "EHT First Image of a Black Hole M87", "crop": "center"},
    {"id": "ic_avatar_space_eclipse.png", "type": "commons", "query": "Total solar eclipse 2017 corona diamond ring", "crop": "center"},

    # ==================== 3. PLANETS ====================
    # (Planets already saved high-res: Earth, Mars, Jupiter, Saturn, Neptune, Moon, Sun, Venus, Uranus, Pluto)
    # Retaining verified 10 planet assets:
    {"id": "ic_avatar_planet_earth.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_mars.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_jupiter.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_saturn.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_neptune.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_moon.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_sun.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_kepler.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_gliesia.png", "type": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_inferno.png", "type": "skip", "crop": "center"},

    # ==================== 4. ANIMALS ====================
    {"id": "ic_avatar_animal_lion.png", "type": "bing", "query": "male lion face close up eye contact studio photography", "crop": "face"},
    {"id": "ic_avatar_animal_tiger.png", "type": "bing", "query": "bengal tiger face close up eye contact photography", "crop": "face"},
    {"id": "ic_avatar_animal_leopard.png", "type": "bing", "query": "snow leopard face close up blue eyes photography", "crop": "face"},
    {"id": "ic_avatar_animal_wolf.png", "type": "bing", "query": "arctic white wolf face portrait snow eye contact", "crop": "face"},
    {"id": "ic_avatar_animal_bear.png", "type": "bing", "query": "polar bear face portrait close up snow", "crop": "face"},
    {"id": "ic_avatar_animal_fox.png", "type": "bing", "query": "red fox face portrait close up snow photography", "crop": "face"},
    {"id": "ic_avatar_animal_eagle.png", "type": "bing", "query": "bald eagle face close up golden eye hooked beak", "crop": "face"},
    {"id": "ic_avatar_animal_owl.png", "type": "bing", "query": "great horned owl face close up eye contact", "crop": "face"},
    {"id": "ic_avatar_animal_panda.png", "type": "bing", "query": "giant panda face close up eating bamboo photography", "crop": "face"},
    {"id": "ic_avatar_animal_gorilla.png", "type": "bing", "query": "silverback mountain gorilla face portrait close up", "crop": "face"},

    # ==================== 5. PETS ====================
    {"id": "ic_avatar_pet_retriever.png", "type": "bing", "query": "golden retriever smiling face close up portrait", "crop": "face"},
    {"id": "ic_avatar_pet_frenchie.png", "type": "bing", "query": "fawn french bulldog puppy face close up bat ears", "crop": "face"},
    {"id": "ic_avatar_pet_husky.png", "type": "bing", "query": "siberian husky face close up bright blue eyes", "crop": "face"},
    {"id": "ic_avatar_pet_germanshepherd.png", "type": "bing", "query": "german shepherd dog face portrait alert ears", "crop": "face"},
    {"id": "ic_avatar_pet_corgi.png", "type": "bing", "query": "pembroke welsh corgi smiling face close up", "crop": "face"},
    {"id": "ic_avatar_pet_mainecoon.png", "type": "bing", "query": "maine coon cat regal face portrait tufted ears", "crop": "face"},
    {"id": "ic_avatar_pet_britishshorthair.png", "type": "bing", "query": "british shorthair blue cat round face copper eyes", "crop": "face"},
    {"id": "ic_avatar_pet_bengal.png", "type": "bing", "query": "bengal cat face close up green eyes leopard spots", "crop": "face"},
    {"id": "ic_avatar_pet_hamster.png", "type": "bing", "query": "syrian hamster cute face close up holding seed", "crop": "face"},
    {"id": "ic_avatar_pet_cockatiel.png", "type": "bing", "query": "cockatiel face close up orange cheeks yellow crest", "crop": "face"},

    # ==================== 6. CARS ====================
    {"id": "ic_avatar_car_ferrari.png", "type": "commons", "query": "Ferrari SF90, BAS 24, Brussels (P1170502-RR)", "crop": "car"},
    {"id": "ic_avatar_car_porsche.png", "type": "commons", "query": "Porsche 911 GT3 RS, Auto 2025, Zurich", "crop": "car"},
    {"id": "ic_avatar_car_lambo.png", "type": "bing", "query": "Lamborghini Revuelto front view wallpaper 4k aggressive", "crop": "car"},
    {"id": "ic_avatar_car_bugatti.png", "type": "bing", "query": "Bugatti Chiron front horseshoe grille wallpaper 4k", "crop": "car"},
    {"id": "ic_avatar_car_mclaren.png", "type": "bing", "query": "McLaren P1 front view headlights wallpaper 4k", "crop": "car"},
    {"id": "ic_avatar_car_aston.png", "type": "bing", "query": "Aston Martin Valkyrie hypercar front view wallpaper", "crop": "car"},
    {"id": "ic_avatar_car_fordgt.png", "type": "bing", "query": "Ford GT front headlights wallpaper 4k aggressive", "crop": "car"},
    {"id": "ic_avatar_car_koenigsegg.png", "type": "bing", "query": "Koenigsegg Jesko Attack front splitter wallpaper 4k", "crop": "car"},
    {"id": "ic_avatar_car_corvette.png", "type": "bing", "query": "Corvette Z06 C8 front face view wallpaper 4k", "crop": "car"},
    {"id": "ic_avatar_car_amgone.png", "type": "bing", "query": "Mercedes-AMG ONE front hypercar wallpaper 4k", "crop": "car"},

    # ==================== 7. MUSIC ====================
    {"id": "ic_avatar_music_daftpunk.png", "type": "bing", "query": "Daft Punk helmets close up portrait studio", "crop": "face"},
    {"id": "ic_avatar_music_weeknd.png", "type": "bing", "query": "The Weeknd After Hours red suit face portrait close up", "crop": "face"},
    {"id": "ic_avatar_music_taylorswift.png", "type": "bing", "query": "Taylor Swift Eras Tour microphone face close up", "crop": "face"},
    {"id": "ic_avatar_music_billie.png", "type": "bing", "query": "Billie Eilish portrait face close up studio", "crop": "face"},
    {"id": "ic_avatar_music_eminem.png", "type": "bing", "query": "Eminem face portrait close up studio", "crop": "face"},
    {"id": "ic_avatar_music_freddie.png", "type": "bing", "query": "Freddie Mercury live aid face close up mustache", "crop": "face"},
    {"id": "ic_avatar_music_bowie.png", "type": "bing", "query": "David Bowie Aladdin Sane lightning bolt face portrait", "crop": "face"},
    {"id": "ic_avatar_music_jimi.png", "type": "bing", "query": "Jimi Hendrix face portrait close up bandana", "crop": "face"},
    {"id": "ic_avatar_music_michael.png", "type": "bing", "query": "Michael Jackson fedora face close up portrait", "crop": "face"},
    {"id": "ic_avatar_music_drake.png", "type": "bing", "query": "Drake artist face close up portrait studio", "crop": "face"},

    # ==================== 8. ABSTRACT ====================
    {"id": "ic_avatar_abstract_iridescent.png", "type": "bing", "query": "3D iridescent liquid glass sphere render 4k black background", "crop": "center"},
    {"id": "ic_avatar_abstract_goldblack.png", "type": "bing", "query": "molten liquid gold swirling through black marble 3D render", "crop": "center"},
    {"id": "ic_avatar_abstract_chromesphere.png", "type": "bing", "query": "liquid mercury chrome reflective sphere 3D render", "crop": "center"},
    {"id": "ic_avatar_abstract_synthwave.png", "type": "bing", "query": "synthwave neon grid wireframe glowing purple sun 4k", "crop": "center"},
    {"id": "ic_avatar_abstract_amethyst.png", "type": "bing", "query": "amethyst purple crystalline geode quartz core macro", "crop": "center"},
    {"id": "ic_avatar_abstract_prism.png", "type": "bing", "query": "crystal glass prism scattering rainbow spectrum light 4k", "crop": "center"},
    {"id": "ic_avatar_abstract_silk.png", "type": "bing", "query": "flowing metallic velvet silk ribbon 3D render black background", "crop": "center"},
    {"id": "ic_avatar_abstract_quantum.png", "type": "bing", "query": "quantum particle energy vortex cyan purple glowing 4k", "crop": "center"},
    {"id": "ic_avatar_abstract_kintsugi.png", "type": "bing", "query": "kintsugi cracked japanese black porcelain repaired pure gold", "crop": "center"},
    {"id": "ic_avatar_abstract_cyberpunk.png", "type": "bing", "query": "cyberpunk neon fiber wave turquoise magenta holographic 3D", "crop": "center"},

    # ==================== 9. MARVEL ====================
    {"id": "ic_avatar_marvel_ironman.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Iron_Man", "crop": "face"},
    {"id": "ic_avatar_marvel_captain.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Captain_America", "crop": "face"},
    {"id": "ic_avatar_marvel_thor.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Thor", "crop": "face"},
    {"id": "ic_avatar_marvel_spiderman.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Spider-Man", "crop": "face"},
    {"id": "ic_avatar_marvel_panther.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Black_Panther", "crop": "face"},
    {"id": "ic_avatar_marvel_strange.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Doctor_Strange", "crop": "face"},
    {"id": "ic_avatar_marvel_thanos.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Thanos", "crop": "face"},
    {"id": "ic_avatar_marvel_loki.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Loki", "crop": "face"},
    {"id": "ic_avatar_marvel_deadpool.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Deadpool", "crop": "face"},
    {"id": "ic_avatar_marvel_wolverine.png", "type": "fandom", "wiki": "marvelcinematicuniverse", "title": "Wolverine", "crop": "face"},

    # ==================== 10. DC COMICS ====================
    {"id": "ic_avatar_dc_batman.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Batman", "crop": "face"},
    {"id": "ic_avatar_dc_superman.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Superman", "crop": "face"},
    {"id": "ic_avatar_dc_wonderwoman.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Wonder_Woman", "crop": "face"},
    {"id": "ic_avatar_dc_joker.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Joker", "crop": "face"},
    {"id": "ic_avatar_dc_peacemaker.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Peacemaker", "crop": "face"},
    {"id": "ic_avatar_dc_harley.png", "type": "fandom", "wiki": "dcextendeduniverse", "title": "Harley_Quinn", "crop": "face"},
    {"id": "ic_avatar_dc_aquaman.png", "type": "bing", "query": "Aquaman Jason Momoa face close up movie portrait", "crop": "face"},
    {"id": "ic_avatar_dc_flash.png", "type": "bing", "query": "The Flash movie Ezra Miller cowl face close up", "crop": "face"},
    {"id": "ic_avatar_dc_blackadam.png", "type": "bing", "query": "Black Adam Dwayne Johnson glowing chest face portrait", "crop": "face"},
    {"id": "ic_avatar_dc_bane.png", "type": "bing", "query": "Bane Tom Hardy The Dark Knight Rises mask face close up", "crop": "face"},

    # ==================== 11. STAR WARS ====================
    {"id": "ic_avatar_sw_vader.png", "type": "bing", "query": "Darth Vader glossy helmet face close up movie portrait", "crop": "face"},
    {"id": "ic_avatar_sw_mando.png", "type": "fandom", "wiki": "starwars", "title": "Din_Djarin", "crop": "face"},
    {"id": "ic_avatar_sw_grogu.png", "type": "fandom", "wiki": "starwars", "title": "Din_Grogu", "crop": "face"},
    {"id": "ic_avatar_sw_ahsoka.png", "type": "fandom", "wiki": "starwars", "title": "Ahsoka_Tano", "crop": "face"},
    {"id": "ic_avatar_sw_obiwan.png", "type": "fandom", "wiki": "starwars", "title": "Obi-Wan_Kenobi", "crop": "face"},
    {"id": "ic_avatar_sw_bobafett.png", "type": "fandom", "wiki": "starwars", "title": "Boba_Fett", "crop": "face"},
    {"id": "ic_avatar_sw_maul.png", "type": "fandom", "wiki": "starwars", "title": "Maul", "crop": "face"},
    {"id": "ic_avatar_sw_kyloren.png", "type": "fandom", "wiki": "starwars", "title": "Ben_Solo", "crop": "face"},
    {"id": "ic_avatar_sw_stormtrooper.png", "type": "fandom", "wiki": "starwars", "title": "Stormtrooper", "crop": "face"},
    {"id": "ic_avatar_sw_luke.png", "type": "fandom", "wiki": "starwars", "title": "Luke_Skywalker", "crop": "face"},

    # ==================== 12. LORD OF THE RINGS ====================
    {"id": "ic_avatar_lotr_aragorn.png", "type": "fandom", "wiki": "lotr", "title": "Aragorn_II", "crop": "face"},
    {"id": "ic_avatar_lotr_gandalf.png", "type": "fandom", "wiki": "lotr", "title": "Gandalf", "crop": "face"},
    {"id": "ic_avatar_lotr_legolas.png", "type": "bing", "query": "Legolas Orlando Bloom Lord of the Rings movie face close up", "crop": "face"},
    {"id": "ic_avatar_lotr_gimli.png", "type": "bing", "query": "Gimli Lord of the Rings movie face close up braided beard", "crop": "face"},
    {"id": "ic_avatar_lotr_frodo.png", "type": "fandom", "wiki": "lotr", "title": "Frodo_Baggins", "crop": "face"},
    {"id": "ic_avatar_lotr_gollum.png", "type": "bing", "query": "Gollum Lord of the Rings movie face close up CGI", "crop": "face"},
    {"id": "ic_avatar_lotr_sauron.png", "type": "bing", "query": "Sauron Dark Lord spiked battle armor helmet face Lord of the Rings", "crop": "face"},
    {"id": "ic_avatar_lotr_galadriel.png", "type": "fandom", "wiki": "lotr", "title": "Galadriel", "crop": "face"},
    {"id": "ic_avatar_lotr_sam.png", "type": "bing", "query": "Samwise Gamgee Sean Astin Lord of the Rings face close up", "crop": "face"},
    {"id": "ic_avatar_lotr_witchking.png", "type": "bing", "query": "Witch King of Angmar iron spiked crown face Lord of the Rings", "crop": "face"},

    # ==================== 13. KAIJUS ====================
    {"id": "ic_avatar_kaiju_godzilla.png", "type": "fandom", "wiki": "godzilla", "title": "Godzilla_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_kong.png", "type": "fandom", "wiki": "godzilla", "title": "King_Kong_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_ghidorah.png", "type": "fandom", "wiki": "godzilla", "title": "King_Ghidorah_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_mothra.png", "type": "fandom", "wiki": "godzilla", "title": "Mothra_(MonsterVerse)", "crop": "center"},
    {"id": "ic_avatar_kaiju_rodan.png", "type": "fandom", "wiki": "godzilla", "title": "Rodan_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_mechagodzilla.png", "type": "fandom", "wiki": "godzilla", "title": "Mechagodzilla_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_shimo.png", "type": "fandom", "wiki": "godzilla", "title": "Shimo", "crop": "face"},
    {"id": "ic_avatar_kaiju_skarking.png", "type": "fandom", "wiki": "godzilla", "title": "Skar_King", "crop": "face"},
    {"id": "ic_avatar_kaiju_muto.png", "type": "bing", "query": "MUTO MonsterVerse titan face red eye Godzilla 2014", "crop": "face"},
    {"id": "ic_avatar_kaiju_skullcrawler.png", "type": "bing", "query": "Skullcrawler Kong Skull Island face MonsterVerse", "crop": "face"},

    # ==================== 14. SCI-FI ====================
    {"id": "ic_avatar_scifi_paul.png", "type": "bing", "query": "Paul Atreides Timothee Chalamet Dune Part Two blue eyes face portrait", "crop": "face"},
    {"id": "ic_avatar_scifi_chani.png", "type": "bing", "query": "Chani Zendaya Dune Part Two fremen stillsuit face portrait", "crop": "face"},
    {"id": "ic_avatar_scifi_neo.png", "type": "fandom", "wiki": "matrix", "title": "Neo", "crop": "face"},
    {"id": "ic_avatar_scifi_t800.png", "type": "fandom", "wiki": "terminator", "title": "T-800", "crop": "face"},
    {"id": "ic_avatar_scifi_deckard.png", "type": "fandom", "wiki": "bladerunner", "title": "K", "crop": "face"},
    {"id": "ic_avatar_scifi_ripley.png", "type": "fandom", "wiki": "avp", "title": "Ellen_Ripley", "crop": "face"},
    {"id": "ic_avatar_scifi_xenomorph.png", "type": "fandom", "wiki": "avp", "title": "Xenomorph_XX121_(Alien)", "crop": "face"},
    {"id": "ic_avatar_scifi_cooper.png", "type": "bing", "query": "Matthew McConaughey Cooper Interstellar astronaut helmet face", "crop": "face"},
    {"id": "ic_avatar_scifi_caesar.png", "type": "fandom", "wiki": "planetoftheapes", "title": "Caesar", "crop": "face"},
    {"id": "ic_avatar_scifi_robocop.png", "type": "bing", "query": "RoboCop classic titanium helmet visor face portrait", "crop": "face"},

    # ==================== 15. VIDEO GAMES ====================
    {"id": "ic_avatar_game_kratos.png", "type": "fandom", "wiki": "godofwar", "title": "Kratos", "crop": "face"},
    {"id": "ic_avatar_game_geralt.png", "type": "fandom", "wiki": "witcher", "title": "Geralt_of_Rivia", "crop": "face"},
    {"id": "ic_avatar_game_chief.png", "type": "fandom", "wiki": "halo", "title": "John-117", "crop": "face"},
    {"id": "ic_avatar_game_arthur.png", "type": "fandom", "wiki": "reddead", "title": "Arthur_Morgan", "crop": "face"},
    {"id": "ic_avatar_game_ellie.png", "type": "fandom", "wiki": "thelastofus", "title": "Ellie_Williams", "crop": "face"},
    {"id": "ic_avatar_game_joel.png", "type": "fandom", "wiki": "thelastofus", "title": "Joel_Miller", "crop": "face"},
    {"id": "ic_avatar_game_jinsakai.png", "type": "fandom", "wiki": "ghostoftsushima", "title": "Jin_Sakai", "crop": "face"},
    {"id": "ic_avatar_game_silverhand.png", "type": "fandom", "wiki": "cyberpunk", "title": "Johnny_Silverhand", "crop": "face"},
    {"id": "ic_avatar_game_cloud.png", "type": "fandom", "wiki": "finalfantasy", "title": "Cloud_Strife", "crop": "face"},
    {"id": "ic_avatar_game_link.png", "type": "fandom", "wiki": "zelda", "title": "Link", "crop": "face"},

    # ==================== 16. ANIME ====================
    {"id": "ic_avatar_anime_goku.png", "type": "fandom", "wiki": "dragonball", "title": "Goku", "crop": "face"},
    {"id": "ic_avatar_anime_luffy.png", "type": "fandom", "wiki": "onepiece", "title": "Monkey_D._Luffy", "crop": "face"},
    {"id": "ic_avatar_anime_naruto.png", "type": "fandom", "wiki": "naruto", "title": "Naruto_Uzumaki", "crop": "face"},
    {"id": "ic_avatar_anime_ichigo.png", "type": "fandom", "wiki": "bleach", "title": "Ichigo_Kurosaki", "crop": "face"},
    {"id": "ic_avatar_anime_tanjiro.png", "type": "fandom", "wiki": "kimetsu-no-yaiba", "title": "Tanjiro_Kamado", "crop": "face"},
    {"id": "ic_avatar_anime_nezuko.png", "type": "fandom", "wiki": "kimetsu-no-yaiba", "title": "Nezuko_Kamado", "crop": "face"},
    {"id": "ic_avatar_anime_gojo.png", "type": "fandom", "wiki": "jujutsu-kaisen", "title": "Satoru_Gojo", "crop": "face"},
    {"id": "ic_avatar_anime_eren.png", "type": "bing", "query": "Eren Yeager Season 4 face close up anime portrait", "crop": "face"},
    {"id": "ic_avatar_anime_levi.png", "type": "fandom", "wiki": "shingekinokyojin", "title": "Levi_Ackerman", "crop": "face"},
    {"id": "ic_avatar_anime_saitama.png", "type": "fandom", "wiki": "onepunchman", "title": "Saitama", "crop": "face"},
]

def main():
    print(f"=== Starting Fetch & Process for {len(AVATARS)} Avatars ===")
    os.makedirs(OUT_DIR, exist_ok=True)
    
    total = len(AVATARS)
    success_count = 0
    skipped_count = 0
    
    for idx, item in enumerate(AVATARS, 1):
        filename = item["id"]
        t = item["type"]
        crop = item.get("crop", "face")
        
        target_path = os.path.join(OUT_DIR, filename)
        if t == "skip" and os.path.exists(target_path):
            print(f"[{idx}/{total}] SKIP existing planet: {filename}")
            skipped_count += 1
            success_count += 1
            continue
            
        print(f"[{idx}/{total}] Processing {filename} ({t})...")
        img_url = None
        
        if t == "fandom":
            img_url = get_fandom_thumb(item["wiki"], item["title"])
            if not img_url:
                # fallback to bing
                fallback_query = f"{item['title']} face portrait"
                print(f"    Fandom failed, trying fallback Bing search for '{fallback_query}'...")
                img_url = get_bing_image(fallback_query)
        elif t == "commons":
            img_url = get_commons_search_thumb(item["query"])
            if not img_url:
                print(f"    Commons failed, trying fallback Bing search for '{item['query']}'...")
                img_url = get_bing_image(item["query"])
        elif t == "bing":
            img_url = get_bing_image(item["query"])
            
        if not img_url:
            print(f"  FAILED to find image URL for {filename}")
            continue
            
        print(f"    URL: {img_url[:75]}...")
        raw_bytes = download_bytes(img_url)
        if not raw_bytes:
            print(f"    Failed downloading {img_url[:75]}")
            continue
            
        if crop_and_save(raw_bytes, filename, crop):
            success_count += 1
            print(f"    SAVED {filename} (320x320)")
        else:
            print(f"    FAILED cropping/saving {filename}")
            
        # polite rate-limiting pause
        time.sleep(0.15)
        
    print(f"\n=== FINISHED: {success_count} / {total} processed successfully ({skipped_count} retained) ===")

if __name__ == "__main__":
    main()
