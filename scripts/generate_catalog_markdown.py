import os
import shutil

CATEGORIES = [
    ("Landscapes", [
        ("land_fuji", "Mount Fuji", "Lake Kawaguchiko with cherry blossoms and Mount Fuji"),
        ("land_aurora", "Aurora Borealis", "Vibrant green aurora borealis over Tromso fjord"),
        ("land_yosemite", "Yosemite Valley", "Tunnel View granite monoliths at golden hour"),
        ("land_matterhorn", "Matterhorn", "Alpine pyramid reflecting in Riffelsee lake"),
        ("land_amalfi", "Amalfi Coast", "Cliffside pastel village of Positano above Mediterranean"),
        ("land_grandcanyon", "Grand Canyon", "Horseshoe Bend curve glowing at crimson sunset"),
        ("land_maldives", "Maldives Lagoon", "Turquoise tropical lagoon and coral reef waters"),
        ("land_sahara", "Sahara Dunes", "Erg Chebbi wind-sculpted sand dunes at dusk"),
        ("land_patagonia", "Torres del Paine", "Granite towers of Torres del Paine above azure lake"),
        ("land_bali", "Bali Terraces", "Emerald green Tegalalang rice terraces at sunrise")
    ]),
    ("Landmarks", [
        ("landmark_eiffel", "Eiffel Tower", "Iconic iron lattice tower in Paris"),
        ("landmark_tajmahal", "Taj Mahal", "Ivory-white marble mausoleum in Agra"),
        ("landmark_pyramids", "Great Pyramids", "Ancient pyramids of Giza under blue sky"),
        ("landmark_cntower", "CN Tower", "Toronto communications and observation needle"),
        ("landmark_liberty", "Statue of Liberty", "Colossal neoclassical sculpture in New York Harbor"),
        ("landmark_colosseum", "The Colosseum", "Ancient Roman amphitheatre in central Rome"),
        ("landmark_bigben", "Big Ben", "Elizabeth Tower clock tower at Palace of Westminster"),
        ("landmark_goldengate", "Golden Gate Bridge", "International orange suspension bridge in San Francisco"),
        ("landmark_operahouse", "Sydney Opera House", "Multi-venue performing arts centre in Sydney Harbour"),
        ("landmark_machupicchu", "Machu Picchu", "15th-century Inca citadel on mountain ridge in Peru")
    ]),
    ("Cities", [
        ("city_nyc", "New York City", "Manhattan skyline at twilight"),
        ("city_tokyo", "Tokyo", "Tokyo Tower and glittering metropolitan skyline"),
        ("city_paris", "Paris", "City of Light rooftops and Eiffel Tower skyline"),
        ("city_london", "London", "River Thames, Tower Bridge, and city lights"),
        ("city_dubai", "Dubai", "Burj Khalifa rising above gleaming desert skyline"),
        ("city_rome", "Rome", "St. Peter's Basilica dome and historic city skyline"),
        ("city_hongkong", "Hong Kong", "Victoria Harbour skyscrapers and glowing neon lights"),
        ("city_sanfrancisco", "San Francisco", "Hills, bay view, and city architecture"),
        ("city_singapore", "Singapore", "Marina Bay Sands and illuminated modern skyline"),
        ("city_chicago", "Chicago", "Loop skyscrapers along Michigan Avenue and lakefront")
    ]),
    ("Space", [
        ("space_pillars", "Pillars of Creation", "Star-forming pillars in Eagle Nebula by Hubble"),
        ("space_carina", "Carina Nebula", "Cosmic Cliffs stellar nursery captured by JWST"),
        ("space_andromeda", "Andromeda Galaxy", "Majestic spiral galaxy neighbor in deep space"),
        ("space_ringnebula", "Ring Nebula", "Planetary nebula gas shroud captured by JWST"),
        ("space_orion", "Orion Nebula", "Massive interstellar nursery glowing in ultraviolet"),
        ("space_crab", "Crab Nebula", "Supernova remnant expanding filament cloud"),
        ("space_sombrero", "Sombrero Galaxy", "Brilliant white bulb with dark dust lane rim"),
        ("space_whirlpool", "Whirlpool Galaxy", "Grand design spiral galaxy interacting with neighbor"),
        ("space_blackhole", "Black Hole M87", "Event Horizon Telescope portrait of supermassive core"),
        ("space_eclipse", "Total Solar Eclipse", "Diamond ring effect and shimmering solar corona")
    ]),
    ("Planets", [
        ("planet_earth", "Planet Earth", "Apollo 17 Blue Marble view of our home planet"),
        ("planet_mars", "Mars", "The red planet captured in true color by OSIRIS"),
        ("planet_jupiter", "Jupiter", "Cassini true color view of Great Red Spot"),
        ("planet_saturn", "Saturn", "Cassini equinox portrait with majestic rings"),
        ("planet_neptune", "Neptune", "Voyager 2 true color portrait of the azure ice giant"),
        ("planet_moon", "The Moon", "High-resolution lunar surface in twilight space"),
        ("planet_sun", "The Sun", "NASA SDO AIA 304 extreme ultraviolet solar sphere"),
        ("planet_kepler", "Venus", "NASA Magellan and Mariner global portrait of Venus"),
        ("planet_gliesia", "Uranus", "NASA Voyager 2 true color portrait of the cyan giant"),
        ("planet_inferno", "Pluto", "NASA New Horizons high-res portrait with heart region")
    ]),
    ("Science", [
        ("sci_einstein", "Albert Einstein", "Theoretical physicist and creator of relativity"),
        ("sci_curie", "Marie Curie", "Pioneering physicist and chemist in radioactivity"),
        ("sci_newton", "Isaac Newton", "Mathematician, physicist, and discoverer of gravity"),
        ("sci_tesla", "Nikola Tesla", "Inventor and electrical engineer of AC electrical systems"),
        ("sci_hawking", "Stephen Hawking", "Theoretical physicist and cosmologist on black holes"),
        ("sci_sagan", "Carl Sagan", "Astronomer, planetary scientist, and science communicator"),
        ("sci_galileo", "Galileo Galilei", "Astronomer and father of observational astronomy"),
        ("sci_darwin", "Charles Darwin", "Naturalist and geologist pioneer of evolutionary biology"),
        ("sci_dna", "DNA Double Helix", "3D molecular visualization of the genetic code"),
        ("sci_quantum", "Quantum Atom", "Luminous atomic orbitals and quantum energy states")
    ]),
    ("Animals", [
        ("animal_lion", "Male Lion", "Majestic golden male lion with full mane"),
        ("animal_tiger", "Bengal Tiger", "Powerful tiger face with vivid amber eyes"),
        ("animal_leopard", "Snow Leopard", "Stealthy snow leopard with piercing blue eyes"),
        ("animal_wolf", "Arctic Wolf", "Noble white wolf in northern snowy wilderness"),
        ("animal_bear", "Polar Bear", "Apex Arctic polar bear in snow portrait"),
        ("animal_fox", "Red Fox", "Crimson red fox in snowy winter landscape"),
        ("animal_eagle", "Bald Eagle", "Soaring bald eagle with intense golden eye"),
        ("animal_owl", "Barn Owl", "Wise nocturnal barn owl perched in shadow"),
        ("animal_panda", "Giant Panda", "Gentle giant panda enjoying fresh bamboo"),
        ("animal_gorilla", "Silverback Gorilla", "Wise mountain silverback gorilla portrait")
    ]),
    ("Pets", [
        ("pet_retriever", "Golden Retriever", "Friendly golden retriever smiling happily"),
        ("pet_frenchie", "French Bulldog", "Playful French bulldog with alert bat ears"),
        ("pet_husky", "Siberian Husky", "Striking husky with icy blue eyes"),
        ("pet_germanshepherd", "German Shepherd", "Loyal German shepherd dog with alert ears"),
        ("pet_corgi", "Pembroke Welsh Corgi", "Cheerful Pembroke Welsh corgi smiling face"),
        ("pet_mainecoon", "Maine Coon Cat", "Regal Maine Coon cat with lynx-like tufts"),
        ("pet_britishshorthair", "British Shorthair", "Plush blue British shorthair with copper eyes"),
        ("pet_bengal", "Bengal Cat", "Exotic leopard-spotted Bengal cat with green eyes"),
        ("pet_hamster", "Golden Hamster", "Cute fluffy hamster holding sunflower seed"),
        ("pet_cockatiel", "Cockatiel", "Yellow-crested cockatiel with orange cheek patches")
    ]),
    ("Cars", [
        ("car_ferrari", "Ferrari SF90", "1000 HP plug-in hybrid Italian supercar"),
        ("car_porsche", "Porsche 911 GT3 RS", "Track-honed naturally aspirated German sports car"),
        ("car_lambo", "Lamborghini Revuelto", "V12 hybrid high performance Italian flagship"),
        ("car_bugatti", "Bugatti Chiron", "1500 HP quad-turbo French hypercar"),
        ("car_mclaren", "McLaren P1", "Twin-turbo V8 hybrid British hypercar"),
        ("car_aston", "Aston Martin Valkyrie", "F1-inspired 6.5L V12 British hypercar"),
        ("car_fordgt", "Ford GT", "Twin-turbo EcoBoost American carbon supercar"),
        ("car_koenigsegg", "Koenigsegg Jesko", "1600 HP megacar with active aero wing"),
        ("car_corvette", "Corvette Z06", "Flat-plane crank American supercar"),
        ("car_amgone", "Mercedes-AMG ONE", "Formula 1 powertrain for the road")
    ]),
    ("Music", [
        ("music_daftpunk", "Daft Punk", "Electronic music helmets in chrome and gold"),
        ("music_weeknd", "The Weeknd", "Abel Tesfaye in After Hours red suit portrait"),
        ("music_taylorswift", "Taylor Swift", "Pop superstar live in concert on the Eras Tour"),
        ("music_billie", "Billie Eilish", "Alternative pop singer-songwriter studio portrait"),
        ("music_eminem", "Eminem", "Marshall Mathers legendary rap icon portrait"),
        ("music_freddie", "Freddie Mercury", "Legendary Queen frontman at Live Aid 1985"),
        ("music_bowie", "David Bowie", "Aladdin Sane lightning bolt face makeup"),
        ("music_jimi", "Jimi Hendrix", "Psychedelic guitar virtuoso in concert"),
        ("music_michael", "Michael Jackson", "King of Pop in iconic fedora hat"),
        ("music_drake", "Drake", "Global hip-hop chart topper studio portrait")
    ]),
    ("Pop Culture", [
        ("pop_godfather", "Don Vito Corleone", "Marlon Brando in tuxedo with red rose from The Godfather"),
        ("pop_barbie", "Barbie", "Margot Robbie in pink gingham dress from Barbie"),
        ("pop_oppenheimer", "J. Robert Oppenheimer", "Cillian Murphy in fedora and pipe from Oppenheimer"),
        ("pop_marilyn", "Marilyn Monroe", "Classic Hollywood studio portrait of Marilyn Monroe"),
        ("pop_elvis", "Elvis Presley", "The King of Rock and Roll in 1968 comeback special"),
        ("pop_indy", "Indiana Jones", "Harrison Ford with brown fedora and leather jacket"),
        ("pop_wednesday", "Wednesday Addams", "Jenna Ortega in black Nevermore uniform with braids"),
        ("pop_wonka", "Willy Wonka", "Gene Wilder in velvet plum coat and top hat"),
        ("pop_topgun", "Maverick Pete Mitchell", "Tom Cruise in aviator sunglasses and flight jacket"),
        ("pop_ghostbusters", "Peter Venkman", "Bill Murray in Ghostbusters jumpsuit with proton pack")
    ]),
    ("Abstract", [
        ("abstract_iridescent", "Iridescent Glass", "3D liquid rainbow glass sphere on obsidian dark"),
        ("abstract_goldblack", "Liquid Gold", "Molten liquid gold swirling through black marble"),
        ("abstract_chromesphere", "Chrome Sphere", "Liquid mercury mirror sphere reflecting studio lights"),
        ("abstract_synthwave", "Synthwave", "Retro 80s neon grid with glowing horizon sun"),
        ("abstract_amethyst", "Amethyst Geode", "Vibrant purple crystalline quartz mineral slice"),
        ("abstract_prism", "Prism Light", "Crystal glass prism scattering spectrum rainbow"),
        ("abstract_silk", "Metallic Silk", "Fluid dynamic velvet silk ribbon twisting in 3D"),
        ("abstract_quantum", "Quantum Vortex", "Luminous cyan and magenta particle energy field"),
        ("abstract_kintsugi", "Kintsugi", "Cracked Japanese porcelain repaired with pure gold"),
        ("abstract_cyberpunk", "Cyber Wave", "Cyberpunk neon fiber wave in turquoise and magenta")
    ]),
    ("Marvel", [
        ("marvel_ironman", "Iron Man", "Tony Stark in high-tech Mark armor helmet"),
        ("marvel_captain", "Captain America", "Steve Rogers with vibranium star shield"),
        ("marvel_thor", "Thor", "Norse God of Thunder wielding Mjolnir"),
        ("marvel_spiderman", "Spider-Man", "Peter Parker in modern cinematic web suit"),
        ("marvel_panther", "Black Panther", "T'Challa King of Wakanda in vibranium habit"),
        ("marvel_strange", "Doctor Strange", "Stephen Strange Master of the Mystic Arts"),
        ("marvel_thanos", "Thanos", "The Mad Titan warlord of Titan"),
        ("marvel_loki", "Loki", "God of Mischief and Lord of Stories"),
        ("marvel_deadpool", "Deadpool", "Wade Wilson the Merc with a Mouth"),
        ("marvel_wolverine", "Wolverine", "Logan with adamantium claws in yellow suit")
    ]),
    ("DC Comics", [
        ("dc_batman", "Batman", "Bruce Wayne the Dark Knight of Gotham in cowl"),
        ("dc_superman", "Superman", "Clark Kent the Man of Steel in cinematic S-shield suit"),
        ("dc_wonderwoman", "Wonder Woman", "Diana Prince Amazon warrior of Themyscira in tiara"),
        ("dc_joker", "The Joker", "The Clown Prince of Crime in cinematic makeup"),
        ("dc_peacemaker", "Peacemaker", "Christopher Smith in chrome beacon helmet"),
        ("dc_harley", "Harley Quinn", "Maid of Mischief in cinematic anti-hero gear"),
        ("dc_aquaman", "Aquaman", "Arthur Curry King of Atlantis in scale armor"),
        ("dc_flash", "The Flash", "Barry Allen the fastest man alive in crimson cowl"),
        ("dc_blackadam", "Black Adam", "Teth-Adam champion of Kahndaq"),
        ("dc_bane", "Bane", "The masked revolutionary in tactical vest")
    ]),
    ("Star Wars", [
        ("sw_vader", "Darth Vader", "Sith Lord and Commander of the Imperial Fleet in black helmet"),
        ("sw_mando", "The Mandalorian", "Din Djarin in shining beskar armor helmet"),
        ("sw_grogu", "Grogu", "Force-sensitive youngling with large expressive ears"),
        ("sw_ahsoka", "Ahsoka Tano", "Former Jedi padawan with white markings and striped lekku"),
        ("sw_obiwan", "Obi-Wan Kenobi", "Jedi Master in brown robes and beard"),
        ("sw_bobafett", "Boba Fett", "Legendary bounty hunter in green Mandalorian helmet"),
        ("sw_maul", "Darth Maul", "Zabrak Sith Lord with red and black facial tattoos"),
        ("sw_kyloren", "Kylo Ren", "Supreme Leader of the First Order in crossguard helmet"),
        ("sw_stormtrooper", "Stormtrooper", "Imperial shock trooper in pristine white helmet"),
        ("sw_luke", "Luke Skywalker", "Jedi Knight hero of the Rebel Alliance in black tunic")
    ]),
    ("Lord of the Rings", [
        ("lotr_aragorn", "Aragorn", "King Elessar rightful heir of Isildur with Elven brooch"),
        ("lotr_gandalf", "Gandalf the White", "Wise wizard of Middle-earth with staff"),
        ("lotr_legolas", "Legolas", "Prince of the Woodland Realm Elven archer"),
        ("lotr_gimli", "Gimli", "Son of Gloin warrior dwarf with iron helmet and braided beard"),
        ("lotr_frodo", "Frodo Baggins", "Ring-bearer Hobbit with Elven cloak and green brooch"),
        ("lotr_gollum", "Gollum", "Corrupted creature consumed by the One Ring"),
        ("lotr_sauron", "Sauron", "The Dark Lord in spiked black battle armor and helm"),
        ("lotr_galadriel", "Galadriel", "Lady of Light and ruler of Lothlorien in silver tiara"),
        ("lotr_sam", "Samwise Gamgee", "Brave and loyal companion of the Fellowship"),
        ("lotr_witchking", "Witch-king", "Lord of the Nazgul with towering spiked iron crown")
    ]),
    ("Harry Potter", [
        ("hp_harry", "Harry Potter", "Gryffindor seeker with round glasses and lightning bolt scar"),
        ("hp_hermione", "Hermione Granger", "Brightest witch of her age in Hogwarts robes"),
        ("hp_ron", "Ron Weasley", "Loyal Gryffindor friend with red hair"),
        ("hp_dumbledore", "Albus Dumbledore", "Hogwarts Headmaster with silver beard and half-moon spectacles"),
        ("hp_snape", "Severus Snape", "Potions Master in black frock coat and dark hair"),
        ("hp_voldemort", "Lord Voldemort", "The Dark Lord with pale serpentine face and slit pupils"),
        ("hp_sirius", "Sirius Black", "Padfoot prisoner of Azkaban in velvet coat"),
        ("hp_hagrid", "Rubeus Hagrid", "Keeper of Keys and Grounds with bushy beard and moleskin coat"),
        ("hp_draco", "Draco Malfoy", "Slytherin student with slicked platinum blond hair"),
        ("hp_dobby", "Dobby", "Free house-elf with large green tennis-ball eyes and bat ears")
    ]),
    ("Game of Thrones", [
        ("got_jonsnow", "Jon Snow", "King in the North in black fur cloak and Valyrian steel sword"),
        ("got_daenerys", "Daenerys Targaryen", "Mother of Dragons with braided platinum silver hair"),
        ("got_tyrion", "Tyrion Lannister", "Hand of the Queen in Hand brooch and dark doublet"),
        ("got_arya", "Arya Stark", "Faceless assassin of Winterfell in northern leather tunic"),
        ("got_cersei", "Cersei Lannister", "Queen of the Seven Kingdoms in royal gold embroidery"),
        ("got_jaime", "Jaime Lannister", "Kingslayer in golden Lannister armor"),
        ("got_nightking", "Night King", "Leader of the White Walkers with icy blue eyes and horned crown"),
        ("got_nedstark", "Ned Stark", "Lord of Winterfell and Warden of the North with broadsword"),
        ("got_brienne", "Brienne of Tarth", "Knight of the Seven Kingdoms in heavy iron plate armor"),
        ("got_drogon", "Drogon", "Giant black dragon with crimson horns and fiery glowing eyes")
    ]),
    ("Kaijus", [
        ("kaiju_godzilla", "Godzilla", "King of the Monsters centered head roar with blue dorsal glow"),
        ("kaiju_kong", "King Kong", "Protector of Skull Island centered snarling facial portrait"),
        ("kaiju_ghidorah", "King Ghidorah", "Lit three-headed golden lightning dragon front view"),
        ("kaiju_mothra", "Mothra", "Queen of the Monsters radiant facial portrait with blue compound eyes"),
        ("kaiju_rodan", "Rodan", "Fire demon bird of prey centered beak and fiery volcanic eyes"),
        ("kaiju_mechagodzilla", "Mechagodzilla", "Apex cybernetic titan polished metallic head with red visor"),
        ("kaiju_shimo", "Shimo", "Ancient frost titan crystalline ice dragon head portrait"),
        ("kaiju_skarking", "Skar King", "Red-furred tyrant ape centered snarling face"),
        ("kaiju_muto", "MUTO", "Electromagnetic parasitic titan triangular head with crimson visor"),
        ("kaiju_skullcrawler", "Skullcrawler", "Subterranean predator with bone-white skull head and yellow eyes")
    ]),
    ("Sci-Fi", [
        ("scifi_paul", "Paul Atreides", "Muad'Dib Duke of Arrakis with blue-in-blue spice eyes"),
        ("scifi_chani", "Chani", "Fremen warrior and desert guide on Arrakis with nose filter"),
        ("scifi_neo", "Neo", "Keanu Reeves in black sunglasses and trenchcoat from The Matrix"),
        ("scifi_t800", "Terminator T-800", "Cyberdyne Model 101 polished chrome skull with glowing red eye"),
        ("scifi_spock", "Spock", "Leonard Nimoy in classic blue Starfleet uniform with Vulcan pointed ears"),
        ("scifi_jakesully", "Jake Sully", "Toruk Makto Na'vi warrior with bioluminescent blue skin and gold eyes"),
        ("scifi_xenomorph", "Xenomorph", "Apex biomechanical alien with elongated black dome and silver metallic teeth"),
        ("scifi_marty", "Marty McFly", "Michael J. Fox in iconic red puffer vest and denim jacket from Back to the Future"),
        ("scifi_caesar", "Caesar", "Andy Serkis modern reboot CGI ape leader with intelligent hazel eyes"),
        ("scifi_robocop", "RoboCop", "Peter Weller in titanium silver helmet and dark visor")
    ]),
    ("Video Games", [
        ("game_kratos", "Kratos", "Ghost of Sparta God of War with full beard and crimson tattoo"),
        ("game_geralt", "Geralt of Rivia", "The White Wolf monster slayer with cat eyes and silver hair (CDPR render)"),
        ("game_chief", "Master Chief", "Spartan-117 in Mjolnir Mark VI green armor and gold visor"),
        ("game_arthur", "Arthur Morgan", "Senior outlaw of Van der Linde gang in leather hat and neckerchief"),
        ("game_ellie", "Ellie", "Immune survivor with eyebrow scar from The Last of Us Part II"),
        ("game_joel", "Joel Miller", "Hardened survivor with graying beard from The Last of Us Part I"),
        ("game_jinsakai", "Jin Sakai", "The Ghost of Tsushima with black armored ghost mask"),
        ("game_silverhand", "Johnny Silverhand", "Keanu Reeves rockerboy rebel with aviator sunglasses and cyberarm"),
        ("game_cloud", "Cloud Strife", "Ex-SOLDIER mercenary with spiky blond hair and mako blue eyes"),
        ("game_link", "Link", "Hero of Time courageous champion with blond hair and Champion tunic")
    ]),
    ("Anime", [
        ("anime_goku", "Son Goku", "Saiyan martial artist defender of Earth with iconic black hair"),
        ("anime_luffy", "Monkey D. Luffy", "Captain of Straw Hat Pirates with red ribbon straw hat and scar"),
        ("anime_naruto", "Naruto Uzumaki", "Seventh Hokage with Hidden Leaf forehead protector and whiskered cheeks"),
        ("anime_ichigo", "Ichigo Kurosaki", "Substitute Soul Reaper with vibrant orange spiky hair and black robes"),
        ("anime_tanjiro", "Tanjiro Kamado", "Demon Slayer with forehead scar, burgundy hair, and Hanafuda earrings"),
        ("anime_nezuko", "Nezuko Kamado", "Demon sister with bamboo mouth muzzle and pink eyes"),
        ("anime_gojo", "Satoru Gojo", "Special Grade Jujutsu sorcerer with white hair and blindfold/Six Eyes"),
        ("anime_eren", "Eren Yeager", "Survey Corps soldier with determined emerald eyes and Survey Corps jacket"),
        ("anime_levi", "Levi Ackerman", "Humanity's strongest soldier Captain Levi with black undercut and cravat"),
        ("anime_saitama", "Saitama", "One Punch Man hero for fun in yellow suit with white cape")
    ])
]

doc = """# Android TV Clean Launcher - Avatar Guidelines & Master Catalog

This document defines the quality standards, technical specifications, and complete 220-item catalog across all 22 categories for the Clean TV Launcher avatar system.

---

## 1. Avatar Design Standards

To ensure every avatar looks crisp, iconic, and immediately recognizable from across the living room on a 4K or 1080p television, all avatar assets must meet the following criteria:

### A. Style & Representation
* **Authentic Modern Representations**: Use authentic movie stills from modern theatrical releases, official high-resolution 3D game character renders (e.g., CD Projekt Red, Sony Santa Monica, 343 Industries), or canonical historical portraits.
* **Strictly No Synthetic AI Images**: Never use AI-generated faces or approximations. Characters must be the authentic actors or canonical studio models (e.g., Michael J. Fox as Marty McFly, Leonard Nimoy as Spock, Sam Worthington as Jake Sully, Andy Serkis as Caesar).
* **No Outdated Retro Art / Vintage Comics**: Avoid 1960s newsprint comic drawings, low-resolution animated TV screen grabs, or generic illustrations. Characters should look modern, sleek, and premium.

### B. Composition & Framing
* **Aspect Ratio**: Exact **1:1 square** canvas.
* **Centered Head & Bust**: The character's face, facial features, and signature upper-bust elements (e.g., Marty's red puffer vest, Spider-Man's chest spider, Wolverine's yellow cowl, Spock's blue Starfleet collar) must be dead-centered horizontally and vertically.
* **Crucial Top Headroom (15–20%)**: Android TV renders avatars inside a circular mask (`BitmapShader`). If a photo is cropped tightly to the forehead or hair, the circular border will cut off the character's hair, hat, bat ears, crown, or horns. Maintain approximately **18–24 pixels of headroom** at the top of a 320×320 image.
* **Gaze Direction**: Front-facing or a subtle 3/4 turn towards the viewer works best. Avoid extreme side profiles, silhouettes, or rear views where facial expressions and eyes cannot be distinguished.

### C. Quality, Lighting & Contrast
* **Contrast Against Dark UI**: The launcher background is a dark obsidian/slate theme (`#12151D`). Avatars must have strong edge contrast and vibrant internal lighting so they pop off the screen.
* **No Muddy Silhouettes**: Avoid characters lost in dense fog, yellow smoke, or dark shadows (such as monsters or night scenes). Lighting should crisply illuminate the eyes, jaws, teeth, and textures.
* **Clean Backgrounds**: Prefer a transparent background (PNG alpha) or a clean, solid neutral dark background (`#12151D` or `#161922`) that blends seamlessly into the avatar circle border.

### D. Technical Specifications
* **Resolution**: **320 × 320 px** (standard) or **512 × 512 px** (ultra-high density).
* **Format**: Portable Network Graphics (**PNG**), 32-bit RGBA or 24-bit RGB, optimized compression.
* **Naming Convention**: `ic_avatar_<id>.png` (strictly lowercase, alphanumeric and underscores only).
* **Target Directory in Repository**:  
  `launcher/app/src/main/res/drawable/`

---

## 2. Master Catalog (22 Categories, 220 Items)

The categories below appear in the exact order configured in `WatchNextManager.java` and `MainActivity.java`.
"""

for cat_idx, (cat_name, items) in enumerate(CATEGORIES, start=1):
    doc += f"\n### {cat_idx}. {cat_name} (10 items)\n\n"
    doc += "| # | Item ID | Character / Subject | Description | Target Filename |\n"
    doc += "| :-: | :--- | :--- | :--- | :--- |\n"
    for item_idx, (item_id, item_label, item_desc) in enumerate(items, start=1):
        filename = f"ic_avatar_{item_id}.png"
        doc += f"| {item_idx} | `{item_id}` | **{item_label}** | {item_desc} | `{filename}` |\n"

doc += """
---

## 3. How to Update Avatars

1. **Direct File Drop**:
   Save any replacement square PNG directly to:
   `launcher/app/src/main/res/drawable/ic_avatar_<id>.png`

2. **Automated Verification**:
   Run the visual audit script to generate contact sheets comparing the square assets against the circular TV crop:
   ```bash
   python scripts/audit_and_grade_all_220.py
   ```
   Audit sheets are generated in `launcher/build/audit_<category>.png`.

3. **Build & Deploy**:
   Build the release APK and install onto the Android TV device:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/build_launcher.ps1
   adb -s 10.0.0.169:5555 install -r launcher/build/clean-launcher-release.apk
   ```
"""

# Write to local repo
local_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "docs", "AVATAR_GUIDELINES_AND_CATALOG.md")
with open(local_path, "w", encoding="utf-8") as f:
    f.write(doc)
print(f"Wrote local doc: {local_path} ({len(doc)} chars)")

# Copy to G:\My Drive\temp
gdrive_dir = r"G:\My Drive\temp"
if os.path.exists(gdrive_dir):
    gdrive_path = os.path.join(gdrive_dir, "AVATAR_GUIDELINES_AND_CATALOG.md")
    shutil.copyfile(local_path, gdrive_path)
    print(f"Copied to Google Drive: {gdrive_path}")
else:
    print(f"Warning: Google Drive directory {gdrive_dir} not found.")
