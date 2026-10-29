# Android TV Clean Launcher - Avatar Guidelines & Master Catalog

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

### 1. Landscapes (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `land_fuji` | **Mount Fuji** | Lake Kawaguchiko with cherry blossoms and Mount Fuji | `ic_avatar_land_fuji.png` |
| 2 | `land_aurora` | **Aurora Borealis** | Vibrant green aurora borealis over Tromso fjord | `ic_avatar_land_aurora.png` |
| 3 | `land_yosemite` | **Yosemite Valley** | Tunnel View granite monoliths at golden hour | `ic_avatar_land_yosemite.png` |
| 4 | `land_matterhorn` | **Matterhorn** | Alpine pyramid reflecting in Riffelsee lake | `ic_avatar_land_matterhorn.png` |
| 5 | `land_amalfi` | **Amalfi Coast** | Cliffside pastel village of Positano above Mediterranean | `ic_avatar_land_amalfi.png` |
| 6 | `land_grandcanyon` | **Grand Canyon** | Horseshoe Bend curve glowing at crimson sunset | `ic_avatar_land_grandcanyon.png` |
| 7 | `land_maldives` | **Maldives Lagoon** | Turquoise tropical lagoon and coral reef waters | `ic_avatar_land_maldives.png` |
| 8 | `land_sahara` | **Sahara Dunes** | Erg Chebbi wind-sculpted sand dunes at dusk | `ic_avatar_land_sahara.png` |
| 9 | `land_patagonia` | **Torres del Paine** | Granite towers of Torres del Paine above azure lake | `ic_avatar_land_patagonia.png` |
| 10 | `land_bali` | **Bali Terraces** | Emerald green Tegalalang rice terraces at sunrise | `ic_avatar_land_bali.png` |

### 2. Landmarks (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `landmark_eiffel` | **Eiffel Tower** | Iconic iron lattice tower in Paris | `ic_avatar_landmark_eiffel.png` |
| 2 | `landmark_tajmahal` | **Taj Mahal** | Ivory-white marble mausoleum in Agra | `ic_avatar_landmark_tajmahal.png` |
| 3 | `landmark_pyramids` | **Great Pyramids** | Ancient pyramids of Giza under blue sky | `ic_avatar_landmark_pyramids.png` |
| 4 | `landmark_cntower` | **CN Tower** | Toronto communications and observation needle | `ic_avatar_landmark_cntower.png` |
| 5 | `landmark_liberty` | **Statue of Liberty** | Colossal neoclassical sculpture in New York Harbor | `ic_avatar_landmark_liberty.png` |
| 6 | `landmark_colosseum` | **The Colosseum** | Ancient Roman amphitheatre in central Rome | `ic_avatar_landmark_colosseum.png` |
| 7 | `landmark_bigben` | **Big Ben** | Elizabeth Tower clock tower at Palace of Westminster | `ic_avatar_landmark_bigben.png` |
| 8 | `landmark_goldengate` | **Golden Gate Bridge** | International orange suspension bridge in San Francisco | `ic_avatar_landmark_goldengate.png` |
| 9 | `landmark_operahouse` | **Sydney Opera House** | Multi-venue performing arts centre in Sydney Harbour | `ic_avatar_landmark_operahouse.png` |
| 10 | `landmark_machupicchu` | **Machu Picchu** | 15th-century Inca citadel on mountain ridge in Peru | `ic_avatar_landmark_machupicchu.png` |

### 3. Cities (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `city_nyc` | **New York City** | Manhattan skyline at twilight | `ic_avatar_city_nyc.png` |
| 2 | `city_tokyo` | **Tokyo** | Tokyo Tower and glittering metropolitan skyline | `ic_avatar_city_tokyo.png` |
| 3 | `city_paris` | **Paris** | City of Light rooftops and Eiffel Tower skyline | `ic_avatar_city_paris.png` |
| 4 | `city_london` | **London** | River Thames, Tower Bridge, and city lights | `ic_avatar_city_london.png` |
| 5 | `city_dubai` | **Dubai** | Burj Khalifa rising above gleaming desert skyline | `ic_avatar_city_dubai.png` |
| 6 | `city_rome` | **Rome** | St. Peter's Basilica dome and historic city skyline | `ic_avatar_city_rome.png` |
| 7 | `city_hongkong` | **Hong Kong** | Victoria Harbour skyscrapers and glowing neon lights | `ic_avatar_city_hongkong.png` |
| 8 | `city_sanfrancisco` | **San Francisco** | Hills, bay view, and city architecture | `ic_avatar_city_sanfrancisco.png` |
| 9 | `city_singapore` | **Singapore** | Marina Bay Sands and illuminated modern skyline | `ic_avatar_city_singapore.png` |
| 10 | `city_chicago` | **Chicago** | Loop skyscrapers along Michigan Avenue and lakefront | `ic_avatar_city_chicago.png` |

### 4. Space (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `space_pillars` | **Pillars of Creation** | Star-forming pillars in Eagle Nebula by Hubble | `ic_avatar_space_pillars.png` |
| 2 | `space_carina` | **Carina Nebula** | Cosmic Cliffs stellar nursery captured by JWST | `ic_avatar_space_carina.png` |
| 3 | `space_andromeda` | **Andromeda Galaxy** | Majestic spiral galaxy neighbor in deep space | `ic_avatar_space_andromeda.png` |
| 4 | `space_ringnebula` | **Ring Nebula** | Planetary nebula gas shroud captured by JWST | `ic_avatar_space_ringnebula.png` |
| 5 | `space_orion` | **Orion Nebula** | Massive interstellar nursery glowing in ultraviolet | `ic_avatar_space_orion.png` |
| 6 | `space_crab` | **Crab Nebula** | Supernova remnant expanding filament cloud | `ic_avatar_space_crab.png` |
| 7 | `space_sombrero` | **Sombrero Galaxy** | Brilliant white bulb with dark dust lane rim | `ic_avatar_space_sombrero.png` |
| 8 | `space_whirlpool` | **Whirlpool Galaxy** | Grand design spiral galaxy interacting with neighbor | `ic_avatar_space_whirlpool.png` |
| 9 | `space_blackhole` | **Black Hole M87** | Event Horizon Telescope portrait of supermassive core | `ic_avatar_space_blackhole.png` |
| 10 | `space_eclipse` | **Total Solar Eclipse** | Diamond ring effect and shimmering solar corona | `ic_avatar_space_eclipse.png` |

### 5. Planets (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `planet_earth` | **Planet Earth** | Apollo 17 Blue Marble view of our home planet | `ic_avatar_planet_earth.png` |
| 2 | `planet_mars` | **Mars** | The red planet captured in true color by OSIRIS | `ic_avatar_planet_mars.png` |
| 3 | `planet_jupiter` | **Jupiter** | Cassini true color view of Great Red Spot | `ic_avatar_planet_jupiter.png` |
| 4 | `planet_saturn` | **Saturn** | Cassini equinox portrait with majestic rings | `ic_avatar_planet_saturn.png` |
| 5 | `planet_neptune` | **Neptune** | Voyager 2 true color portrait of the azure ice giant | `ic_avatar_planet_neptune.png` |
| 6 | `planet_moon` | **The Moon** | High-resolution lunar surface in twilight space | `ic_avatar_planet_moon.png` |
| 7 | `planet_sun` | **The Sun** | NASA SDO AIA 304 extreme ultraviolet solar sphere | `ic_avatar_planet_sun.png` |
| 8 | `planet_kepler` | **Venus** | NASA Magellan and Mariner global portrait of Venus | `ic_avatar_planet_kepler.png` |
| 9 | `planet_gliesia` | **Uranus** | NASA Voyager 2 true color portrait of the cyan giant | `ic_avatar_planet_gliesia.png` |
| 10 | `planet_inferno` | **Pluto** | NASA New Horizons high-res portrait with heart region | `ic_avatar_planet_inferno.png` |

### 6. Science (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `sci_einstein` | **Albert Einstein** | Theoretical physicist and creator of relativity | `ic_avatar_sci_einstein.png` |
| 2 | `sci_curie` | **Marie Curie** | Pioneering physicist and chemist in radioactivity | `ic_avatar_sci_curie.png` |
| 3 | `sci_newton` | **Isaac Newton** | Mathematician, physicist, and discoverer of gravity | `ic_avatar_sci_newton.png` |
| 4 | `sci_tesla` | **Nikola Tesla** | Inventor and electrical engineer of AC electrical systems | `ic_avatar_sci_tesla.png` |
| 5 | `sci_hawking` | **Stephen Hawking** | Theoretical physicist and cosmologist on black holes | `ic_avatar_sci_hawking.png` |
| 6 | `sci_sagan` | **Carl Sagan** | Astronomer, planetary scientist, and science communicator | `ic_avatar_sci_sagan.png` |
| 7 | `sci_galileo` | **Galileo Galilei** | Astronomer and father of observational astronomy | `ic_avatar_sci_galileo.png` |
| 8 | `sci_darwin` | **Charles Darwin** | Naturalist and geologist pioneer of evolutionary biology | `ic_avatar_sci_darwin.png` |
| 9 | `sci_dna` | **DNA Double Helix** | 3D molecular visualization of the genetic code | `ic_avatar_sci_dna.png` |
| 10 | `sci_quantum` | **Quantum Atom** | Luminous atomic orbitals and quantum energy states | `ic_avatar_sci_quantum.png` |

### 7. Animals (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `animal_lion` | **Male Lion** | Majestic golden male lion with full mane | `ic_avatar_animal_lion.png` |
| 2 | `animal_tiger` | **Bengal Tiger** | Powerful tiger face with vivid amber eyes | `ic_avatar_animal_tiger.png` |
| 3 | `animal_leopard` | **Snow Leopard** | Stealthy snow leopard with piercing blue eyes | `ic_avatar_animal_leopard.png` |
| 4 | `animal_wolf` | **Arctic Wolf** | Noble white wolf in northern snowy wilderness | `ic_avatar_animal_wolf.png` |
| 5 | `animal_bear` | **Polar Bear** | Apex Arctic polar bear in snow portrait | `ic_avatar_animal_bear.png` |
| 6 | `animal_fox` | **Red Fox** | Crimson red fox in snowy winter landscape | `ic_avatar_animal_fox.png` |
| 7 | `animal_eagle` | **Bald Eagle** | Soaring bald eagle with intense golden eye | `ic_avatar_animal_eagle.png` |
| 8 | `animal_owl` | **Barn Owl** | Wise nocturnal barn owl perched in shadow | `ic_avatar_animal_owl.png` |
| 9 | `animal_panda` | **Giant Panda** | Gentle giant panda enjoying fresh bamboo | `ic_avatar_animal_panda.png` |
| 10 | `animal_gorilla` | **Silverback Gorilla** | Wise mountain silverback gorilla portrait | `ic_avatar_animal_gorilla.png` |

### 8. Pets (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `pet_retriever` | **Golden Retriever** | Friendly golden retriever smiling happily | `ic_avatar_pet_retriever.png` |
| 2 | `pet_frenchie` | **French Bulldog** | Playful French bulldog with alert bat ears | `ic_avatar_pet_frenchie.png` |
| 3 | `pet_husky` | **Siberian Husky** | Striking husky with icy blue eyes | `ic_avatar_pet_husky.png` |
| 4 | `pet_germanshepherd` | **German Shepherd** | Loyal German shepherd dog with alert ears | `ic_avatar_pet_germanshepherd.png` |
| 5 | `pet_corgi` | **Pembroke Welsh Corgi** | Cheerful Pembroke Welsh corgi smiling face | `ic_avatar_pet_corgi.png` |
| 6 | `pet_mainecoon` | **Maine Coon Cat** | Regal Maine Coon cat with lynx-like tufts | `ic_avatar_pet_mainecoon.png` |
| 7 | `pet_britishshorthair` | **British Shorthair** | Plush blue British shorthair with copper eyes | `ic_avatar_pet_britishshorthair.png` |
| 8 | `pet_bengal` | **Bengal Cat** | Exotic leopard-spotted Bengal cat with green eyes | `ic_avatar_pet_bengal.png` |
| 9 | `pet_hamster` | **Golden Hamster** | Cute fluffy hamster holding sunflower seed | `ic_avatar_pet_hamster.png` |
| 10 | `pet_cockatiel` | **Cockatiel** | Yellow-crested cockatiel with orange cheek patches | `ic_avatar_pet_cockatiel.png` |

### 9. Cars (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `car_ferrari` | **Ferrari SF90** | 1000 HP plug-in hybrid Italian supercar | `ic_avatar_car_ferrari.png` |
| 2 | `car_porsche` | **Porsche 911 GT3 RS** | Track-honed naturally aspirated German sports car | `ic_avatar_car_porsche.png` |
| 3 | `car_lambo` | **Lamborghini Revuelto** | V12 hybrid high performance Italian flagship | `ic_avatar_car_lambo.png` |
| 4 | `car_bugatti` | **Bugatti Chiron** | 1500 HP quad-turbo French hypercar | `ic_avatar_car_bugatti.png` |
| 5 | `car_mclaren` | **McLaren P1** | Twin-turbo V8 hybrid British hypercar | `ic_avatar_car_mclaren.png` |
| 6 | `car_aston` | **Aston Martin Valkyrie** | F1-inspired 6.5L V12 British hypercar | `ic_avatar_car_aston.png` |
| 7 | `car_fordgt` | **Ford GT** | Twin-turbo EcoBoost American carbon supercar | `ic_avatar_car_fordgt.png` |
| 8 | `car_koenigsegg` | **Koenigsegg Jesko** | 1600 HP megacar with active aero wing | `ic_avatar_car_koenigsegg.png` |
| 9 | `car_corvette` | **Corvette Z06** | Flat-plane crank American supercar | `ic_avatar_car_corvette.png` |
| 10 | `car_amgone` | **Mercedes-AMG ONE** | Formula 1 powertrain for the road | `ic_avatar_car_amgone.png` |

### 10. Music (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `music_daftpunk` | **Daft Punk** | Electronic music helmets in chrome and gold | `ic_avatar_music_daftpunk.png` |
| 2 | `music_weeknd` | **The Weeknd** | Abel Tesfaye in After Hours red suit portrait | `ic_avatar_music_weeknd.png` |
| 3 | `music_taylorswift` | **Taylor Swift** | Pop superstar live in concert on the Eras Tour | `ic_avatar_music_taylorswift.png` |
| 4 | `music_billie` | **Billie Eilish** | Alternative pop singer-songwriter studio portrait | `ic_avatar_music_billie.png` |
| 5 | `music_eminem` | **Eminem** | Marshall Mathers legendary rap icon portrait | `ic_avatar_music_eminem.png` |
| 6 | `music_freddie` | **Freddie Mercury** | Legendary Queen frontman at Live Aid 1985 | `ic_avatar_music_freddie.png` |
| 7 | `music_bowie` | **David Bowie** | Aladdin Sane lightning bolt face makeup | `ic_avatar_music_bowie.png` |
| 8 | `music_jimi` | **Jimi Hendrix** | Psychedelic guitar virtuoso in concert | `ic_avatar_music_jimi.png` |
| 9 | `music_michael` | **Michael Jackson** | King of Pop in iconic fedora hat | `ic_avatar_music_michael.png` |
| 10 | `music_drake` | **Drake** | Global hip-hop chart topper studio portrait | `ic_avatar_music_drake.png` |

### 11. Pop Culture (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `pop_godfather` | **Don Vito Corleone** | Marlon Brando in tuxedo with red rose from The Godfather | `ic_avatar_pop_godfather.png` |
| 2 | `pop_barbie` | **Barbie** | Margot Robbie in pink gingham dress from Barbie | `ic_avatar_pop_barbie.png` |
| 3 | `pop_oppenheimer` | **J. Robert Oppenheimer** | Cillian Murphy in fedora and pipe from Oppenheimer | `ic_avatar_pop_oppenheimer.png` |
| 4 | `pop_marilyn` | **Marilyn Monroe** | Classic Hollywood studio portrait of Marilyn Monroe | `ic_avatar_pop_marilyn.png` |
| 5 | `pop_elvis` | **Elvis Presley** | The King of Rock and Roll in 1968 comeback special | `ic_avatar_pop_elvis.png` |
| 6 | `pop_indy` | **Indiana Jones** | Harrison Ford with brown fedora and leather jacket | `ic_avatar_pop_indy.png` |
| 7 | `pop_wednesday` | **Wednesday Addams** | Jenna Ortega in black Nevermore uniform with braids | `ic_avatar_pop_wednesday.png` |
| 8 | `pop_wonka` | **Willy Wonka** | Gene Wilder in velvet plum coat and top hat | `ic_avatar_pop_wonka.png` |
| 9 | `pop_topgun` | **Maverick Pete Mitchell** | Tom Cruise in aviator sunglasses and flight jacket | `ic_avatar_pop_topgun.png` |
| 10 | `pop_ghostbusters` | **Peter Venkman** | Bill Murray in Ghostbusters jumpsuit with proton pack | `ic_avatar_pop_ghostbusters.png` |

### 12. Abstract (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `abstract_iridescent` | **Iridescent Glass** | 3D liquid rainbow glass sphere on obsidian dark | `ic_avatar_abstract_iridescent.png` |
| 2 | `abstract_goldblack` | **Liquid Gold** | Molten liquid gold swirling through black marble | `ic_avatar_abstract_goldblack.png` |
| 3 | `abstract_chromesphere` | **Chrome Sphere** | Liquid mercury mirror sphere reflecting studio lights | `ic_avatar_abstract_chromesphere.png` |
| 4 | `abstract_synthwave` | **Synthwave** | Retro 80s neon grid with glowing horizon sun | `ic_avatar_abstract_synthwave.png` |
| 5 | `abstract_amethyst` | **Amethyst Geode** | Vibrant purple crystalline quartz mineral slice | `ic_avatar_abstract_amethyst.png` |
| 6 | `abstract_prism` | **Prism Light** | Crystal glass prism scattering spectrum rainbow | `ic_avatar_abstract_prism.png` |
| 7 | `abstract_silk` | **Metallic Silk** | Fluid dynamic velvet silk ribbon twisting in 3D | `ic_avatar_abstract_silk.png` |
| 8 | `abstract_quantum` | **Quantum Vortex** | Luminous cyan and magenta particle energy field | `ic_avatar_abstract_quantum.png` |
| 9 | `abstract_kintsugi` | **Kintsugi** | Cracked Japanese porcelain repaired with pure gold | `ic_avatar_abstract_kintsugi.png` |
| 10 | `abstract_cyberpunk` | **Cyber Wave** | Cyberpunk neon fiber wave in turquoise and magenta | `ic_avatar_abstract_cyberpunk.png` |

### 13. Marvel (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `marvel_ironman` | **Iron Man** | Tony Stark in high-tech Mark armor helmet | `ic_avatar_marvel_ironman.png` |
| 2 | `marvel_captain` | **Captain America** | Steve Rogers with vibranium star shield | `ic_avatar_marvel_captain.png` |
| 3 | `marvel_thor` | **Thor** | Norse God of Thunder wielding Mjolnir | `ic_avatar_marvel_thor.png` |
| 4 | `marvel_spiderman` | **Spider-Man** | Peter Parker in modern cinematic web suit | `ic_avatar_marvel_spiderman.png` |
| 5 | `marvel_panther` | **Black Panther** | T'Challa King of Wakanda in vibranium habit | `ic_avatar_marvel_panther.png` |
| 6 | `marvel_strange` | **Doctor Strange** | Stephen Strange Master of the Mystic Arts | `ic_avatar_marvel_strange.png` |
| 7 | `marvel_thanos` | **Thanos** | The Mad Titan warlord of Titan | `ic_avatar_marvel_thanos.png` |
| 8 | `marvel_loki` | **Loki** | God of Mischief and Lord of Stories | `ic_avatar_marvel_loki.png` |
| 9 | `marvel_deadpool` | **Deadpool** | Wade Wilson the Merc with a Mouth | `ic_avatar_marvel_deadpool.png` |
| 10 | `marvel_wolverine` | **Wolverine** | Logan with adamantium claws in yellow suit | `ic_avatar_marvel_wolverine.png` |

### 14. DC Comics (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `dc_batman` | **Batman** | Bruce Wayne the Dark Knight of Gotham in cowl | `ic_avatar_dc_batman.png` |
| 2 | `dc_superman` | **Superman** | Clark Kent the Man of Steel in cinematic S-shield suit | `ic_avatar_dc_superman.png` |
| 3 | `dc_wonderwoman` | **Wonder Woman** | Diana Prince Amazon warrior of Themyscira in tiara | `ic_avatar_dc_wonderwoman.png` |
| 4 | `dc_joker` | **The Joker** | The Clown Prince of Crime in cinematic makeup | `ic_avatar_dc_joker.png` |
| 5 | `dc_peacemaker` | **Peacemaker** | Christopher Smith in chrome beacon helmet | `ic_avatar_dc_peacemaker.png` |
| 6 | `dc_harley` | **Harley Quinn** | Maid of Mischief in cinematic anti-hero gear | `ic_avatar_dc_harley.png` |
| 7 | `dc_aquaman` | **Aquaman** | Arthur Curry King of Atlantis in scale armor | `ic_avatar_dc_aquaman.png` |
| 8 | `dc_flash` | **The Flash** | Barry Allen the fastest man alive in crimson cowl | `ic_avatar_dc_flash.png` |
| 9 | `dc_blackadam` | **Black Adam** | Teth-Adam champion of Kahndaq | `ic_avatar_dc_blackadam.png` |
| 10 | `dc_bane` | **Bane** | The masked revolutionary in tactical vest | `ic_avatar_dc_bane.png` |

### 15. Star Wars (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `sw_vader` | **Darth Vader** | Sith Lord and Commander of the Imperial Fleet in black helmet | `ic_avatar_sw_vader.png` |
| 2 | `sw_mando` | **The Mandalorian** | Din Djarin in shining beskar armor helmet | `ic_avatar_sw_mando.png` |
| 3 | `sw_grogu` | **Grogu** | Force-sensitive youngling with large expressive ears | `ic_avatar_sw_grogu.png` |
| 4 | `sw_ahsoka` | **Ahsoka Tano** | Former Jedi padawan with white markings and striped lekku | `ic_avatar_sw_ahsoka.png` |
| 5 | `sw_obiwan` | **Obi-Wan Kenobi** | Jedi Master in brown robes and beard | `ic_avatar_sw_obiwan.png` |
| 6 | `sw_bobafett` | **Boba Fett** | Legendary bounty hunter in green Mandalorian helmet | `ic_avatar_sw_bobafett.png` |
| 7 | `sw_maul` | **Darth Maul** | Zabrak Sith Lord with red and black facial tattoos | `ic_avatar_sw_maul.png` |
| 8 | `sw_kyloren` | **Kylo Ren** | Supreme Leader of the First Order in crossguard helmet | `ic_avatar_sw_kyloren.png` |
| 9 | `sw_stormtrooper` | **Stormtrooper** | Imperial shock trooper in pristine white helmet | `ic_avatar_sw_stormtrooper.png` |
| 10 | `sw_luke` | **Luke Skywalker** | Jedi Knight hero of the Rebel Alliance in black tunic | `ic_avatar_sw_luke.png` |

### 16. Lord of the Rings (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `lotr_aragorn` | **Aragorn** | King Elessar rightful heir of Isildur with Elven brooch | `ic_avatar_lotr_aragorn.png` |
| 2 | `lotr_gandalf` | **Gandalf the White** | Wise wizard of Middle-earth with staff | `ic_avatar_lotr_gandalf.png` |
| 3 | `lotr_legolas` | **Legolas** | Prince of the Woodland Realm Elven archer | `ic_avatar_lotr_legolas.png` |
| 4 | `lotr_gimli` | **Gimli** | Son of Gloin warrior dwarf with iron helmet and braided beard | `ic_avatar_lotr_gimli.png` |
| 5 | `lotr_frodo` | **Frodo Baggins** | Ring-bearer Hobbit with Elven cloak and green brooch | `ic_avatar_lotr_frodo.png` |
| 6 | `lotr_gollum` | **Gollum** | Corrupted creature consumed by the One Ring | `ic_avatar_lotr_gollum.png` |
| 7 | `lotr_sauron` | **Sauron** | The Dark Lord in spiked black battle armor and helm | `ic_avatar_lotr_sauron.png` |
| 8 | `lotr_galadriel` | **Galadriel** | Lady of Light and ruler of Lothlorien in silver tiara | `ic_avatar_lotr_galadriel.png` |
| 9 | `lotr_sam` | **Samwise Gamgee** | Brave and loyal companion of the Fellowship | `ic_avatar_lotr_sam.png` |
| 10 | `lotr_witchking` | **Witch-king** | Lord of the Nazgul with towering spiked iron crown | `ic_avatar_lotr_witchking.png` |

### 17. Harry Potter (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `hp_harry` | **Harry Potter** | Gryffindor seeker with round glasses and lightning bolt scar | `ic_avatar_hp_harry.png` |
| 2 | `hp_hermione` | **Hermione Granger** | Brightest witch of her age in Hogwarts robes | `ic_avatar_hp_hermione.png` |
| 3 | `hp_ron` | **Ron Weasley** | Loyal Gryffindor friend with red hair | `ic_avatar_hp_ron.png` |
| 4 | `hp_dumbledore` | **Albus Dumbledore** | Hogwarts Headmaster with silver beard and half-moon spectacles | `ic_avatar_hp_dumbledore.png` |
| 5 | `hp_snape` | **Severus Snape** | Potions Master in black frock coat and dark hair | `ic_avatar_hp_snape.png` |
| 6 | `hp_voldemort` | **Lord Voldemort** | The Dark Lord with pale serpentine face and slit pupils | `ic_avatar_hp_voldemort.png` |
| 7 | `hp_sirius` | **Sirius Black** | Padfoot prisoner of Azkaban in velvet coat | `ic_avatar_hp_sirius.png` |
| 8 | `hp_hagrid` | **Rubeus Hagrid** | Keeper of Keys and Grounds with bushy beard and moleskin coat | `ic_avatar_hp_hagrid.png` |
| 9 | `hp_draco` | **Draco Malfoy** | Slytherin student with slicked platinum blond hair | `ic_avatar_hp_draco.png` |
| 10 | `hp_dobby` | **Dobby** | Free house-elf with large green tennis-ball eyes and bat ears | `ic_avatar_hp_dobby.png` |

### 18. Game of Thrones (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `got_jonsnow` | **Jon Snow** | King in the North in black fur cloak and Valyrian steel sword | `ic_avatar_got_jonsnow.png` |
| 2 | `got_daenerys` | **Daenerys Targaryen** | Mother of Dragons with braided platinum silver hair | `ic_avatar_got_daenerys.png` |
| 3 | `got_tyrion` | **Tyrion Lannister** | Hand of the Queen in Hand brooch and dark doublet | `ic_avatar_got_tyrion.png` |
| 4 | `got_arya` | **Arya Stark** | Faceless assassin of Winterfell in northern leather tunic | `ic_avatar_got_arya.png` |
| 5 | `got_cersei` | **Cersei Lannister** | Queen of the Seven Kingdoms in royal gold embroidery | `ic_avatar_got_cersei.png` |
| 6 | `got_jaime` | **Jaime Lannister** | Kingslayer in golden Lannister armor | `ic_avatar_got_jaime.png` |
| 7 | `got_nightking` | **Night King** | Leader of the White Walkers with icy blue eyes and horned crown | `ic_avatar_got_nightking.png` |
| 8 | `got_nedstark` | **Ned Stark** | Lord of Winterfell and Warden of the North with broadsword | `ic_avatar_got_nedstark.png` |
| 9 | `got_brienne` | **Brienne of Tarth** | Knight of the Seven Kingdoms in heavy iron plate armor | `ic_avatar_got_brienne.png` |
| 10 | `got_drogon` | **Drogon** | Giant black dragon with crimson horns and fiery glowing eyes | `ic_avatar_got_drogon.png` |

### 19. Kaijus (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `kaiju_godzilla` | **Godzilla** | King of the Monsters centered head roar with blue dorsal glow | `ic_avatar_kaiju_godzilla.png` |
| 2 | `kaiju_kong` | **King Kong** | Protector of Skull Island centered snarling facial portrait | `ic_avatar_kaiju_kong.png` |
| 3 | `kaiju_ghidorah` | **King Ghidorah** | Lit three-headed golden lightning dragon front view | `ic_avatar_kaiju_ghidorah.png` |
| 4 | `kaiju_mothra` | **Mothra** | Queen of the Monsters radiant facial portrait with blue compound eyes | `ic_avatar_kaiju_mothra.png` |
| 5 | `kaiju_rodan` | **Rodan** | Fire demon bird of prey centered beak and fiery volcanic eyes | `ic_avatar_kaiju_rodan.png` |
| 6 | `kaiju_mechagodzilla` | **Mechagodzilla** | Apex cybernetic titan polished metallic head with red visor | `ic_avatar_kaiju_mechagodzilla.png` |
| 7 | `kaiju_shimo` | **Shimo** | Ancient frost titan crystalline ice dragon head portrait | `ic_avatar_kaiju_shimo.png` |
| 8 | `kaiju_skarking` | **Skar King** | Red-furred tyrant ape centered snarling face | `ic_avatar_kaiju_skarking.png` |
| 9 | `kaiju_muto` | **MUTO** | Electromagnetic parasitic titan triangular head with crimson visor | `ic_avatar_kaiju_muto.png` |
| 10 | `kaiju_skullcrawler` | **Skullcrawler** | Subterranean predator with bone-white skull head and yellow eyes | `ic_avatar_kaiju_skullcrawler.png` |

### 20. Sci-Fi (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `scifi_paul` | **Paul Atreides** | Muad'Dib Duke of Arrakis with blue-in-blue spice eyes | `ic_avatar_scifi_paul.png` |
| 2 | `scifi_chani` | **Chani** | Fremen warrior and desert guide on Arrakis with nose filter | `ic_avatar_scifi_chani.png` |
| 3 | `scifi_neo` | **Neo** | Keanu Reeves in black sunglasses and trenchcoat from The Matrix | `ic_avatar_scifi_neo.png` |
| 4 | `scifi_t800` | **Terminator T-800** | Cyberdyne Model 101 polished chrome skull with glowing red eye | `ic_avatar_scifi_t800.png` |
| 5 | `scifi_spock` | **Spock** | Leonard Nimoy in classic blue Starfleet uniform with Vulcan pointed ears | `ic_avatar_scifi_spock.png` |
| 6 | `scifi_jakesully` | **Jake Sully** | Toruk Makto Na'vi warrior with bioluminescent blue skin and gold eyes | `ic_avatar_scifi_jakesully.png` |
| 7 | `scifi_xenomorph` | **Xenomorph** | Apex biomechanical alien with elongated black dome and silver metallic teeth | `ic_avatar_scifi_xenomorph.png` |
| 8 | `scifi_marty` | **Marty McFly** | Michael J. Fox in iconic red puffer vest and denim jacket from Back to the Future | `ic_avatar_scifi_marty.png` |
| 9 | `scifi_caesar` | **Caesar** | Andy Serkis modern reboot CGI ape leader with intelligent hazel eyes | `ic_avatar_scifi_caesar.png` |
| 10 | `scifi_robocop` | **RoboCop** | Peter Weller in titanium silver helmet and dark visor | `ic_avatar_scifi_robocop.png` |

### 21. Video Games (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `game_kratos` | **Kratos** | Ghost of Sparta God of War with full beard and crimson tattoo | `ic_avatar_game_kratos.png` |
| 2 | `game_geralt` | **Geralt of Rivia** | The White Wolf monster slayer with cat eyes and silver hair (CDPR render) | `ic_avatar_game_geralt.png` |
| 3 | `game_chief` | **Master Chief** | Spartan-117 in Mjolnir Mark VI green armor and gold visor | `ic_avatar_game_chief.png` |
| 4 | `game_arthur` | **Arthur Morgan** | Senior outlaw of Van der Linde gang in leather hat and neckerchief | `ic_avatar_game_arthur.png` |
| 5 | `game_ellie` | **Ellie** | Immune survivor with eyebrow scar from The Last of Us Part II | `ic_avatar_game_ellie.png` |
| 6 | `game_joel` | **Joel Miller** | Hardened survivor with graying beard from The Last of Us Part I | `ic_avatar_game_joel.png` |
| 7 | `game_jinsakai` | **Jin Sakai** | The Ghost of Tsushima with black armored ghost mask | `ic_avatar_game_jinsakai.png` |
| 8 | `game_silverhand` | **Johnny Silverhand** | Keanu Reeves rockerboy rebel with aviator sunglasses and cyberarm | `ic_avatar_game_silverhand.png` |
| 9 | `game_cloud` | **Cloud Strife** | Ex-SOLDIER mercenary with spiky blond hair and mako blue eyes | `ic_avatar_game_cloud.png` |
| 10 | `game_link` | **Link** | Hero of Time courageous champion with blond hair and Champion tunic | `ic_avatar_game_link.png` |

### 22. Anime (10 items)

| # | Item ID | Character / Subject | Description | Target Filename |
| :-: | :--- | :--- | :--- | :--- |
| 1 | `anime_goku` | **Son Goku** | Saiyan martial artist defender of Earth with iconic black hair | `ic_avatar_anime_goku.png` |
| 2 | `anime_luffy` | **Monkey D. Luffy** | Captain of Straw Hat Pirates with red ribbon straw hat and scar | `ic_avatar_anime_luffy.png` |
| 3 | `anime_naruto` | **Naruto Uzumaki** | Seventh Hokage with Hidden Leaf forehead protector and whiskered cheeks | `ic_avatar_anime_naruto.png` |
| 4 | `anime_ichigo` | **Ichigo Kurosaki** | Substitute Soul Reaper with vibrant orange spiky hair and black robes | `ic_avatar_anime_ichigo.png` |
| 5 | `anime_tanjiro` | **Tanjiro Kamado** | Demon Slayer with forehead scar, burgundy hair, and Hanafuda earrings | `ic_avatar_anime_tanjiro.png` |
| 6 | `anime_nezuko` | **Nezuko Kamado** | Demon sister with bamboo mouth muzzle and pink eyes | `ic_avatar_anime_nezuko.png` |
| 7 | `anime_gojo` | **Satoru Gojo** | Special Grade Jujutsu sorcerer with white hair and blindfold/Six Eyes | `ic_avatar_anime_gojo.png` |
| 8 | `anime_eren` | **Eren Yeager** | Survey Corps soldier with determined emerald eyes and Survey Corps jacket | `ic_avatar_anime_eren.png` |
| 9 | `anime_levi` | **Levi Ackerman** | Humanity's strongest soldier Captain Levi with black undercut and cravat | `ic_avatar_anime_levi.png` |
| 10 | `anime_saitama` | **Saitama** | One Punch Man hero for fun in yellow suit with white cape | `ic_avatar_anime_saitama.png` |

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
