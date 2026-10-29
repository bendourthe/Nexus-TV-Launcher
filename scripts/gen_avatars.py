import os

cat_map = {
    '01_Landscapes': 'Landscapes',
    '02_Cities': 'Cities',
    '03_Space': 'Space',
    '04_Animals': 'Animals',
    '05_Cars': 'Cars',
    '06_Pop_Culture': 'Pop Culture',
    '07_Superheros': 'Superheroes',
    '08_Sci-Fi': 'Sci-Fi',
    '09_Monsters': 'Monsters',
    '10_Abstract': 'Abstract'
}

friendly_names = {
    # 01 Landscapes
    'land_amalfi': ('Amalfi Coast', 'Sun-drenched cliffs of Positano'),
    'land_aurora': ('Aurora Borealis', 'Vibrant northern lights in night sky'),
    'land_bali': ('Bali Terraces', 'Lush tropical emerald rice terraces'),
    'land_fuji': ('Mount Fuji', 'Majestic snow-capped volcanic peak'),
    'land_grandcanyon': ('Grand Canyon', 'Vast red rock canyon panorama'),
    'land_maldives': ('Maldives Lagoon', 'Crystal turquoise atoll waters'),
    'land_matterhorn': ('Matterhorn', 'Iconic jagged alpine mountain summit'),
    'land_patagonia': ('Torres del Paine', 'Dramatic granite horns and glacial lakes'),
    'land_sahara': ('Sahara Dunes', 'Golden rolling desert sand dunes'),
    'land_yosemite': ('Yosemite Valley', 'Towering granite cliffs and misty pines'),

    # 02 Cities
    'city_dubai': ('Dubai', 'Futuristic skyline and Burj Khalifa'),
    'city_hongkong': ('Hong Kong', 'Neon-lit Victoria Harbour skyline'),
    'city_lasvegas': ('Las Vegas', 'Dazzling illuminated casino strip'),
    'city_london': ('London', 'Thames riverfront and historic skyline'),
    'city_nyc': ('New York City', 'Iconic Manhattan towers at twilight'),
    'city_paris': ('Paris', 'Haussmann boulevards and Parisian charm'),
    'city_rome': ('Rome', 'Ancient eternal city architecture'),
    'city_sanfrancisco': ('San Francisco', 'Foggy bay and Victorian hills'),
    'city_singapore': ('Singapore', 'Lush Marina Bay architectural skyline'),
    'city_toronto': ('Toronto', 'Lakefront skyline and bustling city center'),
    'landmark_bigben': ('Big Ben', 'Gothic Westminster clock tower in London'),
    'landmark_cntower': ('CN Tower', 'Iconic Toronto telecommunication spire'),
    'landmark_colosseum': ('The Colosseum', 'Ancient Roman gladiatorial amphitheatre'),
    'landmark_eiffel': ('Eiffel Tower', 'Wrought-iron Parisian masterpiece'),
    'landmark_goldengate': ('Golden Gate Bridge', 'Suspension bridge across San Francisco bay'),
    'landmark_liberty': ('Statue of Liberty', 'Beacon of freedom in New York Harbor'),
    'landmark_machupicchu': ('Machu Picchu', 'Incan citadel nestled in Andean peaks'),
    'landmark_operahouse': ('Sydney Opera House', 'Geometric sail-like harbour landmark'),
    'landmark_pyramids': ('Great Pyramids', 'Ancient wonders of Giza plateau'),
    'landmark_tajmahal': ('Taj Mahal', 'Ivory-white marble mausoleum of Agra'),

    # 03 Space
    'planet_earth': ('Planet Earth', 'Blue marble with swirling clouds'),
    'planet_gliesia': ('Ice Giant', 'Deep azure frozen gaseous atmosphere'),
    'planet_inferno': ('Lava World', 'Molten volcanic exoplanet surface'),
    'planet_jupiter': ('Jupiter', 'Great red spot and gas giant storm bands'),
    'planet_kepler': ('Goldilocks World', 'Alien terrestrial habitable exoplanet'),
    'planet_mars': ('Mars', 'Crimson rust deserts and polar ice caps'),
    'planet_moon': ('The Moon', 'Detailed lunar cratered surface'),
    'planet_neptune': ('Neptune', 'Deep ultramarine celestial sphere'),
    'planet_saturn': ('Saturn', 'Magnificent golden rings in orbit'),
    'planet_sun': ('The Sun', 'Fiery solar flares and coronal loops'),
    'space_andromeda': ('Andromeda Galaxy', 'Majestic spiral galaxy neighbor'),
    'space_blackhole': ('Black Hole', 'Event horizon warping celestial light'),
    'space_carina': ('Carina Nebula', 'Cosmic cliffs of star creation'),
    'space_crab': ('Crab Nebula', 'Pulsar supernova remnant in deep space'),
    'space_eclipse': ('Total Solar Eclipse', 'Radiant solar corona shining behind moon'),
    'space_orion': ('Orion Nebula', 'Stellar nursery glowing in interstellar dust'),
    'space_pillars': ('Pillars of Creation', 'Towering columns of gas and dust'),
    'space_ringnebula': ('Ring Nebula', 'Glowing colorful planetary nebula torus'),
    'space_sombrero': ('Sombrero Galaxy', 'Bright nucleus with broad dust ring'),
    'space_whirlpool': ('Whirlpool Galaxy', 'Grand design interacting spiral galaxy'),

    # 04 Animals
    'animal_bear': ('Grizzly Bear', 'Powerful wild grizzly portrait'),
    'animal_eagle': ('Bald Eagle', 'Fierce golden gaze of predatory raptor'),
    'animal_fox': ('Red Fox', 'Cunning scarlet coat in woodland snow'),
    'animal_gorilla': ('Silverback Gorilla', 'Wise and dominant mountain primate'),
    'animal_leopard': ('Snow Leopard', 'Ghost of the mountains with spotted coat'),
    'animal_lion': ('Male Lion', 'Majestic golden mane of the savanna king'),
    'animal_owl': ('Great Horned Owl', 'Intense watchful night predator'),
    'animal_panda': ('Giant Panda', 'Playful gentle bamboo bear'),
    'animal_tiger': ('Bengal Tiger', 'Fiery orange and black striped apex predator'),
    'animal_wolf': ('Timber Wolf', 'Wild howling pack hunter'),
    'pet_bengal': ('Bengal Cat', 'Exotic leopard-spotted domestic feline'),
    'pet_britishshorthair': ('British Shorthair', 'Plush blue-gray round-faced cat'),
    'pet_cockatiel': ('Cockatiel', 'Cheerful crested companion bird'),
    'pet_corgi': ('Pembroke Corgi', 'Spirited royal fox-eared herding dog'),
    'pet_frenchie': ('French Bulldog', 'Affectionate bat-eared companion pup'),
    'pet_germanshepherd': ('German Shepherd', 'Loyal and noble working canine'),
    'pet_hamster': ('Syrian Hamster', 'Cute fluffy cheek-pouching pet'),
    'pet_horse': ('Arabian Horse', 'Galloping noble equestrian beauty'),
    'pet_husky': ('Siberian Husky', 'Blue-eyed arctic sled dog'),
    'pet_mainecoon': ('Maine Coon', 'Gentle giant tufted-eared cat'),
    'pet_retriever': ('Golden Retriever', 'Friendly warm-hearted family retriever'),
    'pet_siberian_husky': ('Arctic Husky', 'Silver-coated winter endurance runner'),
    'pet_tulanne': ('Toucan', 'Vibrant rainbow-billed tropical bird'),

    # 05 Cars
    'car_amgone': ('AMG One', 'Formula 1 hybrid hypercar powerhouse'),
    'car_aston': ('Aston Martin', 'British handcrafted luxury supercar elegance'),
    'car_bugatti': ('Bugatti Chiron', 'Quad-turbo W16 record-breaking beast'),
    'car_corvette': ('Corvette Z06', 'Mid-engine American high-revving roar'),
    'car_ferrari': ('Ferrari Daytona', 'Rosso Corsa sculpted Italian thoroughbred'),
    'car_fordgt': ('Ford GT', 'Carbon-fiber aerodynamic Le Mans winner'),
    'car_koenigsegg': ('Koenigsegg Jesko', 'Swedish megacar engineering triumph'),
    'car_lambo': ('Lamborghini Revuelto', 'V12 hybrid wedge aggressive hypercar'),
    'car_mclaren': ('McLaren P1', 'Dihedral door aerodynamically honed racer'),
    'car_porsche': ('Porsche 911 GT3', 'Track-focused naturally aspirated icon'),

    # 06 Pop Culture
    'game_chief': ('Master Chief', 'Spartan-117 legendary armored hero'),
    'game_cloud': ('Cloud Strife', 'Buster sword-wielding ex-SOLDIER hero of Gaia'),
    'game_ellie': ('Ellie Williams', 'Resilient survivor of the cordyceps outbreak'),
    'game_geralt': ('Geralt of Rivia', 'White Wolf monster hunter of Blaviken'),
    'game_kratos': ('Kratos', 'Ghost of Sparta and Norse god of war'),
    'game_link': ('Link', 'Master Sword-bearing Hero of Hyrule'),
    'music_acdc': ('Angus Young', 'High-voltage electric guitar rock pioneer'),
    'music_billie': ('Billie Eilish', 'Genre-bending pop music visionary'),
    'music_bowie': ('David Bowie', 'Ziggy Stardust glam rock alien icon'),
    'music_daftpunk': ('Daft Punk', 'Legendary chrome robotic electronic duo'),
    'music_freddie': ('Freddie Mercury', 'Legendary Queen frontman and vocal titan'),
    'music_jimi': ('Jimi Hendrix', 'Psychedelic electric guitar virtuoso'),
    'music_michael': ('Michael Jackson', 'King of Pop moonwalking phenomenon'),
    'music_mileycyrus': ('Miley Cyrus', 'Unapologetic rock and pop vocal powerhouse'),
    'pop_barbie': ('Barbie', 'Iconic fashionable cultural symbol'),
    'pop_eleven': ('Eleven', 'Telekinetic girl who stopped the Upside Down'),
    'pop_elvis': ('Elvis Presley', 'King of Rock and Roll charisma'),
    'pop_godfather': ('Vito Corleone', 'Patriarch of the Corleone crime empire'),
    'pop_indy': ('Indiana Jones', 'Fedora-wearing whip-cracking archaeologist'),
    'pop_jacksparrow': ('Captain Jack Sparrow', 'Witty pirate lord of the Caribbean seas'),
    'pop_marilyn': ('Marilyn Monroe', 'Timeless Hollywood golden screen siren'),
    'pop_wednesday': ('Wednesday Addams', 'Morbidly brilliant gothic sleuth'),
    'pop_wonka': ('Willy Wonka', 'Eccentric genius candymaker visionary'),
    'scifi_caesar': ('Caesar', 'Evolved ape leader fighting for freedom'),

    # 07 Superheroes
    'anime_goku': ('Goku', 'Super Saiyan protector of Earth and universe champion'),
    'anime_ichigo': ('Ichigo Kurosaki', 'Substitute Soul Reaper wielding Zangetsu'),
    'anime_luffy': ('Monkey D. Luffy', 'Straw Hat pirate captain searching for One Piece'),
    'anime_naruto': ('Naruto Uzumaki', 'Nine-Tails Jinchuriki and Seventh Hokage'),
    'anime_tanjiro': ('Tanjiro Kamado', 'Sun breathing Demon Slayer and protective brother'),
    'dc_aquaman': ('Aquaman', 'Arthur Curry King of the Seven Seas'),
    'dc_bane': ('Bane', 'Tactical mastermind venom-fueled brute'),
    'dc_batman': ('The Dark Knight', 'Gotham City caped crusader vigilante'),
    'dc_blackadam': ('Black Adam', 'Ancient champion empowered by Egyptian gods'),
    'dc_flash': ('The Flash', 'Barry Allen scarlet speedster of Central City'),
    'dc_harley': ('Harley Quinn', 'Maid of mischief hammer-wielding rebel'),
    'dc_joker': ('The Joker', 'Clown Prince of Crime chaotic genius'),
    'dc_peacemaker': ('Peacemaker', 'Christopher Smith peace at any cost zealot'),
    'dc_superman': ('Superman', 'Kal-El Man of Steel from Krypton'),
    'dc_wonderwoman': ('Wonder Woman', 'Diana Prince Amazonian warrior of Themyscira'),
    'hero_deadpool': ('Deadpool', 'Merc with a Mouth fourth-wall breaker'),
    'hero_hulk': ('The Incredible Hulk', 'Gamma-powered green powerhouse titan'),
    'hero_ironman': ('Iron Man', 'Tony Stark armored genius philanthropist'),
    'hero_panther': ('Black Panther', 'King T-Challa protector of Wakanda'),
    'hero_spiderman': ('Spider-Man', 'Peter Parker friendly neighborhood webslinger'),
    'hero_thor': ('Thor Odinson', 'Asgardian god of thunder wielding Mjolnir'),
    'hero_wolverine': ('Wolverine', 'Logan adamantium-clawed mutant berserker'),

    # 08 Sci-Fi
    'scifi_bb8': ('BB-8', 'Loyal astromech rolling sphere droid'),
    'scifi_chewbacca': ('Chewbacca', 'Loyal Wookiee warrior and Falcon co-pilot'),
    'scifi_grogu': ('Grogu', 'Force-sensitive adorable Foundling'),
    'scifi_luke': ('Luke Skywalker', 'Jedi Knight who restored hope to the galaxy'),
    'scifi_mando': ('The Mandalorian', 'Din Djarin beskar-armored bounty hunter'),
    'scifi_maul': ('Darth Maul', 'Double-bladed lightsaber Sith warrior'),
    'scifi_obiwan': ('Obi-Wan Kenobi', 'Wise Jedi Master and guardian of the Force'),
    'scifi_paul': ('Paul Atreides', 'Muad-Dib prophesied savior of Arrakis'),
    'scifi_r2d2': ('R2-D2', 'Brave astromech droid and hero of the galaxy'),
    'scifi_robocop': ('RoboCop', 'Part man part machine all cop in Detroit'),
    'scifi_spock': ('Mr. Spock', 'Logical Vulcan Starfleet first officer'),
    'scifi_t800': ('T-800 Terminator', 'Cyberdyne Systems Model 101 assassin'),
    'scifi_vader': ('Darth Vader', 'Dark Lord of the Sith ruler of the Empire'),
    'scifi_xenomorph': ('Xenomorph', 'Perfect apex biomechanical alien organism'),
    'scifi_yoda': ('Master Yoda', 'Centuries-old Grand Master of the Jedi Order'),

    # 09 Monsters
    'kaiju_ghidorah': ('King Ghidorah', 'Three-headed golden dragon false king'),
    'kaiju_godzilla': ('Godzilla', 'Alpha predator King of the Monsters'),
    'kaiju_kong': ('Kong', 'King of Skull Island and Hollow Earth'),
    'kaiju_mechagodzilla': ('Mechagodzilla', 'Apex biomechanical robotic titan weapon'),
    'kaiju_mothra': ('Mothra', 'Queen of the Monsters benevolent winged titan'),
    'kaiju_muto': ('MUTO', 'Electromagnetic pulse parasite titan'),
    'kaiju_rodan': ('Rodan', 'Fire demon born of volcanic fury'),
    'kaiju_shimo': ('Shimo', 'Ancient frost titan capable of an ice age'),
    'kaiju_skarking': ('Skar King', 'Cruel whip-wielding ruler of Hollow Earth apes'),
    'kaiju_skullcrawler': ('Skullcrawler', 'Subterranean serpentine predator of Skull Island'),

    # 10 Abstract
    'abstract_amethyst': ('Amethyst Crystal', 'Deep purple crystalline geometric fracture'),
    'abstract_chromesphere': ('Chrome Sphere', 'Mirror liquid metal reflective sphere'),
    'abstract_cyberpunk': ('Cyber Matrix', 'Neon teal and magenta cybernetic grid'),
    'abstract_goldblack': ('Gold & Obsidian', 'Flowing liquid gold on dark stone veins'),
    'abstract_iridescent': ('Iridescent Flora', 'Shimmering pearlescent holographic petals'),
    'abstract_kintsugi': ('Kintsugi Gold', 'Japanese pottery mended with liquid gold seams'),
    'abstract_prism': ('Prism Spectrum', 'Refracted glass dispersing white light rainbows'),
    'abstract_quantum': ('Quantum Wave', 'Fluorescent particles in oscillating resonance'),
    'abstract_silk': ('Liquid Silk', 'Undulating midnight-blue and cyan fabric ripples'),
    'abstract_synthwave': ('Synthwave Grid', 'Retro sunset wireframe horizon in neon violet')
}

src_root = r'G:\My Drive\temp\TV-Launcher'
cat_dirs = sorted([d for d in os.listdir(src_root) if os.path.isdir(os.path.join(src_root, d))])

lines = []
lines.append('    public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays.asList(')
for c in cat_dirs:
    cat_name = cat_map[c]
    cat_path = os.path.join(src_root, c)
    if c == '07_Superheros':
        def sh_key(f):
            if '_hero_' in f: return (0, f)
            if '_dc_' in f: return (1, f)
            if '_anime_' in f: return (2, f)
            return (3, f)
        files = sorted([f for f in os.listdir(cat_path) if f.lower().endswith('.png')], key=sh_key)
    else:
        files = sorted([f for f in os.listdir(cat_path) if f.lower().endswith('.png')])
    lines.append(f'            // --- Category: {cat_name} ({len(files)} items) ---')
    for f in files:
        base = f[:-4]
        key = base.replace('ic_avatar_', '')
        label, desc = friendly_names.get(key, (base, cat_name))
        line = f'            new AvatarCharacter("{key}", "{label}", "{cat_name}", "{desc}", R.drawable.{base}),'
        lines.append(line)

lines[-1] = lines[-1][:-1] # remove trailing comma
lines.append('    );')

output_path = r'launcher/app/src/main/res/avatar_characters_generated.txt'
with open(output_path, 'w', encoding='utf-8') as out:
    out.write('\n'.join(lines))

print(f'Wrote {len(lines)} lines to {output_path}')
