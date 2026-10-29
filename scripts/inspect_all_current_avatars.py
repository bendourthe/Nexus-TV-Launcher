import os
import PIL.Image

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")

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

total = 0
missing = []
existing = []

for cat, items in CATEGORIES:
    for item in items:
        total += 1
        fn = f"ic_avatar_{item}.png"
        fp = os.path.join(DRAWABLE_DIR, fn)
        if not os.path.exists(fp):
            missing.append(item)
        else:
            sz = os.path.getsize(fp)
            img = PIL.Image.open(fp)
            existing.append((item, img.size, sz))

print(f"Total defined items: {total}")
print(f"Existing items: {len(existing)}")
print(f"Missing items: {len(missing)} -> {missing}")
