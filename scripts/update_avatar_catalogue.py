import os
import re

JAVA_FILE = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "java", "com", "cleantv", "launcher", "WatchNextManager.java")
MAIN_ACTIVITY = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "java", "com", "cleantv", "launcher", "MainActivity.java")

AVATAR_ENTRIES = [
    # 1. Landscapes
    ("land_fuji", "Mount Fuji", "Landscapes", "Lake Kawaguchiko with cherry blossoms and Mount Fuji", "R.drawable.ic_avatar_land_fuji"),
    ("land_aurora", "Aurora", "Landscapes", "Vibrant green aurora borealis over Tromso fjord", "R.drawable.ic_avatar_land_aurora"),
    ("land_yosemite", "Yosemite", "Landscapes", "Tunnel View granite monoliths at golden hour", "R.drawable.ic_avatar_land_yosemite"),
    ("land_matterhorn", "Matterhorn", "Landscapes", "Alpine pyramid reflecting in Riffelsee lake", "R.drawable.ic_avatar_land_matterhorn"),
    ("land_amalfi", "Amalfi Coast", "Landscapes", "Cliffside pastel village of Positano above Mediterranean", "R.drawable.ic_avatar_land_amalfi"),
    ("land_grandcanyon", "Grand Canyon", "Landscapes", "Horseshoe Bend curve glowing at crimson sunset", "R.drawable.ic_avatar_land_grandcanyon"),
    ("land_maldives", "Maldives", "Landscapes", "Turquoise tropical lagoon and coral reef waters", "R.drawable.ic_avatar_land_maldives"),
    ("land_sahara", "Sahara", "Landscapes", "Erg Chebbi wind-sculpted sand dunes at dusk", "R.drawable.ic_avatar_land_sahara"),
    ("land_patagonia", "Patagonia", "Landscapes", "Granite towers of Torres del Paine above azure lake", "R.drawable.ic_avatar_land_patagonia"),
    ("land_bali", "Bali Terraces", "Landscapes", "Emerald green Tegalalang rice terraces at sunrise", "R.drawable.ic_avatar_land_bali"),

    # 2. Space
    ("space_pillars", "Pillars of Creation", "Space", "Iconic star-forming pillars in Eagle Nebula by Hubble", "R.drawable.ic_avatar_space_pillars"),
    ("space_carina", "Carina Nebula", "Space", "Cosmic Cliffs stellar nursery captured by JWST", "R.drawable.ic_avatar_space_carina"),
    ("space_andromeda", "Andromeda", "Space", "Majestic spiral galaxy neighbor in deep space", "R.drawable.ic_avatar_space_andromeda"),
    ("space_ringnebula", "Ring Nebula", "Space", "Planetary nebula gas shroud captured by JWST", "R.drawable.ic_avatar_space_ringnebula"),
    ("space_orion", "Orion Nebula", "Space", "Massive interstellar nursery glowing in ultraviolet", "R.drawable.ic_avatar_space_orion"),
    ("space_crab", "Crab Nebula", "Space", "Supernova remnant expanding filament cloud", "R.drawable.ic_avatar_space_crab"),
    ("space_sombrero", "Sombrero Galaxy", "Space", "Brilliant white bulb with dark dust lane rim", "R.drawable.ic_avatar_space_sombrero"),
    ("space_whirlpool", "Whirlpool Galaxy", "Space", "Grand design spiral galaxy interacting with neighbor", "R.drawable.ic_avatar_space_whirlpool"),
    ("space_blackhole", "Black Hole M87", "Space", "Event Horizon Telescope portrait of supermassive core", "R.drawable.ic_avatar_space_blackhole"),
    ("space_eclipse", "Solar Eclipse", "Space", "Total solar eclipse diamond ring and shimmering corona", "R.drawable.ic_avatar_space_eclipse"),

    # 3. Planets
    ("planet_earth", "Planet Earth", "Planets", "Apollo 17 Blue Marble view of our home planet", "R.drawable.ic_avatar_planet_earth"),
    ("planet_mars", "Mars", "Planets", "The red planet captured in true color by OSIRIS", "R.drawable.ic_avatar_planet_mars"),
    ("planet_jupiter", "Jupiter", "Planets", "Cassini true color view of Great Red Spot", "R.drawable.ic_avatar_planet_jupiter"),
    ("planet_saturn", "Saturn", "Planets", "Cassini equinox portrait with majestic rings", "R.drawable.ic_avatar_planet_saturn"),
    ("planet_neptune", "Neptune", "Planets", "Voyager 2 true color portrait of the azure ice giant", "R.drawable.ic_avatar_planet_neptune"),
    ("planet_moon", "The Moon", "Planets", "High-resolution lunar surface in twilight space", "R.drawable.ic_avatar_planet_moon"),
    ("planet_sun", "The Sun", "Planets", "NASA SDO AIA 304 extreme ultraviolet solar sphere", "R.drawable.ic_avatar_planet_sun"),
    ("planet_kepler", "Venus", "Planets", "NASA Magellan and Mariner global portrait of Venus", "R.drawable.ic_avatar_planet_kepler"),
    ("planet_gliesia", "Uranus", "Planets", "NASA Voyager 2 true color portrait of the cyan giant", "R.drawable.ic_avatar_planet_gliesia"),
    ("planet_inferno", "Pluto", "Planets", "NASA New Horizons high-res portrait with heart region", "R.drawable.ic_avatar_planet_inferno"),

    # 4. Animals
    ("animal_lion", "Lion", "Animals", "Majestic golden male lion with full mane", "R.drawable.ic_avatar_animal_lion"),
    ("animal_tiger", "Bengal Tiger", "Animals", "Powerful tiger face with vivid amber eyes", "R.drawable.ic_avatar_animal_tiger"),
    ("animal_leopard", "Snow Leopard", "Animals", "Stealthy snow leopard with piercing blue eyes", "R.drawable.ic_avatar_animal_leopard"),
    ("animal_wolf", "Arctic Wolf", "Animals", "Noble white wolf in northern snowy wilderness", "R.drawable.ic_avatar_animal_wolf"),
    ("animal_bear", "Polar Bear", "Animals", "Apex Arctic polar bear in snow portrait", "R.drawable.ic_avatar_animal_bear"),
    ("animal_fox", "Red Fox", "Animals", "Crimson red fox in snowy winter landscape", "R.drawable.ic_avatar_animal_fox"),
    ("animal_eagle", "Bald Eagle", "Animals", "Soaring bald eagle with intense golden eye", "R.drawable.ic_avatar_animal_eagle"),
    ("animal_owl", "Barn Owl", "Animals", "Wise nocturnal barn owl perched in shadow", "R.drawable.ic_avatar_animal_owl"),
    ("animal_panda", "Giant Panda", "Animals", "Gentle giant panda enjoying fresh bamboo", "R.drawable.ic_avatar_animal_panda"),
    ("animal_gorilla", "Silverback Gorilla", "Animals", "Wise mountain silverback gorilla portrait", "R.drawable.ic_avatar_animal_gorilla"),

    # 5. Pets
    ("pet_retriever", "Golden Retriever", "Pets", "Friendly golden retriever smiling happily", "R.drawable.ic_avatar_pet_retriever"),
    ("pet_frenchie", "French Bulldog", "Pets", "Playful French bulldog with alert bat ears", "R.drawable.ic_avatar_pet_frenchie"),
    ("pet_husky", "Siberian Husky", "Pets", "Striking husky with icy blue eyes", "R.drawable.ic_avatar_pet_husky"),
    ("pet_germanshepherd", "German Shepherd", "Pets", "Loyal German shepherd dog with alert ears", "R.drawable.ic_avatar_pet_germanshepherd"),
    ("pet_corgi", "Welsh Corgi", "Pets", "Cheerful Pembroke Welsh corgi smiling face", "R.drawable.ic_avatar_pet_corgi"),
    ("pet_mainecoon", "Maine Coon", "Pets", "Regal Maine Coon cat with lynx-like tufts", "R.drawable.ic_avatar_pet_mainecoon"),
    ("pet_britishshorthair", "British Shorthair", "Pets", "Plush blue British shorthair with copper eyes", "R.drawable.ic_avatar_pet_britishshorthair"),
    ("pet_bengal", "Bengal Cat", "Pets", "Exotic leopard-spotted Bengal cat with green eyes", "R.drawable.ic_avatar_pet_bengal"),
    ("pet_hamster", "Syrian Hamster", "Pets", "Cute fluffy hamster holding sunflower seed", "R.drawable.ic_avatar_pet_hamster"),
    ("pet_cockatiel", "Cockatiel", "Pets", "Yellow-crested cockatiel with orange cheek patches", "R.drawable.ic_avatar_pet_cockatiel"),

    # 6. Cars
    ("car_ferrari", "Ferrari SF90", "Cars", "1000 HP plug-in hybrid Italian supercar", "R.drawable.ic_avatar_car_ferrari"),
    ("car_porsche", "Porsche 911 GT3 RS", "Cars", "Track-honed naturally aspirated German sports car", "R.drawable.ic_avatar_car_porsche"),
    ("car_lambo", "Lamborghini Revuelto", "Cars", "V12 hybrid high performance Italian flagship", "R.drawable.ic_avatar_car_lambo"),
    ("car_bugatti", "Bugatti Chiron", "Cars", "1500 HP quad-turbo French hypercar", "R.drawable.ic_avatar_car_bugatti"),
    ("car_mclaren", "McLaren P1", "Cars", "Twin-turbo V8 hybrid British hypercar", "R.drawable.ic_avatar_car_mclaren"),
    ("car_aston", "Aston Martin Valkyrie", "Cars", "F1-inspired 6.5L V12 British hypercar", "R.drawable.ic_avatar_car_aston"),
    ("car_fordgt", "Ford GT", "Cars", "Twin-turbo EcoBoost American carbon supercar", "R.drawable.ic_avatar_car_fordgt"),
    ("car_koenigsegg", "Koenigsegg Jesko", "Cars", "1600 HP megacar with active aero wing", "R.drawable.ic_avatar_car_koenigsegg"),
    ("car_corvette", "Corvette Z06", "Cars", "Flat-plane crank American supercar", "R.drawable.ic_avatar_car_corvette"),
    ("car_amgone", "Mercedes-AMG ONE", "Cars", "Formula 1 powertrain for the road", "R.drawable.ic_avatar_car_amgone"),

    # 7. Music
    ("music_daftpunk", "Daft Punk", "Music", "Iconic electronic music helmets in chrome and gold", "R.drawable.ic_avatar_music_daftpunk"),
    ("music_weeknd", "The Weeknd", "Music", "Abel Tesfaye in After Hours red suit portrait", "R.drawable.ic_avatar_music_weeknd"),
    ("music_taylorswift", "Taylor Swift", "Music", "Pop superstar live in concert on the Eras Tour", "R.drawable.ic_avatar_music_taylorswift"),
    ("music_billie", "Billie Eilish", "Music", "Alternative pop singer-songwriter studio portrait", "R.drawable.ic_avatar_music_billie"),
    ("music_eminem", "Eminem", "Music", "Marshall Mathers legendary rap icon portrait", "R.drawable.ic_avatar_music_eminem"),
    ("music_freddie", "Freddie Mercury", "Music", "Legendary Queen frontman at Live Aid 1985", "R.drawable.ic_avatar_music_freddie"),
    ("music_bowie", "David Bowie", "Music", "Aladdin Sane lightning bolt face makeup", "R.drawable.ic_avatar_music_bowie"),
    ("music_jimi", "Jimi Hendrix", "Music", "Psychedelic guitar virtuoso in concert", "R.drawable.ic_avatar_music_jimi"),
    ("music_michael", "Michael Jackson", "Music", "King of Pop in iconic fedora hat", "R.drawable.ic_avatar_music_michael"),
    ("music_drake", "Drake", "Music", "Global hip-hop chart topper studio portrait", "R.drawable.ic_avatar_music_drake"),

    # 8. Abstract
    ("abstract_iridescent", "Iridescent Glass", "Abstract", "3D liquid rainbow glass sphere on obsidian dark", "R.drawable.ic_avatar_abstract_iridescent"),
    ("abstract_goldblack", "Liquid Gold", "Abstract", "Molten liquid gold swirling through black marble", "R.drawable.ic_avatar_abstract_goldblack"),
    ("abstract_chromesphere", "Chrome Sphere", "Abstract", "Liquid mercury mirror sphere reflecting studio lights", "R.drawable.ic_avatar_abstract_chromesphere"),
    ("abstract_synthwave", "Synthwave", "Abstract", "Retro 80s neon grid with glowing horizon sun", "R.drawable.ic_avatar_abstract_synthwave"),
    ("abstract_amethyst", "Amethyst Geode", "Abstract", "Vibrant purple crystalline quartz mineral slice", "R.drawable.ic_avatar_abstract_amethyst"),
    ("abstract_prism", "Prism Light", "Abstract", "Crystal glass prism scattering spectrum rainbow", "R.drawable.ic_avatar_abstract_prism"),
    ("abstract_silk", "Metallic Silk", "Abstract", "Fluid dynamic velvet silk ribbon twisting in 3D", "R.drawable.ic_avatar_abstract_silk"),
    ("abstract_quantum", "Quantum Vortex", "Abstract", "Luminous cyan and magenta particle energy field", "R.drawable.ic_avatar_abstract_quantum"),
    ("abstract_kintsugi", "Kintsugi", "Abstract", "Cracked Japanese porcelain repaired with pure gold", "R.drawable.ic_avatar_abstract_kintsugi"),
    ("abstract_cyberpunk", "Cyber Wave", "Abstract", "Cyberpunk neon fiber wave in turquoise and magenta", "R.drawable.ic_avatar_abstract_cyberpunk"),

    # 9. Marvel
    ("marvel_ironman", "Iron Man", "Marvel", "Tony Stark in high-tech Mark armor helmet", "R.drawable.ic_avatar_marvel_ironman"),
    ("marvel_captain", "Captain America", "Marvel", "Steve Rogers with vibranium star shield", "R.drawable.ic_avatar_marvel_captain"),
    ("marvel_thor", "Thor", "Marvel", "Norse God of Thunder wielding Mjolnir", "R.drawable.ic_avatar_marvel_thor"),
    ("marvel_spiderman", "Spider-Man", "Marvel", "Peter Parker in modern cinematic web suit", "R.drawable.ic_avatar_marvel_spiderman"),
    ("marvel_panther", "Black Panther", "Marvel", "T'Challa King of Wakanda in vibranium habit", "R.drawable.ic_avatar_marvel_panther"),
    ("marvel_strange", "Doctor Strange", "Marvel", "Stephen Strange Master of the Mystic Arts", "R.drawable.ic_avatar_marvel_strange"),
    ("marvel_thanos", "Thanos", "Marvel", "The Mad Titan warlord of Titan", "R.drawable.ic_avatar_marvel_thanos"),
    ("marvel_loki", "Loki", "Marvel", "God of Mischief and Lord of Stories", "R.drawable.ic_avatar_marvel_loki"),
    ("marvel_deadpool", "Deadpool", "Marvel", "Wade Wilson the Merc with a Mouth", "R.drawable.ic_avatar_marvel_deadpool"),
    ("marvel_wolverine", "Wolverine", "Marvel", "Logan with adamantium claws in yellow suit", "R.drawable.ic_avatar_marvel_wolverine"),

    # 10. DC Comics
    ("dc_batman", "Batman", "DC Comics", "Bruce Wayne the Dark Knight of Gotham", "R.drawable.ic_avatar_dc_batman"),
    ("dc_superman", "Superman", "DC Comics", "Clark Kent the Man of Steel", "R.drawable.ic_avatar_dc_superman"),
    ("dc_wonderwoman", "Wonder Woman", "DC Comics", "Diana Prince Amazon warrior of Themyscira", "R.drawable.ic_avatar_dc_wonderwoman"),
    ("dc_joker", "Joker", "DC Comics", "The Clown Prince of Crime in Gotham City", "R.drawable.ic_avatar_dc_joker"),
    ("dc_peacemaker", "Peacemaker", "DC Comics", "Christopher Smith in chrome beacon helmet", "R.drawable.ic_avatar_dc_peacemaker"),
    ("dc_harley", "Harley Quinn", "DC Comics", "Maid of Mischief in cinematic anti-hero gear", "R.drawable.ic_avatar_dc_harley"),
    ("dc_aquaman", "Aquaman", "DC Comics", "Arthur Curry King of Atlantis", "R.drawable.ic_avatar_dc_aquaman"),
    ("dc_flash", "The Flash", "DC Comics", "Barry Allen the fastest man alive", "R.drawable.ic_avatar_dc_flash"),
    ("dc_blackadam", "Black Adam", "DC Comics", "Teth-Adam champion of Kahndaq", "R.drawable.ic_avatar_dc_blackadam"),
    ("dc_bane", "Bane", "DC Comics", "The masked revolutionary who broke the Bat", "R.drawable.ic_avatar_dc_bane"),

    # 11. Star Wars
    ("sw_vader", "Darth Vader", "Star Wars", "Sith Lord and Commander of the Imperial Fleet", "R.drawable.ic_avatar_sw_vader"),
    ("sw_mando", "The Mandalorian", "Star Wars", "Din Djarin in shining beskar armor", "R.drawable.ic_avatar_sw_mando"),
    ("sw_grogu", "Grogu", "Star Wars", "Force-sensitive youngling foundling", "R.drawable.ic_avatar_sw_grogu"),
    ("sw_ahsoka", "Ahsoka Tano", "Star Wars", "Former Jedi padawan wielding dual white sabers", "R.drawable.ic_avatar_sw_ahsoka"),
    ("sw_obiwan", "Obi-Wan Kenobi", "Star Wars", "Jedi Master and guardian of the Republic", "R.drawable.ic_avatar_sw_obiwan"),
    ("sw_bobafett", "Boba Fett", "Star Wars", "Legendary bounty hunter and Daimyo of Mos Espa", "R.drawable.ic_avatar_sw_bobafett"),
    ("sw_maul", "Darth Maul", "Star Wars", "Zabrak Sith Lord wielding dual red blades", "R.drawable.ic_avatar_sw_maul"),
    ("sw_kyloren", "Kylo Ren", "Star Wars", "Supreme Leader of the First Order", "R.drawable.ic_avatar_sw_kyloren"),
    ("sw_stormtrooper", "Stormtrooper", "Star Wars", "Imperial shock trooper in clean white armor", "R.drawable.ic_avatar_sw_stormtrooper"),
    ("sw_luke", "Luke Skywalker", "Star Wars", "Jedi Knight hero of the Rebel Alliance", "R.drawable.ic_avatar_sw_luke"),

    # 12. Lord of the Rings
    ("lotr_aragorn", "Aragorn", "Lord of the Rings", "King Elessar rightful heir of Isildur", "R.drawable.ic_avatar_lotr_aragorn"),
    ("lotr_gandalf", "Gandalf", "Lord of the Rings", "Gandalf the White wizard of Middle-earth", "R.drawable.ic_avatar_lotr_gandalf"),
    ("lotr_legolas", "Legolas", "Lord of the Rings", "Prince of the Woodland Realm master archer", "R.drawable.ic_avatar_lotr_legolas"),
    ("lotr_gimli", "Gimli", "Lord of the Rings", "Son of Gloin warrior of the Lonely Mountain", "R.drawable.ic_avatar_lotr_gimli"),
    ("lotr_frodo", "Frodo Baggins", "Lord of the Rings", "Ring-bearer who traveled to the fires of Mordor", "R.drawable.ic_avatar_lotr_frodo"),
    ("lotr_gollum", "Gollum", "Lord of the Rings", "Corrupted creature consumed by the One Ring", "R.drawable.ic_avatar_lotr_gollum"),
    ("lotr_sauron", "Sauron", "Lord of the Rings", "The Dark Lord in spiked battle armor", "R.drawable.ic_avatar_lotr_sauron"),
    ("lotr_galadriel", "Galadriel", "Lord of the Rings", "Lady of Light and ruler of Lothlorien", "R.drawable.ic_avatar_lotr_galadriel"),
    ("lotr_sam", "Samwise Gamgee", "Lord of the Rings", "Brave and loyal companion of the Fellowship", "R.drawable.ic_avatar_lotr_sam"),
    ("lotr_witchking", "Witch-king", "Lord of the Rings", "Lord of the Nazgul with iron flail and crown", "R.drawable.ic_avatar_lotr_witchking"),

    # 13. Kaijus
    ("kaiju_godzilla", "Godzilla", "Kaijus", "King of the Monsters with atomic breath", "R.drawable.ic_avatar_kaiju_godzilla"),
    ("kaiju_kong", "King Kong", "Kaijus", "Protector of Skull Island and Hollow Earth", "R.drawable.ic_avatar_kaiju_kong"),
    ("kaiju_ghidorah", "King Ghidorah", "Kaijus", "Three-headed dragon of golden lightning", "R.drawable.ic_avatar_kaiju_ghidorah"),
    ("kaiju_mothra", "Mothra", "Kaijus", "Queen of the Monsters radiant titan goddess", "R.drawable.ic_avatar_kaiju_mothra"),
    ("kaiju_rodan", "Rodan", "Kaijus", "Fire demon of the volcanic skies", "R.drawable.ic_avatar_kaiju_rodan"),
    ("kaiju_mechagodzilla", "Mechagodzilla", "Kaijus", "Cybernetic apex titan built for destruction", "R.drawable.ic_avatar_kaiju_mechagodzilla"),
    ("kaiju_shimo", "Shimo", "Kaijus", "Ancient frost titan queen of the Ice Age", "R.drawable.ic_avatar_kaiju_shimo"),
    ("kaiju_skarking", "Skar King", "Kaijus", "Tyrannical Hollow Earth ape overlord", "R.drawable.ic_avatar_kaiju_skarking"),
    ("kaiju_muto", "MUTO", "Kaijus", "Massive electromagnetic parasitic titan", "R.drawable.ic_avatar_kaiju_muto"),
    ("kaiju_skullcrawler", "Skullcrawler", "Kaijus", "Subterranean apex predator of Skull Island", "R.drawable.ic_avatar_kaiju_skullcrawler"),

    # 14. Sci-Fi
    ("scifi_paul", "Paul Atreides", "Sci-Fi", "Muad'Dib Duke of Arrakis with blue-in-blue eyes", "R.drawable.ic_avatar_scifi_paul"),
    ("scifi_chani", "Chani", "Sci-Fi", "Fremen warrior and desert guide on Arrakis", "R.drawable.ic_avatar_scifi_chani"),
    ("scifi_neo", "Neo", "Sci-Fi", "Thomas Anderson The One in the Matrix", "R.drawable.ic_avatar_scifi_neo"),
    ("scifi_t800", "Terminator T-800", "Sci-Fi", "Cyberdyne Model 101 chrome combat chassis", "R.drawable.ic_avatar_scifi_t800"),
    ("scifi_deckard", "Officer K", "Sci-Fi", "Nexus-9 Blade Runner in 2049 Los Angeles", "R.drawable.ic_avatar_scifi_deckard"),
    ("scifi_ripley", "Ellen Ripley", "Sci-Fi", "Warrant Officer and Weyland-Yutani survivor", "R.drawable.ic_avatar_scifi_ripley"),
    ("scifi_xenomorph", "Xenomorph", "Sci-Fi", "Apex biomechanical extraterrestrial predator", "R.drawable.ic_avatar_scifi_xenomorph"),
    ("scifi_cooper", "Cooper", "Sci-Fi", "Endurance astronaut venturing beyond the wormhole", "R.drawable.ic_avatar_scifi_cooper"),
    ("scifi_caesar", "Caesar", "Sci-Fi", "Ape leader who founded the new civilization", "R.drawable.ic_avatar_scifi_caesar"),
    ("scifi_robocop", "RoboCop", "Sci-Fi", "Alex Murphy titanium cyborg protector of Detroit", "R.drawable.ic_avatar_scifi_robocop"),

    # 15. Video Games
    ("game_kratos", "Kratos", "Video Games", "Ghost of Sparta God of War", "R.drawable.ic_avatar_game_kratos"),
    ("game_geralt", "Geralt of Rivia", "Video Games", "The White Wolf legendary monster slayer", "R.drawable.ic_avatar_game_geralt"),
    ("game_chief", "Master Chief", "Video Games", "Spartan-117 savior of humanity in Mjolnir armor", "R.drawable.ic_avatar_game_chief"),
    ("game_arthur", "Arthur Morgan", "Video Games", "Senior outlaw of the Van der Linde gang", "R.drawable.ic_avatar_game_arthur"),
    ("game_ellie", "Ellie", "Video Games", "Immune survivor in the post-cordyceps world", "R.drawable.ic_avatar_game_ellie"),
    ("game_joel", "Joel Miller", "Video Games", "Hardened survivor and father figure", "R.drawable.ic_avatar_game_joel"),
    ("game_jinsakai", "Jin Sakai", "Video Games", "The Ghost of Tsushima honorable samurai", "R.drawable.ic_avatar_game_jinsakai"),
    ("game_silverhand", "Johnny Silverhand", "Video Games", "Rockerboy rebel legend of Night City", "R.drawable.ic_avatar_game_silverhand"),
    ("game_cloud", "Cloud Strife", "Video Games", "Ex-SOLDIER mercenary wielding the Buster Sword", "R.drawable.ic_avatar_game_cloud"),
    ("game_link", "Link", "Video Games", "Hero of Time courageous champion of Hyrule", "R.drawable.ic_avatar_game_link"),

    # 16. Anime
    ("anime_goku", "Son Goku", "Anime", "Saiyan martial artist defender of Earth", "R.drawable.ic_avatar_anime_goku"),
    ("anime_luffy", "Monkey D. Luffy", "Anime", "Captain of the Straw Hat Pirates seeking One Piece", "R.drawable.ic_avatar_anime_luffy"),
    ("anime_naruto", "Naruto Uzumaki", "Anime", "Seventh Hokage hero of the Hidden Leaf", "R.drawable.ic_avatar_anime_naruto"),
    ("anime_ichigo", "Ichigo Kurosaki", "Anime", "Substitute Soul Reaper wielding Zangetsu", "R.drawable.ic_avatar_anime_ichigo"),
    ("anime_tanjiro", "Tanjiro Kamado", "Anime", "Demon Slayer practitioner of Sun Breathing", "R.drawable.ic_avatar_anime_tanjiro"),
    ("anime_nezuko", "Nezuko Kamado", "Anime", "Demon sister with bamboo muzzle and pink eyes", "R.drawable.ic_avatar_anime_nezuko"),
    ("anime_gojo", "Satoru Gojo", "Anime", "Special Grade Jujutsu sorcerer with Six Eyes", "R.drawable.ic_avatar_anime_gojo"),
    ("anime_eren", "Eren Yeager", "Anime", "Survey Corps soldier and holder of Attack Titan", "R.drawable.ic_avatar_anime_eren"),
    ("anime_levi", "Levi Ackerman", "Anime", "Humanity's strongest soldier Captain Levi", "R.drawable.ic_avatar_anime_levi"),
    ("anime_saitama", "Saitama", "Anime", "One Punch Man hero for fun", "R.drawable.ic_avatar_anime_saitama"),
]

def update_watch_next_manager():
    with open(JAVA_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    lines = []
    lines.append("    public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays.asList(")
    current_cat = None
    for item in AVATAR_ENTRIES:
        cid, label, cat, desc, res = item
        if cat != current_cat:
            current_cat = cat
            lines.append(f"            // --- Category: {cat} (10 items) ---")
        lines.append(f'            new AvatarCharacter("{cid}", "{label}", "{cat}", "{desc}", {res}),')
    
    # replace last comma with semicolon on closing
    lines[-1] = lines[-1].rstrip(",")
    lines.append("    );")
    
    new_block = "\n".join(lines)
    
    pattern = re.compile(r'    public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays\.asList\(.*?\n    \);', re.DOTALL)
    if not pattern.search(content):
        print("ERROR: Could not find AVATAR_CHARACTERS block in WatchNextManager.java")
        return False
        
    content = pattern.sub(new_block, content, count=1)
    
    # Also ensure default for ben returns "marvel_ironman" or "animal_lion"
    with open(JAVA_FILE, "w", encoding="utf-8") as f:
        f.write(content)
        
    print(f"Successfully updated WatchNextManager.java with {len(AVATAR_ENTRIES)} avatars across 16 categories.")
    return True

def update_main_activity():
    with open(MAIN_ACTIVITY, "r", encoding="utf-8") as f:
        content = f.read()

    # Exact 16 categories in requested sequence
    new_categories = """        // Group into 16 distinct horizontal category rows (10 items each = 160 avatars total)
        String[] categories = new String[]{
                "Landscapes",
                "Space",
                "Planets",
                "Animals",
                "Pets",
                "Cars",
                "Music",
                "Abstract",
                "Marvel",
                "DC Comics",
                "Star Wars",
                "Lord of the Rings",
                "Kaijus",
                "Sci-Fi",
                "Video Games",
                "Anime"
        };"""

    pattern = re.compile(r'        // Group into.*?String\[\] categories = new String\[\]\{.*?\};', re.DOTALL)
    if not pattern.search(content):
        print("ERROR: Could not find categories array block in MainActivity.java")
        return False

    content = pattern.sub(new_categories, content, count=1)

    with open(MAIN_ACTIVITY, "w", encoding="utf-8") as f:
        f.write(content)

    print("Successfully updated MainActivity.java categories array.")
    return True

if __name__ == "__main__":
    if update_watch_next_manager() and update_main_activity():
        print("ALL UPDATES COMPLETED SUCCESSFULLY!")
    else:
        sys.exit(1)
