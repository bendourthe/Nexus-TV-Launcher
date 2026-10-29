import os
import io
import json
import urllib.request
import urllib.parse
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

HEADERS_COMMONS = {"User-Agent": "CleanLauncher/1.0 (bdour@cleanlauncher.internal)"}
HEADERS_FANDOM = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}

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

def get_commons_file_url(filename):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&titles=File:{urllib.parse.quote(filename)}&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers=HEADERS_COMMONS)
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
        print(f"  [Commons File Error {filename}]: {e}")
    return None

def search_commons_file(query):
    # Filter out stamps, charts, coins, maps, book scans
    full_query = f"{query} -stamp -coin -chart -diagram -map -book -illustration -drawing"
    url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(full_query)}&gsrnamespace=6&gsrlimit=5&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
    req = urllib.request.Request(url, headers=HEADERS_COMMONS)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        data = json.loads(resp.read())
        pages = data.get("query", {}).get("pages", {})
        for p in pages.values():
            title = p.get("title", "").lower()
            if any(skip in title for skip in ["stamp", "coin", "map", "chart", "diagram", "book", "text"]):
                continue
            infos = p.get("imageinfo", [])
            if infos:
                url_candidate = infos[0].get("thumburl") or infos[0].get("url")
                if url_candidate:
                    return url_candidate
    except Exception as e:
        print(f"  [Commons Search Error {query}]: {e}")
    return None

def download_image(url, referer=None):
    headers = {"User-Agent": "Mozilla/5.0"}
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    try:
        resp = urllib.request.urlopen(req, timeout=12)
        return resp.read()
    except Exception as e:
        print(f"  [Download Error {url[:60]}...]: {e}")
        return None

def crop_and_save(raw_bytes, target_filename, crop_mode="face"):
    try:
        img = PIL.Image.open(io.BytesIO(raw_bytes)).convert("RGBA")
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
        out_path = os.path.join(DRAWABLE_DIR, target_filename)
        final.save(out_path, format="PNG", optimize=True)
        return True
    except Exception as e:
        print(f"  [Crop Error {target_filename}]: {e}")
        return False

# EXACT MAPPINGS FOR ALL 160 AVATARS:
TARGETS = [
    # 1. LANDSCAPES
    {"id": "ic_avatar_land_fuji.png", "src": "commons_search", "query": "Mount Fuji lake Kawaguchiko cherry blossom", "crop": "center"},
    {"id": "ic_avatar_land_aurora.png", "src": "commons_search", "query": "Aurora Borealis Tromso green fjord reflection", "crop": "center"},
    {"id": "ic_avatar_land_yosemite.png", "src": "commons_search", "query": "Yosemite Valley Tunnel View sunset El Capitan Half Dome", "crop": "center"},
    {"id": "ic_avatar_land_matterhorn.png", "src": "commons_search", "query": "Matterhorn Riffelsee reflection Zermatt Switzerland", "crop": "center"},
    {"id": "ic_avatar_land_amalfi.png", "src": "commons_search", "query": "Positano village Amalfi coast cliff colorful houses sea", "crop": "center"},
    {"id": "ic_avatar_land_grandcanyon.png", "src": "commons_search", "query": "Horseshoe Bend Page Arizona Colorado River sunset", "crop": "center"},
    {"id": "ic_avatar_land_maldives.png", "src": "commons_search", "query": "Maldives overwater bungalows turquoise ocean aerial", "crop": "center"},
    {"id": "ic_avatar_land_sahara.png", "src": "commons_search", "query": "Erg Chebbi sand dunes Sahara desert sunset Morocco", "crop": "center"},
    {"id": "ic_avatar_land_patagonia.png", "src": "commons_search", "query": "Torres del Paine granite horns Lake Pehoe Patagonia Chile", "crop": "center"},
    {"id": "ic_avatar_land_bali.png", "src": "commons_search", "query": "Tegalalang rice terrace Ubud Bali green palm sunrise", "crop": "center"},

    # 2. SPACE
    {"id": "ic_avatar_space_pillars.png", "src": "commons_search", "query": "Pillars of Creation 2014 HST WFC3", "crop": "center"},
    {"id": "ic_avatar_space_carina.png", "src": "commons_search", "query": "Cosmic Cliffs Carina Nebula JWST NIRCam", "crop": "center"},
    {"id": "ic_avatar_space_andromeda.png", "src": "commons_search", "query": "Andromeda Galaxy M31 Spitzer GALEX Herschel", "crop": "center"},
    {"id": "ic_avatar_space_ringnebula.png", "src": "commons_search", "query": "Ring Nebula Messier 57 JWST NIRCam", "crop": "center"},
    {"id": "ic_avatar_space_orion.png", "src": "commons_search", "query": "Messier 42 Orion Nebula Hubble 2006", "crop": "center"},
    {"id": "ic_avatar_space_crab.png", "src": "commons_search", "query": "Crab Nebula Messier 1 Hubble large", "crop": "center"},
    {"id": "ic_avatar_space_sombrero.png", "src": "commons_search", "query": "Sombrero Galaxy Messier 104 Hubble", "crop": "center"},
    {"id": "ic_avatar_space_whirlpool.png", "src": "commons_search", "query": "Whirlpool Galaxy Messier 51 Hubble 2005", "crop": "center"},
    {"id": "ic_avatar_space_blackhole.png", "src": "commons_search", "query": "EHT First Image of a Black Hole M87", "crop": "center"},
    {"id": "ic_avatar_space_eclipse.png", "src": "commons_search", "query": "Total solar eclipse 2017 corona diamond ring", "crop": "center"},

    # 3. PLANETS (Preserve verified NASA planetary imagery)
    {"id": "ic_avatar_planet_earth.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_mars.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_jupiter.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_saturn.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_neptune.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_moon.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_sun.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_kepler.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_gliesia.png", "src": "skip", "crop": "center"},
    {"id": "ic_avatar_planet_inferno.png", "src": "skip", "crop": "center"},

    # 4. ANIMALS
    {"id": "ic_avatar_animal_lion.png", "src": "skip", "crop": "face"}, # Verified fixed above!
    {"id": "ic_avatar_animal_tiger.png", "src": "commons_search", "query": "Portrait Panthera tigris altaica Bella Leipzig Zoo 2013", "crop": "face"},
    {"id": "ic_avatar_animal_leopard.png", "src": "commons_search", "query": "Snow Leopard Panthera uncia head portrait blue eyes", "crop": "face"},
    {"id": "ic_avatar_animal_wolf.png", "src": "commons_search", "query": "Arctic wolf Canis lupus arctos head portrait snow", "crop": "face"},
    {"id": "ic_avatar_animal_bear.png", "src": "commons_search", "query": "Polar Bear Ursus maritimus head portrait close up Svalbard", "crop": "face"},
    {"id": "ic_avatar_animal_fox.png", "src": "commons_search", "query": "Red Fox Portrait Vulpes vulpes head close up", "crop": "face"},
    {"id": "ic_avatar_animal_eagle.png", "src": "commons_search", "query": "Bald Eagle Portrait Haliaeetus leucocephalus head hooked beak", "crop": "face"},
    {"id": "ic_avatar_animal_owl.png", "src": "commons_search", "query": "Barn Owl Tyto alba portrait head close up feathers", "crop": "face"},
    {"id": "ic_avatar_animal_panda.png", "src": "commons_search", "query": "Giant Panda Ailuropoda melanoleuca eating bamboo portrait", "crop": "face"},
    {"id": "ic_avatar_animal_gorilla.png", "src": "commons_search", "query": "Silverback mountain gorilla Gorilla beringei head portrait", "crop": "face"},

    # 5. PETS
    {"id": "ic_avatar_pet_retriever.png", "src": "commons_search", "query": "Golden Retriever dog head portrait smiling", "crop": "face"},
    {"id": "ic_avatar_pet_frenchie.png", "src": "commons_search", "query": "French Bulldog dog head portrait bat ears", "crop": "face"},
    {"id": "ic_avatar_pet_husky.png", "src": "commons_search", "query": "Siberian Husky dog head portrait blue eyes", "crop": "face"},
    {"id": "ic_avatar_pet_germanshepherd.png", "src": "commons_search", "query": "German Shepherd dog head portrait alert ears", "crop": "face"},
    {"id": "ic_avatar_pet_corgi.png", "src": "commons_search", "query": "Pembroke Welsh Corgi dog head portrait smiling", "crop": "face"},
    {"id": "ic_avatar_pet_mainecoon.png", "src": "commons_search", "query": "Maine Coon cat head portrait tufted ears", "crop": "face"},
    {"id": "ic_avatar_pet_britishshorthair.png", "src": "commons_search", "query": "British Shorthair blue cat round head copper eyes", "crop": "face"},
    {"id": "ic_avatar_pet_bengal.png", "src": "commons_search", "query": "Bengal cat head portrait green eyes leopard rosettes", "crop": "face"},
    {"id": "ic_avatar_pet_hamster.png", "src": "commons_search", "query": "Golden hamster Syrian hamster Mesocricetus auratus close up", "crop": "face"},
    {"id": "ic_avatar_pet_cockatiel.png", "src": "commons_search", "query": "Cockatiel Nymphicus hollandicus portrait yellow crest orange cheeks", "crop": "face"},

    # 6. CARS (Front aggressive face of hypercars)
    {"id": "ic_avatar_car_ferrari.png", "src": "commons_search", "query": "Ferrari SF90 front view red supercar", "crop": "car"},
    {"id": "ic_avatar_car_porsche.png", "src": "commons_search", "query": "Porsche 911 GT3 RS Auto 2025 Zurich front", "crop": "car"},
    {"id": "ic_avatar_car_lambo.png", "src": "commons_search", "query": "Lamborghini Revuelto front view yellow Geneva", "crop": "car"},
    {"id": "ic_avatar_car_bugatti.png", "src": "commons_search", "query": "Bugatti Chiron front horseshoe grille Geneva", "crop": "car"},
    {"id": "ic_avatar_car_mclaren.png", "src": "commons_search", "query": "McLaren P1 front view orange Geneva", "crop": "car"},
    {"id": "ic_avatar_car_aston.png", "src": "commons_search", "query": "Aston Martin Valkyrie front hypercar Geneva", "crop": "car"},
    {"id": "ic_avatar_car_fordgt.png", "src": "commons_search", "query": "Ford GT front headlights supercar blue", "crop": "car"},
    {"id": "ic_avatar_car_koenigsegg.png", "src": "commons_search", "query": "Koenigsegg Jesko front Geneva motor show", "crop": "car"},
    {"id": "ic_avatar_car_corvette.png", "src": "commons_search", "query": "Chevrolet Corvette Z06 C8 front blue", "crop": "car"},
    {"id": "ic_avatar_car_amgone.png", "src": "commons_search", "query": "Mercedes-AMG One front IAA Munich hypercar", "crop": "car"},

    # 7. MUSIC
    {"id": "ic_avatar_music_daftpunk.png", "src": "commons_search", "query": "Daft Punk Grammy Awards 2014 white helmets", "crop": "face"},
    {"id": "ic_avatar_music_weeknd.png", "src": "commons_search", "query": "The Weeknd Cannes 2023 head portrait", "crop": "face"},
    {"id": "ic_avatar_music_taylorswift.png", "src": "commons_search", "query": "Taylor Swift at the 2023 MTV Video Music Awards head portrait", "crop": "face"},
    {"id": "ic_avatar_music_billie.png", "src": "commons_search", "query": "Billie Eilish 2019 by Glenn Francis head portrait", "crop": "face"},
    {"id": "ic_avatar_music_eminem.png", "src": "commons_search", "query": "Eminem live head portrait Concert for Valor", "crop": "face"},
    {"id": "ic_avatar_music_freddie.png", "src": "commons_search", "query": "Freddie Mercury performing in 1984 head portrait Queen", "crop": "face"},
    {"id": "ic_avatar_music_bowie.png", "src": "commons_search", "query": "David Bowie 1974 head portrait close up", "crop": "face"},
    {"id": "ic_avatar_music_jimi.png", "src": "commons_search", "query": "Jimi Hendrix 1967 head portrait close up", "crop": "face"},
    {"id": "ic_avatar_music_michael.png", "src": "commons_search", "query": "Michael Jackson fedora portrait 1988", "crop": "face"},
    {"id": "ic_avatar_music_drake.png", "src": "commons_search", "query": "Drake at The Cheesecake Factory 2019 head portrait", "crop": "face"},

    # 8. ABSTRACT (Procedural & crystalline)
    {"id": "ic_avatar_abstract_amethyst.png", "src": "commons_search", "query": "Amethyst geode crystalline macro purple quartz", "crop": "center"},
    {"id": "ic_avatar_abstract_prism.png", "src": "commons_search", "query": "Light dispersion glass prism rainbow spectrum", "crop": "center"},
    {"id": "ic_avatar_abstract_kintsugi.png", "src": "commons_search", "query": "Kintsugi repaired Japanese ceramic gold lacquer", "crop": "center"},
    {"id": "ic_avatar_abstract_quantum.png", "src": "commons_search", "query": "Quantum vortex simulation luminous energy field", "crop": "center"},
    {"id": "ic_avatar_abstract_iridescent.png", "src": "commons_search", "query": "Iridescent soap bubble macro thin film interference", "crop": "center"},
    {"id": "ic_avatar_abstract_goldblack.png", "src": "commons_search", "query": "Liquid gold black marble swirling luxury", "crop": "center"},
    {"id": "ic_avatar_abstract_chromesphere.png", "src": "commons_search", "query": "Chrome mirror sphere reflection panorama", "crop": "center"},
    {"id": "ic_avatar_abstract_synthwave.png", "src": "commons_search", "query": "80s retro synthwave neon grid sun sunset", "crop": "center"},
    {"id": "ic_avatar_abstract_silk.png", "src": "commons_search", "query": "Flowing metallic silk ribbon 3D dynamic", "crop": "center"},
    {"id": "ic_avatar_abstract_cyberpunk.png", "src": "commons_search", "query": "Fiber optic cyan magenta glowing neon strands", "crop": "center"},

    # 9. MARVEL (Official MCU Movie Face Portraits)
    {"id": "ic_avatar_marvel_ironman.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Iron_Man", "crop": "face"},
    {"id": "ic_avatar_marvel_captain.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Captain_America", "crop": "face"},
    {"id": "ic_avatar_marvel_thor.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Thor", "crop": "face"},
    {"id": "ic_avatar_marvel_spiderman.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Spider-Man", "crop": "face"},
    {"id": "ic_avatar_marvel_panther.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Black_Panther", "crop": "face"},
    {"id": "ic_avatar_marvel_strange.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Doctor_Strange", "crop": "face"},
    {"id": "ic_avatar_marvel_thanos.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Thanos", "crop": "face"},
    {"id": "ic_avatar_marvel_loki.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Loki", "crop": "face"},
    {"id": "ic_avatar_marvel_deadpool.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Deadpool", "crop": "face"},
    {"id": "ic_avatar_marvel_wolverine.png", "src": "fandom", "wiki": "marvelcinematicuniverse", "title": "Wolverine", "crop": "face"},

    # 10. DC COMICS (Official DC Movie Face Portraits)
    {"id": "ic_avatar_dc_batman.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Batman", "crop": "face"},
    {"id": "ic_avatar_dc_superman.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Superman", "crop": "face"},
    {"id": "ic_avatar_dc_wonderwoman.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Wonder_Woman", "crop": "face"},
    {"id": "ic_avatar_dc_joker.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Joker", "crop": "face"},
    {"id": "ic_avatar_dc_peacemaker.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Peacemaker", "crop": "face"},
    {"id": "ic_avatar_dc_harley.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Harley_Quinn", "crop": "face"},
    {"id": "ic_avatar_dc_aquaman.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Aquaman", "crop": "face"},
    {"id": "ic_avatar_dc_flash.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Flash", "crop": "face"},
    {"id": "ic_avatar_dc_blackadam.png", "src": "fandom", "wiki": "dcextendeduniverse", "title": "Black_Adam", "crop": "face"},
    {"id": "ic_avatar_dc_bane.png", "src": "fandom", "wiki": "batman", "title": "Bane_(The_Dark_Knight_Rises)", "crop": "face"},

    # 11. STAR WARS (Official Live-Action Face Portraits)
    {"id": "ic_avatar_sw_vader.png", "src": "fandom", "wiki": "starwars", "title": "Darth_Vader's_armor", "crop": "face"},
    {"id": "ic_avatar_sw_mando.png", "src": "fandom", "wiki": "starwars", "title": "Din_Djarin", "crop": "face"},
    {"id": "ic_avatar_sw_grogu.png", "src": "fandom", "wiki": "starwars", "title": "Din_Grogu", "crop": "face"},
    {"id": "ic_avatar_sw_ahsoka.png", "src": "fandom", "wiki": "starwars", "title": "Ahsoka_Tano", "crop": "face"},
    {"id": "ic_avatar_sw_obiwan.png", "src": "fandom", "wiki": "starwars", "title": "Obi-Wan_Kenobi", "crop": "face"},
    {"id": "ic_avatar_sw_bobafett.png", "src": "fandom", "wiki": "starwars", "title": "Boba_Fett", "crop": "face"},
    {"id": "ic_avatar_sw_maul.png", "src": "fandom", "wiki": "starwars", "title": "Maul", "crop": "face"},
    {"id": "ic_avatar_sw_kyloren.png", "src": "fandom", "wiki": "starwars", "title": "Ben_Solo", "crop": "face"},
    {"id": "ic_avatar_sw_stormtrooper.png", "src": "fandom", "wiki": "starwars", "title": "Stormtrooper", "crop": "face"},
    {"id": "ic_avatar_sw_luke.png", "src": "fandom", "wiki": "starwars", "title": "Luke_Skywalker", "crop": "face"},

    # 12. LORD OF THE RINGS (Peter Jackson Movie Trilogy Portraits)
    {"id": "ic_avatar_lotr_aragorn.png", "src": "fandom", "wiki": "lotr", "title": "Aragorn_II", "crop": "face"},
    {"id": "ic_avatar_lotr_gandalf.png", "src": "fandom", "wiki": "lotr", "title": "Gandalf", "crop": "face"},
    {"id": "ic_avatar_lotr_legolas.png", "src": "fandom", "wiki": "lotr", "title": "Legolas", "crop": "face"},
    {"id": "ic_avatar_lotr_gimli.png", "src": "fandom", "wiki": "lotr", "title": "Gimli", "crop": "face"},
    {"id": "ic_avatar_lotr_frodo.png", "src": "fandom", "wiki": "lotr", "title": "Frodo_Baggins", "crop": "face"},
    {"id": "ic_avatar_lotr_gollum.png", "src": "fandom", "wiki": "lotr", "title": "Gollum", "crop": "face"},
    {"id": "ic_avatar_lotr_sauron.png", "src": "fandom", "wiki": "lotr", "title": "Sauron", "crop": "face"},
    {"id": "ic_avatar_lotr_galadriel.png", "src": "fandom", "wiki": "lotr", "title": "Galadriel", "crop": "face"},
    {"id": "ic_avatar_lotr_sam.png", "src": "fandom", "wiki": "lotr", "title": "Samwise_Gamgee", "crop": "face"},
    {"id": "ic_avatar_lotr_witchking.png", "src": "fandom", "wiki": "lotr", "title": "Witch-king_of_Angmar", "crop": "face"},

    # 13. KAIJUS (MonsterVerse Movie Titan Portraits)
    {"id": "ic_avatar_kaiju_godzilla.png", "src": "fandom", "wiki": "godzilla", "title": "Godzilla_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_kong.png", "src": "fandom", "wiki": "godzilla", "title": "King_Kong_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_ghidorah.png", "src": "fandom", "wiki": "godzilla", "title": "King_Ghidorah_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_mothra.png", "src": "fandom", "wiki": "godzilla", "title": "Mothra_(MonsterVerse)", "crop": "center"},
    {"id": "ic_avatar_kaiju_rodan.png", "src": "fandom", "wiki": "godzilla", "title": "Rodan_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_mechagodzilla.png", "src": "fandom", "wiki": "godzilla", "title": "Mechagodzilla_(MonsterVerse)", "crop": "face"},
    {"id": "ic_avatar_kaiju_shimo.png", "src": "fandom", "wiki": "godzilla", "title": "Shimo", "crop": "face"},
    {"id": "ic_avatar_kaiju_skarking.png", "src": "fandom", "wiki": "godzilla", "title": "Skar_King", "crop": "face"},
    {"id": "ic_avatar_kaiju_muto.png", "src": "fandom", "wiki": "godzilla", "title": "M.U.T.O.", "crop": "face"},
    {"id": "ic_avatar_kaiju_skullcrawler.png", "src": "fandom", "wiki": "godzilla", "title": "Skullcrawler", "crop": "face"},

    # 14. SCI-FI (Modern Movie / TV Protagonists - NO Marvel)
    {"id": "ic_avatar_scifi_paul.png", "src": "fandom", "wiki": "dune", "title": "Paul_Atreides", "crop": "face"},
    {"id": "ic_avatar_scifi_chani.png", "src": "fandom", "wiki": "dune", "title": "Chani", "crop": "face"},
    {"id": "ic_avatar_scifi_neo.png", "src": "fandom", "wiki": "matrix", "title": "Neo", "crop": "face"},
    {"id": "ic_avatar_scifi_t800.png", "src": "fandom", "wiki": "terminator", "title": "T-800", "crop": "face"},
    {"id": "ic_avatar_scifi_deckard.png", "src": "fandom", "wiki": "bladerunner", "title": "K", "crop": "face"},
    {"id": "ic_avatar_scifi_ripley.png", "src": "fandom", "wiki": "avp", "title": "Ellen_Ripley", "crop": "face"},
    {"id": "ic_avatar_scifi_xenomorph.png", "src": "fandom", "wiki": "avp", "title": "Xenomorph_XX121_(Alien)", "crop": "face"},
    {"id": "ic_avatar_scifi_cooper.png", "src": "fandom", "wiki": "interstellarfilm", "title": "Joseph_Cooper", "crop": "face"},
    {"id": "ic_avatar_scifi_caesar.png", "src": "fandom", "wiki": "planetoftheapes", "title": "Caesar", "crop": "face"},
    {"id": "ic_avatar_scifi_robocop.png", "src": "fandom", "wiki": "robocop", "title": "Alex_Murphy", "crop": "face"},

    # 15. VIDEO GAMES (Modern AAA Protagonists)
    {"id": "ic_avatar_game_kratos.png", "src": "fandom", "wiki": "godofwar", "title": "Kratos", "crop": "face"},
    {"id": "ic_avatar_game_geralt.png", "src": "fandom", "wiki": "witcher", "title": "Geralt_of_Rivia", "crop": "face"},
    {"id": "ic_avatar_game_chief.png", "src": "fandom", "wiki": "halo", "title": "John-117", "crop": "face"},
    {"id": "ic_avatar_game_arthur.png", "src": "fandom", "wiki": "reddead", "title": "Arthur_Morgan", "crop": "face"},
    {"id": "ic_avatar_game_ellie.png", "src": "fandom", "wiki": "thelastofus", "title": "Ellie_Williams", "crop": "face"},
    {"id": "ic_avatar_game_joel.png", "src": "fandom", "wiki": "thelastofus", "title": "Joel_Miller", "crop": "face"},
    {"id": "ic_avatar_game_jinsakai.png", "src": "fandom", "wiki": "ghostoftsushima", "title": "Jin_Sakai", "crop": "face"},
    {"id": "ic_avatar_game_silverhand.png", "src": "fandom", "wiki": "cyberpunk", "title": "Johnny_Silverhand", "crop": "face"},
    {"id": "ic_avatar_game_cloud.png", "src": "fandom", "wiki": "finalfantasy", "title": "Cloud_Strife", "crop": "face"},
    {"id": "ic_avatar_game_link.png", "src": "fandom", "wiki": "zelda", "title": "Link", "crop": "face"},

    # 16. ANIME (Modern Iconic Anime Protagonists)
    {"id": "ic_avatar_anime_goku.png", "src": "fandom", "wiki": "dragonball", "title": "Goku", "crop": "face"},
    {"id": "ic_avatar_anime_luffy.png", "src": "fandom", "wiki": "onepiece", "title": "Monkey_D._Luffy", "crop": "face"},
    {"id": "ic_avatar_anime_naruto.png", "src": "fandom", "wiki": "naruto", "title": "Naruto_Uzumaki", "crop": "face"},
    {"id": "ic_avatar_anime_ichigo.png", "src": "fandom", "wiki": "bleach", "title": "Ichigo_Kurosaki", "crop": "face"},
    {"id": "ic_avatar_anime_tanjiro.png", "src": "fandom", "wiki": "kimetsu-no-yaiba", "title": "Tanjiro_Kamado", "crop": "face"},
    {"id": "ic_avatar_anime_nezuko.png", "src": "fandom", "wiki": "kimetsu-no-yaiba", "title": "Nezuko_Kamado", "crop": "face"},
    {"id": "ic_avatar_anime_gojo.png", "src": "fandom", "wiki": "jujutsu-kaisen", "title": "Satoru_Gojo", "crop": "face"},
    {"id": "ic_avatar_anime_eren.png", "src": "fandom", "wiki": "shingekinokyojin", "title": "Eren_Yeager", "crop": "face"},
    {"id": "ic_avatar_anime_levi.png", "src": "fandom", "wiki": "shingekinokyojin", "title": "Levi_Ackerman", "crop": "face"},
    {"id": "ic_avatar_anime_saitama.png", "src": "fandom", "wiki": "onepunchman", "title": "Saitama", "crop": "face"},
]

def main():
    print(f"=== CURATING ALL {len(TARGETS)} AVATARS ===")
    total = len(TARGETS)
    ok_count = 0
    
    for idx, item in enumerate(TARGETS, 1):
        filename = item["id"]
        src = item["src"]
        crop = item["crop"]
        
        if src == "skip":
            print(f"[{idx}/{total}] SKIP verified: {filename}")
            ok_count += 1
            continue
            
        print(f"[{idx}/{total}] Fetching {filename} ({src})...")
        img_url = None
        referer = None
        
        if src == "fandom":
            img_url, referer = get_fandom_thumb(item["wiki"], item["title"])
        elif src == "commons_file":
            img_url = get_commons_file_url(item["file"])
        elif src == "commons_search":
            img_url = search_commons_file(item["query"])
            
        if not img_url:
            print(f"  FAILED to find URL for {filename}")
            continue
            
        raw = download_image(img_url, referer)
        if not raw:
            print(f"  FAILED download for {filename}")
            continue
            
        if crop_and_save(raw, filename, crop):
            print(f"  SUCCESS: {filename}")
            ok_count += 1
        else:
            print(f"  FAILED crop for {filename}")

    print(f"\n=== FINISHED CURATION: {ok_count}/{total} OK ===")

if __name__ == "__main__":
    main()
