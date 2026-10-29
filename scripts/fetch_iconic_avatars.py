import urllib.request
import urllib.parse
import json
import io
import os
import PIL.Image
import PIL.ImageOps

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

# Explicit curated high-resolution sources
SOURCES = {
    # --- SUPERHERO ---
    "ic_avatar_hero_iron.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/6/65/Iron_Man_Vol_7_10_Lee_Variant_Textless.jpg/revision/latest",
        "crop_focus": "top",  # head & upper chest
        "label": "Iron Man"
    },
    "ic_avatar_hero_spider.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/7/71/Peter_Parker_%28Earth-616%29_from_Amazing_Spider-Man_Vol_5_15_Cover.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Spider-Man"
    },
    "ic_avatar_hero_bat.png": {
        "url": "https://static.wikia.nocookie.net/marvel_dc/images/0/08/Batman_Vol_3_131_Textless_Fabok_Variant.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Batman"
    },
    "ic_avatar_hero_solar.png": {
        "url": "https://static.wikia.nocookie.net/marvel_dc/images/0/08/Summer_of_Superman_Special_Vol_1_1_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Superman"
    },
    "ic_avatar_hero_shield.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/b/b1/Captain_America_Vol_6_2_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Captain America"
    },
    "ic_avatar_hero_panther.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/9/99/Black_Panther_Vol_9_5_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Black Panther"
    },
    "ic_avatar_hero_thunder.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/f/f2/Immortal_Thor_Vol_1_4_D%C3%A1vila_Variant_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Thor"
    },
    "ic_avatar_hero_amazon.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/3/35/Wolverine_Vol_8_8_Ope%C3%B1a_Virgin_Variant.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Wolverine"
    },
    "ic_avatar_hero_speed.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/d/d8/Deadpool_Vol_9_1_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Deadpool"
    },
    "ic_avatar_hero_mystic.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/c/c9/Doctor_Strange_Vol_8_1_Magic_Connecting_Variant_Textless.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Doctor Strange"
    },

    # --- SCI-FI ---
    "ic_avatar_scifi_commander.png": {
        "url": "https://static.wikia.nocookie.net/starwars/images/6/67/Darth_Vader_SWE_detail.png/revision/latest",
        "crop_focus": "center",
        "label": "Darth Vader"
    },
    "ic_avatar_scifi_pilot.png": {
        "url": "https://static.wikia.nocookie.net/starwars/images/4/46/DinDjarinArmor-CGSWG.png/revision/latest",
        "crop_focus": "top",
        "label": "The Mandalorian"
    },
    "ic_avatar_scifi_trooper.png": {
        "url": "https://static.wikia.nocookie.net/halo/images/8/84/InfiniteChiefTRANSPARENTholdingReachAR.png/revision/latest",
        "crop_focus": "top",
        "label": "Master Chief"
    },
    "ic_avatar_scifi_hacker.png": {
        "url": "https://static.wikia.nocookie.net/cyberpunk/images/a/af/V_Infobox_CP2077.png/revision/latest",
        "crop_focus": "top",
        "label": "Cyberpunk V"
    },
    "ic_avatar_scifi_cyborg.png": {
        "url": "https://static.wikia.nocookie.net/starwars/images/b/b9/Stormtrooper-ROOCE.png/revision/latest",
        "crop_focus": "top",
        "label": "Stormtrooper"
    },
    "ic_avatar_scifi_bounty.png": {
        "url": "https://static.wikia.nocookie.net/starwars/images/6/62/BobaFettHS-SWI213.png/revision/latest",
        "crop_focus": "center",
        "label": "Boba Fett"
    },
    "ic_avatar_scifi_android.png": {
        "url": "https://static.wikia.nocookie.net/terminator/images/1/1c/Tg-t800-bts-170543-100.jpeg/revision/latest",
        "crop_focus": "center",
        "label": "Terminator T-800"
    },
    "ic_avatar_scifi_alien.png": {
        "url": "https://static.wikia.nocookie.net/avp/images/e/ec/Productionstill03.PNG/revision/latest",
        "crop_focus": "center",
        "label": "Xenomorph"
    },
    "ic_avatar_scifi_drone.png": {
        "url": "https://static.wikia.nocookie.net/starwars/images/2/27/Ahsoka-Tano-AG-2023.png/revision/latest",
        "crop_focus": "top",
        "label": "Ahsoka Tano"
    },
    "ic_avatar_scifi_mech.png": {
        "url": "https://static.wikia.nocookie.net/marveldatabase/images/0/05/Venom_Vol_7_1_Textess.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Venom"
    },

    # --- FANTASY ---
    "ic_avatar_fantasy_dragon.png": {
        "url": "https://static.wikia.nocookie.net/lotr/images/1/1c/Smaug-Crop.JPG/revision/latest",
        "crop_focus": "center",
        "label": "Smaug the Dragon"
    },
    "ic_avatar_fantasy_paladin.png": {
        "url": "https://static.wikia.nocookie.net/witcher/images/5/51/Netflix_geralt_shirt.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Geralt of Rivia"
    },
    "ic_avatar_fantasy_archmage.png": {
        "url": "https://static.wikia.nocookie.net/lotr/images/4/47/Gandalf_by_Damiani.png/revision/latest",
        "crop_focus": "top",
        "label": "Gandalf"
    },
    "ic_avatar_fantasy_rogue.png": {
        "url": "https://static.wikia.nocookie.net/gameofthrones/images/d/d0/JonSnow8x06.PNG/revision/latest",
        "crop_focus": "top",
        "label": "Jon Snow"
    },
    "ic_avatar_fantasy_dwarf.png": {
        "url": "https://static.wikia.nocookie.net/godofwar/images/e/e9/Kratos-_GOW_Ragnarok.png/revision/latest",
        "crop_focus": "top",
        "label": "Kratos"
    },
    "ic_avatar_fantasy_elf.png": {
        "url": "https://static.wikia.nocookie.net/lotr/images/b/be/Legolas%2C_Giancola.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Legolas"
    },
    "ic_avatar_fantasy_valkyrie.png": {
        "url": "https://static.wikia.nocookie.net/gameofthrones/images/4/4f/Daenerys_Season_8.jpg/revision/latest",
        "crop_focus": "top",
        "label": "Daenerys Targaryen"
    },
    "ic_avatar_fantasy_druid.png": {
        "url": "https://static.wikia.nocookie.net/zelda_gamepedia_en/images/4/47/TLoZ_Series_Link_Render.png/revision/latest",
        "crop_focus": "top",
        "label": "Link"
    },
    "ic_avatar_fantasy_griffin.png": {
        "url": "https://static.wikia.nocookie.net/lotr/images/2/2e/Forty-Two.jpg/revision/latest",
        "crop_focus": "center",
        "label": "Gimli"
    },
    "ic_avatar_fantasy_phoenix.png": {
        "url": "https://static.wikia.nocookie.net/gameofthrones/images/1/1f/Night_King_BTW.jpg/revision/latest",
        "crop_focus": "center",
        "label": "Night King"
    },

    # --- PLANETS (10 Official NASA High-Res True-Color Globes) ---
    "ic_avatar_planet_earth.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/The_Earth_seen_from_Apollo_17.jpg/960px-The_Earth_seen_from_Apollo_17.jpg",
        "crop_focus": "center",
        "label": "Planet Earth"
    },
    "ic_avatar_planet_mars.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/OSIRIS_Mars_true_color.jpg/960px-OSIRIS_Mars_true_color.jpg",
        "crop_focus": "center",
        "label": "Mars"
    },
    "ic_avatar_planet_jupiter.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Jupiter_and_its_shrunken_Great_Red_Spot.jpg/960px-Jupiter_and_its_shrunken_Great_Red_Spot.jpg",
        "crop_focus": "center",
        "label": "Jupiter"
    },
    "ic_avatar_planet_saturn.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c7/Saturn_during_Equinox.jpg/960px-Saturn_during_Equinox.jpg",
        "crop_focus": "center",
        "label": "Saturn"
    },
    "ic_avatar_planet_neptune.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/63/Neptune_-_Voyager_2_%2829347980845%29_flatten_crop.jpg/960px-Neptune_-_Voyager_2_%2829347980845%29_flatten_crop.jpg",
        "crop_focus": "center",
        "label": "Neptune"
    },
    "ic_avatar_planet_moon.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/FullMoon2010.jpg/960px-FullMoon2010.jpg",
        "crop_focus": "center",
        "label": "The Moon"
    },
    "ic_avatar_planet_sun.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg/960px-The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg",
        "crop_focus": "center",
        "label": "The Sun"
    },
    "ic_avatar_planet_kepler.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/Venus_globe.jpg/960px-Venus_globe.jpg",
        "crop_focus": "center",
        "label": "Venus"
    },
    "ic_avatar_planet_gliesia.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Uranus2.jpg/960px-Uranus2.jpg",
        "crop_focus": "center",
        "label": "Uranus"
    },
    "ic_avatar_planet_inferno.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7e/Nh-pluto-in-true-color_2x.jpg/960px-Nh-pluto-in-true-color_2x.jpg",
        "crop_focus": "center",
        "label": "Pluto"
    },

    # --- VEHICLES (Official Manufacturer Studio / Aerospace Press) ---
    "ic_avatar_vehicle_supercar.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6f/Bugatti_Chiron%2C_GIMS_2018%2C_Le_Grand-Saconnex_%281X7A1765%29.jpg/960px-Bugatti_Chiron%2C_GIMS_2018%2C_Le_Grand-Saconnex_%281X7A1765%29.jpg",
        "crop_focus": "center",
        "label": "Bugatti Chiron"
    },
    "ic_avatar_vehicle_motorcycle.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/61/2013_McLaren_P1.jpg/960px-2013_McLaren_P1.jpg",
        "crop_focus": "center",
        "label": "McLaren P1"
    },
    "ic_avatar_vehicle_offroad.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/2021_Ferrari_SF90_Stradale_3.jpg/960px-2021_Ferrari_SF90_Stradale_3.jpg",
        "crop_focus": "center",
        "label": "Ferrari SF90"
    },
    "ic_avatar_vehicle_yacht.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Geneva_International_Motor_Show_2018%2C_Le_Grand-Saconnex_%281X7A1486%29.jpg/960px-Geneva_International_Motor_Show_2018%2C_Le_Grand-Saconnex_%281X7A1486%29.jpg",
        "crop_focus": "center",
        "label": "Lamborghini Aventador"
    },
    "ic_avatar_vehicle_rover.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/Porsche_911_GT3_RS%2C_GIMS_2018%2C_Le_Grand-Saconnex_%281X7A0098%29.jpg/960px-Porsche_911_GT3_RS%2C_GIMS_2018%2C_Le_Grand-Saconnex_%281X7A0098%29.jpg",
        "crop_focus": "center",
        "label": "Porsche 911 GT3 RS"
    },
    "ic_avatar_vehicle_jet.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/An_F-22_Raptor_from_the_F-22_Raptor_Demonstration_Team_performs_during_the_2023_Dyess_Big_Country_Air_Fest_at_Dyess_Air_Force_Base%2C_Texas%2C_April_22%2C_2023.jpg/960px-An_F-22_Raptor_from_the_F-22_Raptor_Demonstration_Team_performs_during_the_2023_Dyess_Big_Country_Air_Fest_at_Dyess_Air_Force_Base%2C_Texas%2C_April_22%2C_2023.jpg",
        "crop_focus": "center",
        "label": "F-22 Raptor"
    },
    "ic_avatar_vehicle_helicopter.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/Lockheed_SR-71_Blackbird.jpg/960px-Lockheed_SR-71_Blackbird.jpg",
        "crop_focus": "center",
        "label": "SR-71 Blackbird"
    },
    "ic_avatar_vehicle_spaceshuttle.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c7/Space_Shuttle_Atlantis_launches_from_KSC_on_STS-132.jpg/960px-Space_Shuttle_Atlantis_launches_from_KSC_on_STS-132.jpg",
        "crop_focus": "center",
        "label": "Space Shuttle Atlantis"
    },
    "ic_avatar_vehicle_vessel.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3c/2020_Ducati_Panigale_V4_Superleggera.jpg/960px-2020_Ducati_Panigale_V4_Superleggera.jpg",
        "crop_focus": "center",
        "label": "Ducati Panigale V4"
    },
    "ic_avatar_vehicle_train.png": {
        "url": "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Shinkansen_500_%288086233447%29.jpg/960px-Shinkansen_500_%288086233447%29.jpg",
        "crop_focus": "center",
        "label": "Shinkansen Bullet Train"
    },
}

def process_and_save(target_name, info):
    url = info["url"]
    crop_focus = info.get("crop_focus", "center")
    label = info.get("label", target_name)
    
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
    req = urllib.request.Request(url, headers=headers)
    try:
        data = urllib.request.urlopen(req, timeout=12).read()
        im = PIL.Image.open(io.BytesIO(data)).convert("RGBA")
        w, h = im.size
        
        # Determine square crop region
        square_dim = min(w, h)
        if crop_focus == "top":
            # For character portraits: focus on head and upper body
            crop_dim = min(w, int(h * 0.7))
            left = (w - crop_dim) // 2
            top = int(h * 0.04)
            if top + crop_dim > h:
                top = h - crop_dim
            im_cropped = im.crop((left, top, left + crop_dim, top + crop_dim))
        else:
            # Center crop
            im_cropped = PIL.ImageOps.fit(im, (square_dim, square_dim), method=PIL.Image.Resampling.LANCZOS, centering=(0.5, 0.5))
            
        final_im = im_cropped.resize((320, 320), PIL.Image.Resampling.LANCZOS)
        out_path = os.path.join(OUT_DIR, target_name)
        final_im.save(out_path, format="PNG", optimize=True)
        print(f"[OK] Saved {target_name} ({label})")
        return True
    except Exception as e:
        print(f"[ERROR] Failed {target_name} ({label}): {e}")
        return False

if __name__ == "__main__":
    print(f"Downloading and generating {len(SOURCES)} professional avatar images...")
    success = 0
    for target, info in SOURCES.items():
        if process_and_save(target, info):
            success += 1
    print(f"\nCompleted: {success}/{len(SOURCES)} successfully processed.")
