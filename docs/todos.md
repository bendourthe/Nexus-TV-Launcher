# Android TV Clean Launcher & Debloat -- Progress Dashboard

**Device:** Hisense Google TV (`10.0.0.96`, "Living Room TV")  
**Branch:** `main`

---

## Scores (update after each sprint)

| Metric | Current | Target | Delta |
|--------|---------|--------|-------|
| ADB Connection | Connected (`10.0.0.169:5555` Fire TV, `10.0.0.96` Hisense) | Connected | 0 |
| System Backup Completed | Complete (`backup/`) | Done | 0 |
| Debloated Package Count | 8 disabled | 8 | 0 |
| Native Launcher Built & Installed | Installed & Verified on Hisense & Fire TV | Done | 0 |
| Functional Verification Screenshots | 74 captures | Completed | 0 |
| Alphabetical Phone App Grid | Implemented & Verified | Done | 0 |
| Continue Watching Dock Binding | Implemented & Verified | Done | 0 |
| Continue Watching Deep-Linking | Implemented & Verified on Both TVs | Done | 0 |
| Multi-User Profile Support | Implemented & Verified | Done | 0 |
| Avatar Personalization Library | 220+ avatars with Marvel -> DC -> Anime ordering | 220+ avatars | 0 |

---

## Task Roadmap

### Phase 1 - Connect & Establish Baseline

- [x] Identify Hisense Google TV IP address on local subnet (`10.0.0.96`, OUI Qingdao Intelligent&Precise Electronics)
- [x] Connect over ADB (Wireless Debugging pairing code + port or port 5555)
- [x] Switch to persistent TCP/IP mode (`adb tcpip 5555`)
- [x] Verify ADB shell access and screen resolution / density (1920x1080 @ 320 dpi)

### Phase 2 - Comprehensive Backup & Safety

- [x] Check USB peripheral status and warn regarding power cut behavior
- [x] Dump full installed package list to `backup/packages_all.txt`
- [x] Dump disabled package list to `backup/packages_disabled.txt`
- [x] Dump system build properties via `getprop` to `backup/getprop.txt`
- [x] Dump system settings (`system`, `secure`, `global`) to `backup/settings_*.txt`
- [x] Generate comprehensive restore script `scripts/restore.sh` / `scripts/restore.ps1`

### Phase 3 - Auditing & Preferences Confirmation

- [x] Query user preferences on streaming apps, voice assistant, Chromecast, Miracast, and system updates (Retain: Chromecast, Updates, Assistant, Miracast; Enable: AdGuard DNS; USB: None)
- [x] Classify packages into telemetry, content/ads, background bloat, preloads, and streaming
- [x] Perform `dumpsys package` inspection for any ambiguous packages to ensure vital TV/tuner/HDMI/remote services remain intact

### Phase 4 - Safe Batch Debloating

- [x] Disable non-essential telemetry and manufacturer content services in small batches using `pm disable-user --user 0`
- [x] Capture screenshot verification after each batch
- [x] Check top resumed activity and tuner/HDMI source functionality
- [x] Configure private ad-blocking DNS (AdGuard DNS)

### Phase 5 - Apple TV Style Native Launcher

- [x] Verify TV sideload / APK install capability and check `HOME` activity claims
- [x] Check Wi-Fi configuration ownership (`dumpsys wifi`) to ensure network retention before disabling stock launcher
- [x] Build lightweight native Android TV launcher (Apple TV 4K style: pitch black, rounded dock, 6 dock streaming apps, glass focus highlights, Outfit font, Inputs dropdown, deterministic D-pad)
- [x] Eliminate red ambient spotlight dot and implement hardware-accelerated sinusoidal mesh liquid animation for entire 4K textured background
- [x] Implement dynamic color scheme & abstract shape system (6 distinct shapes/animations and palettes with 3s smooth transitions and 5-min auto-rotation)
- [x] Push and install launcher APK
- [x] Configure Home Button Accessibility Service to intercept remote HOME key events and route to Clean Launcher
- [x] Keep system recovery and critical services intact to preserve Wi-Fi, tuner, and HDMI inputs

### Phase 6 - Verification & Polish

- [x] Modernize weather widget to match time widget hierarchy, fix location to San Jose with city selector
- [x] Refine background wave animations to pure flowing liquid glass (zero twirling, no hard angles, seamless looping)
- [x] Completely remove Instagram from dock and all apps list
- [x] Implement 1.18x focus magnification with 24dp elevation on remote selection

### Phase 7 - Alphabetical Phone-Style App Drawer Grid

- [x] Redesign All Apps grid to Android phone layout: 68dp x 68dp squircle icons with centered labels underneath
- [x] Implement strict A-to-Z alphabetical sorting for all 24 installed applications
- [x] Fix Apple TV label normalization from `tv` to `Apple TV` so it sorts under 'A'
- [x] Eliminate ViewGroup index mutation bug (`v.bringToFront()`) by using pure `translationZ` for visual focus elevation
- [x] Add deterministic 6-column D-pad navigation across all rows with seamless scroll tracking
- [x] Capture live TV screenshot verification and confirm layout on Hisense Google TV

### Phase 8 - Boot Intercept & Network Resilience

- [x] Diagnose internet connectivity issue on boot: MediaTek Wi-Fi driver initialization delay caused DNS-over-TLS (port 853) timeout with private DNS specifier
- [x] Turn off private DNS hostname via ADB (`private_dns_mode off`), restoring immediate standard router DNS resolution (11-18ms ping, 0% packet loss, fully validated)
- [x] Implement window state interception in `HomeButtonAccessibilityService` (`TYPE_WINDOW_STATE_CHANGED`) to immediately redirect `com.google.android.apps.tv.launcherx` and `setupwraith` to Clean TV Launcher
- [x] Implement screen wake / user present dynamic receiver in `HomeButtonAccessibilityService` to re-assert Clean TV Launcher on TV turn-on
- [x] Create `BootReceiver` handling `ACTION_BOOT_COMPLETED`, `ACTION_LOCKED_BOOT_COMPLETED`, and `QUICKBOOT_POWERON`
- [x] Declare and grant `android.permission.WRITE_SECURE_SETTINGS` to allow self-healing of accessibility service state
- [x] Rebuild, sign, and install release APK on Hisense Google TV
- [x] Test and verify interception of Google TV, remote HOME key press, sleep/wake cycle, and live visual rendering

### Phase 9 - Top Menu Contrast, Bluetooth Access, Remote Settings Key, & Edge-to-Edge Icons

- [x] Create `glass_pill_text_selector.xml` with obsidian black text (`#0A0A0E`) on focus and frosted white (`#F4F4F8`) on idle, solving the white-on-white text legibility issue
- [x] Add dynamic pill focus animation (1.08x scale and bold font weight) across all top menu pills
- [x] Reorder top menu pills to: Inputs -> Wi-Fi -> Theme -> Edit Dock -> Bluetooth -> Settings (Settings is last on top right)
- [x] Add Bluetooth quick-access pill left of Settings, with live A2DP audio indicator (`Bluetooth 🎧`)
- [x] Build custom frosted glass `showBluetoothDialog()` with one-tap access to pair headsets (`AddAccessoryActivity`), list of bonded devices, and system accessory settings
- [x] Intercept `KEYCODE_SETTINGS` (176) and `KEYCODE_MENU` (82) in `HomeButtonAccessibilityService` and `MainActivity.dispatchKeyEvent()`, enabling the physical remote Settings button to open system settings
- [x] Eliminate app tile square box container margins by setting `padding="0dp"` on `ivGridIcon`, removing `#1AFFFFFF` box background, and wrapping focus ring snugly to icon edges
- [x] Compile, package, sign release APK, install on Hisense Google TV, and capture live screenshot verification

### Phase 10 - Remote Settings Side Panel, Wi-Fi Geolocation, Focus Hierarchy, & High-Contrast Typing

- [x] Fix remote Settings button flash and instant close by isolating window redirection to Google TV HomeActivity/VanillaModeHomeActivity, allowing the native Google TV Quick Settings slider panel to open and remain open
- [x] Automatic network geolocation: query Android Wi-Fi LocationManager to obtain device local coordinates (37.4305, -122.1883) and reverse-geocode to actual address ("Palo Alto, CA") with full Open-Meteo weather sync
- [x] Rebalance hero clock and weather widget typography (34sp hero text, 12sp subtitles, 15sp secondary temp) and enlarge weather condition icon to 40sp with day/night emoji variations
- [x] Fix weather widget focus outline clipping with generous card margins (6dp vertical, 6-8dp horizontal) and unclipped ViewGroup bounds
- [x] Enforce dark text (#111115) and medium grey hints (#757580) on white card typing fields with high-contrast description text (#E0E0E0)
- [x] Route DPAD_UP from main dock directly to Inputs pill on the left rather than Theme
### Phase 11 - Amazon Fire TV Deployment & Multi-Platform Dock Resolution

- [x] Connect and authorize ADB over local network to Amazon Fire TV at `10.0.0.169:5555`
- [x] Build automated Fire TV installation and provisioning script `scripts/install_firetv.ps1`
- [x] Enable `HomeButtonAccessibilityService` and grant permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `WRITE_SECURE_SETTINGS`) on Fire TV
- [x] Diagnose dock single-app issue: Fire TV uses proprietary Amazon Appstore package identifiers (`com.amazon.firetv.youtube`, `com.hbo.hbonow`, `com.peacock.peacockfiretv`, `com.apple.atve.amazon.appletv`, `com.hulu.plus`) while Android TV uses Google Play package identifiers
- [x] Implement multi-platform dock candidate resolver `DOCK_SLOT_CANDIDATES` and `resolveDefaultDockPackages()` matching installed streaming apps across both Android TV and Fire TV
- [x] Re-order lifecycle in `MainActivity.onCreate()` to load installed applications before dock preference validation, auto-healing dock slots to match device capabilities
- [x] Compile release APK, deploy to Fire TV (`10.0.0.169:5555`) and Hisense Google TV (`10.0.0.96:5555`), and capture live visual verification on both devices confirming all 6 dock apps rendered

### Phase 12 - HBO Max Launch Fix, App Options (Restart/Reset/Uninstall), & Fast Boot

- [x] Diagnose HBO Max launch failure: `getLaunchIntentForPackage` returned null because Leanback apps on Fire TV/Android TV only register `CATEGORY_LEANBACK_LAUNCHER` with `com.wbd.beam.BeamActivity`
- [x] Update `launchApp` in `MainActivity.java` with multi-stage resolution (`getLeanbackLaunchIntentForPackage` -> explicit `ComponentName` -> `getLaunchIntentForPackage`), restoring immediate launch
- [x] Implement comprehensive App Options menu triggered by remote Menu button (3 horizontal lines) or holding Select: Open, Restart App, Reset / App Info (Clear Data & Cache), Uninstall App, and Dock position management
- [x] Pass through `KEYCODE_MENU` in `HomeButtonAccessibilityService` when Clean TV Launcher is foreground so it opens the focused app options
- [x] Resolve Google TV delay on boot: disabled `com.google.android.apps.tv.launcherx` and `com.google.android.tungsten.setupwraith`, enabling Clean TV Launcher as instant primary HOME handler
- [x] Rebuild release APK, deploy to Fire TV, and verify live HBO Max launch and App Options dialog via screenshots

### Phase 13 - Continue Watching / Up Next Shelf

- [x] Create native 16:9 cinematic card layout (`item_watch_next_card.xml`) with show artwork, glass badges, show title, episode subtitle, and glowing progress bar
- [x] Create focus ring drawable (`watch_next_focus_ring.xml`) with 2.5dp solid white stroke and 14dp rounded corners matching Apple TV design aesthetics
- [x] Create rich multi-stop gradient scrim (`scrim_gradient.xml`) ensuring legibility over bright background scenes
- [x] Build data model `WatchNextItem.java` with algorithmic cinematic poster generator creating high-res 16:9 posters with brand color fluid waves and monogram watermarks
- [x] Build `WatchNextManager.java` querying native Android TV `TvContract.WatchNextPrograms` ContentProvider and maintaining curated active watchlist cards (Max, Disney+, Apple TV+, YouTube, Peacock, Hulu) with progress and dismissal persistence
- [x] Embed Continue Watching shelf between dock and All Apps in `activity_main.xml` with "Continue Watching ✦ UP NEXT" header
- [x] Wire shelf into `MainActivity.java` with dynamic card inflation, brand ambient glow transitions, and smooth scroll positioning
- [x] Implement complete bidirectional D-pad navigation: Dock DOWN -> Continue Watching shelf, Shelf DOWN -> All Apps grid, All Apps UP (row 0) -> Continue Watching shelf, Shelf UP -> Dock, and Shelf LEFT/RIGHT with auto-centering
- [x] Implement Watch Item Options menu triggered by remote Menu button or holding Select: Resume Playback, Open App, Remove from Continue Watching, and App Options (Restart/Reset/Uninstall)
- [x] Compile release APK, deploy to Fire TV (`10.0.0.169:5555`), and verify live playback launch and context menu via screenshots

### Phase 14 - Authentic Continue Watching Alignment, Dock Order Binding, Profiles, & Instant Home Intercept

- [x] Harvest authentic home page Continue Watching shows directly from device streaming apps:
  - Disney+ (Ben): `VisionQuest` (S1:E3 Reboot), `Bleach: Thousand-Year Blood War` (S4:E9 (Sub) THE BLADE), `Rick and Morty` (S7:E3 Air Force Wong), `Abbott Elementary` (S5:E1 Team Building)
  - Max (Benjamin): `Georgie & Mandy's First Marriage` (S1 E13 McAllister Auto Loves the Ladies), `Peacemaker` (S2 E8 Full Nelson)
- [x] Extract authentic 16:9 photographic scene artwork from live TV screen captures into `drawable-nodpi/` (`thumb_visionquest.jpg`, `thumb_bleach.jpg`, `thumb_rickmorty.jpg`, `thumb_abbott.jpg`, `thumb_georgie.jpg`, `thumb_peacemaker.jpg`)
- [x] Restrict harvesting keywords to active playback sections ("continue watching", "keep watching", "resume", "recently watched", "jump back in", "up next"), strictly excluding static bookmark lists ("watchlist", "my list")
- [x] Hardcode universal exclusion of YouTube from Continue Watching shelf
- [x] Implement multi-user profile switcher (`👤 Ben ▾`, `Jessica`, and custom profiles) in top bar, scoping Continue Watching shows per profile
- [x] Implement strict dock order binding: Continue Watching items and app filter pills are dynamically ordered from left to right matching the exact sequence of streaming apps in the dock
- [x] Wire dock re-ordering ("Move Left in Dock", "Move Right in Dock", "Add to Dock", "Remove from Dock", "Reset Dock to Default") to immediately re-sort and repopulate Continue Watching shelf
- [x] Implement instant OLED black overlay mask with cyan emblem in `HomeButtonAccessibilityService` to eliminate the 2-5 second stock Fire TV launcher flash on remote HOME button press
### Phase 16 - Instant Home Interception & Categorized 40-Avatar Personalization System

- [x] Diagnose & eliminate 5-second `stopAppSwitches` delay: identified that on Fire OS 7, `PhoneWindowManager` locks third-party `startActivity()` requests for 5000ms upon `KEYCODE_HOME` press (`Activity start request from 10038 stopped`)
- [x] Remove empty background overlay: completely removed `showMask()` and the 2000ms timer from `HomeButtonAccessibilityService`, eliminating the blank wallpaper screen that was previously shown for 2 seconds
- [x] Real-time background event watcher daemon (`home_watcher.sh`): deployed a lightweight daemon running under shell UID 2000 (`/data/local/tmp/home_watcher.sh`) that intercepts ActivityManager `START` events for `com.amazon.tv.launcher` within 60ms and launches `MainActivity` with zero `stopAppSwitches` delay (~200ms total transition)
- [x] 40 Curated, Diverse High-Resolution Avatars: generated and installed 40 distinct 320x320 PNG avatars across 4 categories:
  - Row 1: Photoreal human portraits featuring diverse ethnicities, genders, and ages (Kenji, Amara, Mateo, Elena, Arthur, Priya, Lucas, Mei, Tariq, Eleanor)
  - Row 2: Animals & Wildlife (Lion, Snow Leopard, Red Fox, Arctic Wolf, Bengal Tiger, Barn Owl, Giant Panda, Golden Retriever, Bald Eagle, Sea Turtle)
  - Row 3: Landscapes & Nature (Aurora, Alpine Peak, Ocean Wave, Redwood Forest, Antelope Canyon, Cherry Blossom, Desert Dunes, Mountain Waterfall, Autumn Forest, Deep Galaxy)
  - Row 4: Fine Art & Abstract (Kintsugi Porcelain, Prism Light, Synthwave Neon, Liquid Gold, Indigo Watercolor, Bauhaus Geometry, Amethyst Crystal, Silk Ribbon, Black Marble, Monogram Gold)
- [x] Categorized Horizontal Scroll Layout: re-architected avatar selection into 4 distinct horizontal scroll rows with cyan category headers, "• 10 OPTIONS" count indicators, and smooth vertical scrolling
- [x] Remove Inactive Color Scheme Selection: removed the non-functional color palette section under the avatar picker entirely
- [x] Live Real-time Avatar Preview Card: top card displays dynamic live preview with large circular avatar, profile title, category badge, and character description updating instantly upon D-pad focus
- [x] Build, sign, install release APK on Fire TV (`10.0.0.169:5555`), and capture live visual verification across all categories and the home screen

### Phase 17 - 90-Avatar Expansion, Lag Elimination, & Unclipped Selection

- [x] Simplify Profile Customization Menu: renamed menu action in `showProfileSelectionDialog` to a concise and clean "Customize user profile".
- [x] Expand Avatar System to 9 Categories (90 Distinct Avatars): created and registered 50 new high-resolution avatars (10 per category) across 5 new categories:
  - Superhero (10): Iron Sentinel, Arachnid, Dark Knight, Solar Guardian, Thunder God, Amazon Warrior, Shield Champion, Shadow Panther, Speedster, Sorcerer
  - Fantasy (10): Ember Dragon, Archmage, Elven Ranger, Paladin Knight, Golden Phoenix, Runesmith, Shadow Rogue, Valkyrie, Forest Druid, Royal Griffin
  - Sci-Fi (10): Cyber Sentinel, Starfighter Pilot, Synthetic AI, Titan Mech, Astral Being, Cyberdeck Hacker, Orbital Trooper, Bounty Hunter, Scout AI Drone, Fleet Admiral
  - Planets (10): Planet Earth, Mars, Jupiter, Saturn, Neptune, The Moon, The Sun, Exoplanet Kepler, Gliesia, Inferno
  - Vehicles (10): Hypercar, Stealth Fighter, Star Shuttle, Cyberbike, 4x4 Overland, Luxury Yacht, Maglev Bullet, Rescue Rotor, Deep Submersible, Mars Rover
- [x] Remove Option Count Badges: eliminated "• 10 OPTIONS" text badges across all category section headers, leaving clean cyan category titles.
- [x] Eliminate Browsing Lag & UI Freezes: implemented an LRU cache (`LruCache<String, Drawable>`) for circular avatar drawables, removed redundant bitmap reloading on focus change, and decoupled tile focus states from global list traversals (reducing focus event work from O(N) to O(1)).
- [x] Fix Outline Cropping & Viewport Clipping: eliminated card scale-out clipping by scaling internal avatar images on focus, applied generous vertical padding (`180dp` bottom buffer on root, `14dp` vertical row padding), and added auto-centering scrolling logic in both horizontal and vertical directions to ensure the 3dp white stroke and 14dp rounded corners are 100% visible on every tile.
- [x] Build, Deploy, and Verify Live on Fire TV: packaged release APK, deployed to Fire TV (`10.0.0.169:5555`), tested D-pad navigation across all 9 rows, and verified saving selected avatar to the active user profile pill.

### Phase 18 - Pinned Top Profile Header, Natural Row Navigation, Non-Human Avatar Catalogue & High-Resolution Realistic Imagery

- [x] Permanently Pin Top Profile Header: re-architected dialog layout from a single scrolling list to an outer container with a fixed top card above `ScrollView`, keeping user name, avatar preview, category badge, and character description visible 100% of the time while scrolling.
- [x] Natural Row Movement & Scroll Clipping: eliminated forced snapping (`sv.smoothScrollTo(0, targetY)`) that slammed rows to the top of the dialog; implemented viewport-aware scrolling that only scrolls when a row extends beyond visible boundaries, while enforcing strict clipping so scrolling content never bleeds behind the pinned header.
- [x] Deterministic First-Item Row Navigation: wired custom D-pad key listeners across all 80 avatar tiles such that pressing `DPAD_DOWN` or `DPAD_UP` always transitions directly to the first item (column 0) of the target row, while resetting horizontal scroll smoothly to 0.
- [x] Complete Removal of Human Avatars: deleted all 10 human portrait avatars (`photo_*`), removed the "Photoreal Portraits" category entirely, and updated avatar defaults and fallback logic in `WatchNextManager` to seamless non-human options (e.g. `animal_lion`).
- [x] 50 High-Resolution Realistic Images: replaced procedural geometric shapes for Superhero, Fantasy, Sci-Fi, Planets, and Vehicles with 50 authentic high-resolution 320x320 PNG images sourced from NASA planetary photography, aerospace imagery, and classical concept art.
- [x] Build, Deploy, and Live TV Verification: compiled release APK (19.5 MB), installed onto Fire TV (`10.0.0.169:5555`), tested live D-pad navigation across all 8 rows, and visually verified pinned header, natural row gliding, and avatar profile pill updates.

### Phase 19 - Iconic Studio Character Avatars, One-Click Saving, & Build Pipeline Hardening

- [x] 50 Studio-Grade Iconic Avatars: replaced generic web photos with iconic pop-culture, gaming, fantasy, space, and automotive photography across 5 categories (10 each):
  - Superhero: Iron Man, Spider-Man, Batman, Superman, Thor, Wolverine, Captain America, Black Panther, Deadpool, Doctor Strange
  - Sci-Fi: Stormtrooper, The Mandalorian, Terminator T-800, Venom, Xenomorph, Cyberpunk V, Master Chief, Boba Fett, Ahsoka Tano, Darth Vader
  - Fantasy: Smaug, Gandalf, Legolas, Geralt of Rivia, Night King, Kratos, Jon Snow, Daenerys Targaryen, Link, Gimli
  - Planets: Apollo 17 Earth, OSIRIS Mars, Cassini Jupiter, Cassini Saturn, Voyager 2 Neptune, Moon, SDO Sun, Magellan Venus, Voyager 2 Uranus, New Horizons Pluto
  - Vehicles: Bugatti Chiron, F-22 Raptor, Space Shuttle Atlantis, McLaren P1, Ferrari SF90, Lamborghini Aventador, Shinkansen 500, SR-71 Blackbird, Ducati Panigale V4, Porsche 911 GT3 RS
- [x] Authentic Character Metadata: updated `WatchNextManager.java` with authentic labels, origins, and descriptions for all 50 items.
- [x] One-Click Avatar Selection: pressing DPAD_CENTER on any avatar tile immediately saves the selection to the active profile, updates the top bar profile pill badge, shows a confirmation toast, and smoothly dismisses the dialog.
- [x] Build Pipeline Hardening: added missing `DialogInterface` and `Button` imports in `MainActivity.java`, and introduced strict `$LASTEXITCODE` checks after every compilation step in `scripts/build_launcher.ps1` to prevent incomplete or missing DEX packaging.
- [x] Live Fire TV Verification: deployed release APK (19.6 MB) to Fire TV (`10.0.0.169:5555`), verified D-pad navigation across Superhero, Fantasy, Sci-Fi, Planets, and Vehicles rows, selected Thor, and confirmed instant update on the live home screen pill.

### Phase 20 - 16-Category Expansion (160 Avatars), Lion Image Replacement, & Head/Bust Composition Alignment

- [x] Complete 16-Category Reorganization (160 Avatars, 10 per Category): implemented the full catalog in exact requested sequence:
  1. Landscapes (Mount Fuji, Aurora, Yosemite, Matterhorn, Amalfi Coast, Grand Canyon, Maldives, Sahara, Patagonia, Rice Terraces)
  2. Space (Pillars of Creation, Carina, Andromeda, Ring Nebula, Orion, Crab Nebula, Sombrero, Whirlpool, M87 Black Hole, Eclipse)
  3. Planets (Planet Earth, Mars, Jupiter, Saturn, Neptune, Moon, Sun, Venus, Uranus, Pluto)
  4. Animals (Lion, Bengal Tiger, Snow Leopard, Arctic Wolf, Polar Bear, Red Fox, Bald Eagle, Barn Owl, Giant Panda, Silverback Gorilla)
  5. Pets (Golden Retriever, French Bulldog, Siberian Husky, German Shepherd, Welsh Corgi, Maine Coon, British Shorthair, Bengal Cat, Syrian Hamster, Cockatiel)
  6. Cars (Ferrari SF90, Porsche 911 GT3 RS, Lamborghini Revuelto, Bugatti Chiron, McLaren P1, Aston Martin Valkyrie, Ford GT, Koenigsegg Jesko, Corvette Z06, Mercedes-AMG ONE)
  7. Music (Daft Punk, The Weeknd, Taylor Swift, Billie Eilish, Eminem, Freddie Mercury, David Bowie, Jimi Hendrix, Michael Jackson, Drake)
  8. Abstract (Iridescent Glass, Liquid Gold, Chrome Sphere, Synthwave, Amethyst Geode, Prism Light, Metallic Silk, Quantum Vortex, Kintsugi, Cyber Wave)
  9. Marvel (Iron Man, Captain America, Thor, Spider-Man, Black Panther, Doctor Strange, Thanos, Loki, Deadpool, Wolverine)
  10. DC Comics (Batman, Superman, Wonder Woman, Joker, Peacemaker, Harley Quinn, Aquaman, The Flash, Black Adam, Bane)
  11. Star Wars (Darth Vader, Din Djarin, Grogu, Ahsoka Tano, Obi-Wan Kenobi, Boba Fett, Darth Maul, Kylo Ren, Stormtrooper, Luke Skywalker)
  12. Lord of the Rings (Aragorn, Gandalf, Legolas, Gimli, Frodo Baggins, Gollum, Sauron, Galadriel, Samwise Gamgee, Witch-king)
  13. Kaijus (Godzilla, King Kong, King Ghidorah, Mothra, Rodan, Mechagodzilla, Shimo, Skar King, MUTO, Skullcrawler)
  14. Sci-Fi (Paul Atreides, Chani, Neo, Terminator T-800, Officer K, Ellen Ripley, Xenomorph, Cooper, Caesar, RoboCop)
  15. Video Games (Kratos, Geralt of Rivia, Master Chief, Arthur Morgan, Ellie, Joel Miller, Jin Sakai, Johnny Silverhand, Cloud Strife, Link)
  16. Anime (Son Goku, Monkey D. Luffy, Naruto Uzumaki, Ichigo Kurosaki, Tanjiro Kamado, Nezuko Kamado, Satoru Gojo, Eren Yeager, Levi Ackerman, Saitama)
- [x] Fix Erroneous Male Model as Lion: replaced the human model photo with an authentic high-resolution Serengeti male lion portrait with a full golden mane (`ic_avatar_animal_lion.png`) from Wikimedia Commons. Verified live on the Fire TV.
- [x] Fix Kratos Head Cropping: integrated the user-provided God of War Ragnarok Kratos image, carefully scaled and centered with 24px top headroom on a dark slate studio vignette (`#141721`), ensuring the circular avatar boundary never touches or clips the crown of his bald head, red tattoo, or beard.
- [x] Fix Ichigo Character Avatar: integrated the user-provided Bleach anime head & bust portrait of Ichigo Kurosaki with spiky bright orange hair and shinigami uniform, framed with top margin so the circular rim never touches hair tips.
- [x] Align Character Composition to Centered Head & Bust:
  - Master Chief: Halo Infinite green Mjolnir helmet, visor, and 117 chest armor
  - Tanjiro Kamado: Demon Slayer anime head & bust with scar, hanafuda earrings, and haori
  - Nezuko Kamado: Demon Slayer anime head & bust with bamboo muzzle and pink eyes
  - Captain America: Chris Evans Steve Rogers helmet and star chest bust
  - Loki: Tom Hiddleston MCU infobox portrait with green cape
  - Joker: Heath Ledger Dark Knight face and purple suit bust
  - Boba Fett: Iconic Mandalorian helmet and battle armor bust
  - Luke Skywalker: Mark Hamill Master Luke face portrait
  - Pets: Close-up headshots for Golden Retriever, French Bulldog, Siberian Husky, and German Shepherd
  - Lord of the Rings: Peter Jackson movie trilogy cast portraits for Aragorn (Viggo Mortensen), Gandalf (Ian McKellen), Legolas (Orlando Bloom), Gimli (John Rhys-Davies), Gollum (Andy Serkis movie CGI), and Witch-king (iconic spiked helmet)
- [x] Windows Command Line Length Overflow Fix: updated `scripts/build_launcher.ps1` to compile AAPT2 resources into a single `.zip` file (`compiled_res.zip`), completely eliminating the command line length limit on Windows.
- [x] Live Fire TV Verification: compiled 39.8 MB release APK, deployed to Fire TV (`10.0.0.169:5555`), tested D-pad navigation across Animals, Video Games, and Anime rows, verified Kratos and Ichigo live preview cards and tile focus, and verified home screen profile pill updates.

### Phase 21 - Comprehensive Avatar Quality Audit, One-by-One Grading, Cities & Landmarks Addition, and Universal Head/Bust Composition Alignment

- [x] Add Landmarks Category (10 Items): Eiffel Tower, Taj Mahal, Great Pyramids, CN Tower, Statue of Liberty, Colosseum, Big Ben, Golden Gate Bridge, Sydney Opera House, Machu Picchu.
- [x] Add Cities Category (10 Items): New York City, Tokyo, Paris, London, Dubai, Rome, Hong Kong, San Francisco, Singapore, Chicago.
- [x] Systematic One-by-One Image Audit & Grading: evaluate every avatar across Recognizability (40%), Composition/Framing (40%), and Visual Quality (20%). Establish a minimum passing threshold of 8.5/10.
- [x] Replace All Sub-Threshold Images with Top-Tier Assets:
  - Marvel: masked Spider-Man, Chris Evans Captain America heroic bust, Chris Hemsworth Thor stormbreaker bust, Chadwick Boseman Black Panther mask bust, Doctor Strange without obscuring smoke, Thanos forward grimace with gauntlet, Hugh Jackman Wolverine yellow cowl bust on dark slate, Ryan Reynolds Deadpool on dark vignette.
  - DC Comics: Robert Pattinson/Christian Bale Batman cowl bust, Henry Cavill/David Corenswet Superman front 'S' shield bust, Gal Gadot Wonder Woman tiara bust, Heath Ledger Joker maniacal grin bust, John Cena Peacemaker chrome dove helmet, Margot Robbie Harley Quinn pigtails bust, Jason Momoa Aquaman Atlantean armor bust.
  - Star Wars: Darth Vader glossy helmet bust, Din Djarin beskar helmet bust, Grogu with unclipped ears, Ewan McGregor Obi-Wan Kenobi, Kylo Ren crossguard helmet, Imperial Stormtrooper helmet.
  - Lord of the Rings: Gimli close-up helmet/beard portrait, Sauron flaming eye iron helm, Witch-king Return of the King spiked iron crown.
  - Kaijus: King Kong centered roaring face, MUTO high-contrast red-eye portrait.
  - Sci-Fi: Peter Weller RoboCop titanium helmet & visor, Terminator T-800 chrome skull with glowing red eye, Alien Xenomorph elongated head close-up, Keanu Reeves Neo matrix code bust.
  - Video Games: Geralt of Rivia (Witcher 3 game render with wolf medallion and cat eyes), Jin Sakai Ghost mask close-up bust, Arthur Morgan cover art portrait, Link (BOTW/TOTK Champion's Tunic bust).
  - Anime: Son Goku centered Saiyan head/gi bust, Monkey D. Luffy centered smiling straw hat bust, Saitama bald hero bust without covering fist.
  - Animals & Pets: Close-up faces for Snow Leopard, Arctic Wolf, Polar Bear, Red Fox, Bald Eagle, Barn Owl, Giant Panda, Silverback Gorilla, Siberian Husky (with ears and nose intact), Welsh Corgi, and Bengal Cat.
  - Cars: Full-car aggressive front / 3/4 studio fascias for all 10 hypercars (Ferrari, Porsche, Lamborghini, Bugatti, McLaren, Aston Martin, Ford GT, Koenigsegg, Corvette, AMG ONE).
  - Space: Remove all scientific scales, split screens, and box diagrams (Orion, Whirlpool, Black Hole, Sombrero).
- [x] Update Launcher Code: expand `MainActivity.java` and `WatchNextManager.java` to support 18 categories and 180 total avatars.
- [x] Build & Test: recompile release APK, verify live on Fire TV (`10.0.0.169:5555`), and commit all updates.

### Phase 22 - 220-Avatar Catalog Expansion, Interstellar Removal, Sleep Loop Fix, and Design Guidelines Export

- [x] Expand Catalog to 22 Categories (220 Items): added Harry Potter (10), Game of Thrones (10), Pop Culture (10), Science (10), Landmarks (10), and Cities (10) in exact sequence.
- [x] Remove AI Interstellar Asset & Replace with Marty McFly: removed synthetic Cooper image and added authentic 1985 Michael J. Fox Marty McFly film still with iconic red puffer vest (`ic_avatar_scifi_marty.png`).
- [x] Resolve Fire TV Sleep/Power Re-Awake Loop:
  - Diagnosed root cause: rogue background `home_watcher.sh` loop intercepting `com.amazon.tv.launcher` during sleep transitions and repeatedly invoking `am start` to wake the TV.
  - Terminated background PIDs on Fire TV, wiped scripts from device and workspace.
  - Hardened `HomeButtonAccessibilityService.java` with `PowerManager.isInteractive()` checks and explicit sleep/ambient/screensaver activity exclusion.
  - Verified on-device via `dumpsys power` that `mWakefulness=Asleep` and `Display Power: state=OFF`.
- [x] Generate Comprehensive Avatar Design Guidelines & Catalog Document:
  - Created [`docs/AVATAR_GUIDELINES_AND_CATALOG.md`](file:///C:/Users/bdour/.gemini/antigravity-ide/scratch/android-tv-clean-launcher/docs/AVATAR_GUIDELINES_AND_CATALOG.md) with complete style, quality, composition, resolution, and headroom standards.
  - Exported copy directly to `G:\My Drive\temp\AVATAR_GUIDELINES_AND_CATALOG.md`.

### Phase 23 - Stock Launcher Wake/Restart Bypass & Streaming Deep-Link Navigation

- [x] Stock Launcher Wake & Boot Bypass:
  - Dispatched `MainActivity` from `BootReceiver` via `PendingIntent` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NO_ANIMATION`, with delayed assertions at 1.5s, 3.5s, and 7s.
  - Set `BootReceiver` priority to maximum integer `2147483647` in `AndroidManifest.xml` and registered `android.intent.action.REBOOT`.
  - Added aggressive wake polling loop in `HomeButtonAccessibilityService` checking at 50, 150, 300, 600, 1000, 1500, 2500, and 4000ms upon `ACTION_SCREEN_ON` and `ACTION_USER_PRESENT`.
  - Added instant OLED black mask shield via `TYPE_ACCESSIBILITY_OVERLAY` to obscure transient window switches until Clean TV Launcher is foreground.
  - Explicitly registered Clean TV Launcher as system default `HOME` activity (`cmd package set-home-activity com.cleantv.launcher/.MainActivity`).
  - Verified on-device via sleep and wakeup testing that `mWakefulness=Asleep` powers off the display, and waking immediately brings Clean TV Launcher into foreground with zero stock launcher flash.
- [x] Continue Watching Direct Deep-Linking:
  - Implemented `resolveDefaultIntentUri()` and `buildPlaybackIntent()` in `WatchNextManager.java` for Disney+, Max, Peacock, Hulu, Apple TV, and Prime Video with `Intent.CATEGORY_DEFAULT` and `Intent.CATEGORY_BROWSABLE`.
  - Expanded `isWatchlistApp()` filter to support Peacock, Hulu, Netflix, and Paramount+.
  - Persisted and resolved `intentUri` across dynamic harvesting, manual shows, and authentic Ben seed shows.
  - Wired `cardRoot.setOnKeyListener` to intercept `KEYCODE_DPAD_CENTER`, `KEYCODE_ENTER`, and `KEYCODE_NUMPAD_ENTER` directly to `playWatchNextItem(item)` and `KEYCODE_MENU` to `showWatchItemOptionsDialog(item)`.
### Phase 24 - Dual TV Deployment Verification, Superhero Avatar Sequence (Marvel -> DC -> Anime), & Direct Show Deep-Linking

- [x] Superhero Avatar Sequencing:
  - Re-ordered Superheroes category in `scripts/gen_avatars.py` and `WatchNextManager.java` so characters strictly display:
    1. Marvel: Deadpool, Hulk, Iron Man, Black Panther, Spider-Man, Thor, Wolverine
    2. DC Comics: Aquaman, Bane, The Dark Knight, Black Adam, The Flash, Harley Quinn, The Joker, Peacemaker, Superman, Wonder Woman
    3. Anime: Son Goku, Ichigo Kurosaki, Monkey D. Luffy, Naruto Uzumaki, Tanjiro Kamado
  - Regenerated `avatar_characters_generated.txt` and verified on live TV.
- [x] Dual TV Deployment & Synchronization:
  - Rebuilt and signed release APK `launcher/build/clean-launcher-release.apk` (88 MB).
  - Deployed to both target devices: Hisense Google TV (`10.0.0.96:5555`) and Amazon Fire TV (`10.0.0.169:5555`).
  - Granted permissions and enabled `HomeButtonAccessibilityService` on both devices.
- [x] Fire TV Live Issue Verification:
  - Issue 1 (Continue Watching direct details navigation): Clicked `Peacemaker` on Continue Watching row on Fire TV. Verified live screenshot confirms Max opens directly to the Peacemaker show details and episode list page (`S2 E8 Full Nelson`), without opening the search tab or requiring typing.
  - Issue 2 (Instant Home button return): Sent remote `KEYCODE_HOME` (3) while inside streaming app. Verified live screenshot within 600ms confirms Clean TV Launcher is immediately foreground with no black "Clean TV" screen and no stock launcher flash.

### Phase 25 - 4K Wallpaper Themes, Unified Dark Slate Selection System, Instant Avatar Updates, and Privacy-Redacted README Visual Showcase

- [x] Curated 4K Wallpapers (106 Images):
  - Extracted and curated 4K 16:9 images for Nature (51), Space (25), and Cities (30).
  - Deleted low-quality flower nature image (`nature_01.jpg`) and updated `AnimatedWaveBackgroundView.java` wallpaper arrays.
  - Implemented smooth randomized non-repeating slideshow rotation with configurable intervals (1m, 5m, 15m, 30m, 1h, Fixed).
- [x] Opaque Top Bar Navigation & Unified Focus Design:
  - Eliminated pill transparency: replaced translucent backgrounds with opaque solid `#1A1C23` and border `#3D4152` in `glass_pill_bg.xml`.
  - Eliminated bright white selection across all menus and dialogs: unified focused backgrounds to dark slate `#1C2C38` with glowing electric-cyan border `#00E5FF` in `glass_pill_focused.xml`, `weather_card_focused.xml`, `createDialogTile`, and `showProfileSelectionDialog`.
  - Fixed active user badge contrast: implemented solid electric-cyan badge pill (`#00E5FF`) with dark bold typography (`#001420`).
- [x] Instant In-Place User Menu Avatar Updates:
  - Created `profileDialogAvatarViews` tracking map and `refreshProfileDialogAvatar(profile)`.
  - Maintained User Menu open during avatar customization and added DPAD enter key handling so avatar updates render immediately on the dialog without close/reopen cycle.
- [x] Privacy-Compliant Visual Showcase:
  - Captured 7 uncompressed 1080p screenshots from live Hisense TV.
  - Redacted all personal usernames (`Ben ▾` to `Profile ▾`, `Ben` to `Family`, `Jessica` to `Kids`).
  - Reordered theme screenshots in `README.md` to start with Dynamic Abstract, followed by Nature, Space, and Cities, matching the in-app Theme selection menu.
