import os
import io
import base64
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
OUT_HTML = os.path.join(os.path.dirname(__file__), "..", "launcher", "build", "avatars_grid.html")

CATEGORIES = [
    ("Landscapes", ["land_fuji", "land_aurora", "land_yosemite", "land_matterhorn", "land_amalfi", "land_grandcanyon", "land_maldives", "land_sahara", "land_patagonia", "land_bali"]),
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

# Create preview image compilation
total_rows = len(CATEGORIES)
canvas = PIL.Image.new("RGB", (10 * 100 + 40, total_rows * 110 + 40), (16, 18, 27))

for row_idx, (cat_name, items) in enumerate(CATEGORIES):
    for col_idx, item_id in enumerate(items):
        file_path = os.path.join(DRAWABLE_DIR, f"ic_avatar_{item_id}.png")
        if os.path.exists(file_path):
            img = PIL.Image.open(file_path).convert("RGB")
            thumb = img.resize((90, 90), PIL.Image.LANCZOS)
            x = 20 + col_idx * 100
            y = 20 + row_idx * 110
            canvas.paste(thumb, (x, y))

out_img = os.path.join(os.path.dirname(__file__), "..", "launcher", "build", "all_160_avatars_preview.png")
canvas.save(out_img)
print("Saved all 160 avatars preview to:", out_img)
