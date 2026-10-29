import os
import io
import PIL.Image
import PIL.ImageDraw
import PIL.ImageFont

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
BUILD_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build")
os.makedirs(BUILD_DIR, exist_ok=True)

CATEGORIES = [
    ("Landscapes", ["land_fuji", "land_aurora", "land_yosemite", "land_matterhorn", "land_amalfi", "land_grandcanyon", "land_maldives", "land_sahara", "land_patagonia", "land_bali"]),
    ("Landmarks", ["landmark_eiffel", "landmark_tajmahal", "landmark_pyramids", "landmark_cntower", "landmark_liberty", "landmark_colosseum", "landmark_bigben", "landmark_goldengate", "landmark_operahouse", "landmark_machupicchu"]),
    ("Cities", ["city_nyc", "city_tokyo", "city_paris", "city_london", "city_dubai", "city_rome", "city_hongkong", "city_sanfrancisco", "city_singapore", "city_chicago"]),
    ("Space", ["space_pillars", "space_carina", "space_andromeda", "space_ringnebula", "space_orion", "space_crab", "space_sombrero", "space_whirlpool", "space_blackhole", "space_eclipse"]),
    ("Planets", ["planet_earth", "planet_mars", "planet_jupiter", "planet_saturn", "planet_neptune", "planet_moon", "planet_sun", "planet_kepler", "planet_gliesia", "planet_inferno"]),
    ("Animals", ["animal_lion", "animal_tiger", "animal_leopard", "animal_wolf", "animal_bear", "animal_fox", "animal_eagle", "animal_owl", "animal_panda", "animal_gorilla"]),
    ("Pets", ["pet_retriever", "pet_frenchie", "pet_husky", "pet_germanshepherd", "pet_corgi", "pet_mainecoon", "pet_britishshorthair", "pet_bengal", "pet_hamster", "pet_cockatiel"]),
    ("Cars", ["car_ferrari", "car_porsche", "car_lambo", "car_bugatti", "car_mclaren", "car_aston", "car_fordgt", "car_koenigsegg", "car_corvette", "car_amgone"]),
    ("Music", ["music_daftpunk", "music_weeknd", "music_taylorswift", "music_billie", "music_eminem", "music_freddie", "music_bowie", "music_jimi", "music_michael", "music_drake"]),
    ("Abstract", ["abstract_iridescent", "abstract_goldblack", "abstract_chromesphere", "abstract_synthwave", "abstract_amethyst", "abstract_prism", "abstract_silk", "abstract_quantum", "abstract_kintsugi", "abstract_cyberpunk"]),
    ("Marvel", ["marvel_ironman", "marvel_captain", "marvel_thor", "marvel_spiderman", "marvel_panther", "marvel_strange", "marvel_thanos", "marvel_loki", "marvel_deadpool", "marvel_wolverine"]),
    ("DC Comics", ["dc_batman", "dc_superman", "dc_wonderwoman", "dc_joker", "dc_peacemaker", "dc_harley", "dc_aquaman", "dc_flash", "dc_blackadam", "dc_bane"]),
    ("Star Wars", ["sw_vader", "sw_mando", "sw_grogu", "sw_ahsoka", "sw_obiwan", "sw_bobafett", "sw_maul", "sw_kyloren", "sw_stormtrooper", "sw_luke"]),
    ("Lord of the Rings", ["lotr_aragorn", "lotr_gandalf", "lotr_legolas", "lotr_gimli", "lotr_frodo", "lotr_gollum", "lotr_sauron", "lotr_galadriel", "lotr_sam", "lotr_witchking"]),
    ("Kaijus", ["kaiju_godzilla", "kaiju_kong", "kaiju_ghidorah", "kaiju_mothra", "kaiju_rodan", "kaiju_mechagodzilla", "kaiju_shimo", "kaiju_skarking", "kaiju_muto", "kaiju_skullcrawler"]),
    ("Sci-Fi", ["scifi_paul", "scifi_chani", "scifi_neo", "scifi_t800", "scifi_deckard", "scifi_ripley", "scifi_xenomorph", "scifi_cooper", "scifi_caesar", "scifi_robocop"]),
    ("Video Games", ["game_kratos", "game_geralt", "game_chief", "game_arthur", "game_ellie", "game_joel", "game_jinsakai", "game_silverhand", "game_cloud", "game_link"]),
    ("Anime", ["anime_goku", "anime_luffy", "anime_naruto", "anime_ichigo", "anime_tanjiro", "anime_nezuko", "anime_gojo", "anime_eren", "anime_levi", "anime_saitama"]),
]

def render_category_sheet(cat_name, items, out_path):
    # 10 items, each rendered as:
    # Top: square original (140x140)
    # Bottom: circular preview (140x140) as seen on TV
    # Underneath: item ID label
    tile_w = 150
    tile_h = 320
    pad = 20
    canvas_w = pad * 2 + 10 * tile_w
    canvas_h = pad * 2 + tile_h + 40
    
    canvas = PIL.Image.new("RGB", (canvas_w, canvas_h), (18, 20, 28))
    draw = PIL.ImageDraw.Draw(canvas)
    
    # Title
    draw.text((pad, pad // 2), f"Category Audit: {cat_name} (Top: Square asset, Bottom: TV Circular Crop)", fill=(240, 240, 250))
    
    mask = PIL.Image.new("L", (140, 140), 0)
    draw_m = PIL.ImageDraw.Draw(mask)
    draw_m.ellipse((0, 0, 140, 140), fill=255)
    
    for i, item_id in enumerate(items):
        file_path = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
        x = pad + i * tile_w + 5
        y = pad + 40
        
        if os.path.exists(file_path):
            img = PIL.Image.open(file_path).convert("RGBA")
            img_sq = img.resize((140, 140), PIL.Image.LANCZOS)
            
            # Paste square
            canvas.paste(img_sq, (x, y), mask=img_sq)
            
            # Draw square border
            draw.rectangle((x, y, x + 140, y + 140), outline=(80, 85, 105), width=1)
            
            # Circular preview
            circ_y = y + 150
            circ = PIL.Image.new("RGBA", (140, 140), (0, 0, 0, 0))
            circ.paste(img_sq, (0, 0), mask=mask)
            canvas.paste(circ, (x, circ_y), mask=circ)
            draw.ellipse((x, circ_y, x + 140, circ_y + 140), outline=(255, 255, 255, 120), width=2)
            
            # Label
            draw.text((x, circ_y + 145), item_id.replace(f"{item_id.split('_')[0]}_", ""), fill=(180, 185, 200))
        else:
            draw.text((x, y + 50), "MISSING", fill=(255, 80, 80))
            
    canvas.save(out_path)
    print(f"Saved audit sheet: {out_path}")

for cat_name, items in CATEGORIES:
    clean_cat = cat_name.lower().replace(" ", "_")
    out_file = os.path.join(BUILD_DIR, f"audit_{clean_cat}.png")
    render_category_sheet(cat_name, items, out_file)

print("All category sheets generated successfully.")
