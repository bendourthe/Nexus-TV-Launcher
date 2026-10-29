package com.cleantv.launcher;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WatchNextManager {
    private static final String TAG = "WatchNextManager";
    private static final String PREFS_NAME = "CleanTvWatchNextPrefs";
    private static final String KEY_DISMISSED = "dismissed_ids";
    private static final String KEY_LAST_WATCHED_PKG = "last_watched_pkg";
    private static final String KEY_CUSTOM_ITEMS_PREFIX = "custom_shows_json_";
    public static final String KEY_CURRENT_PROFILE = "current_launcher_profile";
    public static final String KEY_PROFILES_LIST = "launcher_profiles_list";
    public static final String DEFAULT_PROFILE = "Ben";

    public static class AppFilter {
        public final String key;
        public final String label;
        public final int count;
        public final int brandColor;

        public AppFilter(String key, String label, int count, int brandColor) {
            this.key = key;
            this.label = label;
            this.count = count;
            this.brandColor = brandColor;
        }
    }

    public static String getCurrentProfile(Context context) {
        if (context == null)
            return DEFAULT_PROFILE;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_CURRENT_PROFILE, DEFAULT_PROFILE);
    }

    public static void setCurrentProfile(Context context, String profile) {
        if (context == null || profile == null || profile.trim().isEmpty())
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_CURRENT_PROFILE, profile.trim()).apply();
        ProfileSyncManager.onLocalProfileChanged(context);
    }

    public static List<String> getProfilesList(Context context) {
        List<String> list = new ArrayList<>();
        if (context == null) {
            list.add("Ben");
            list.add("Jessica");
            return list;
        }
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_PROFILES_LIST, null);
        if (raw == null || raw.trim().isEmpty()) {
            list.add("Ben");
            list.add("Jessica");
            return list;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                String p = arr.getString(i).trim();
                if (!p.isEmpty() && !list.contains(p)) {
                    list.add(p);
                }
            }
        } catch (Throwable t) {
            list.add("Ben");
            list.add("Jessica");
        }
        if (!list.contains("Ben"))
            list.add(0, "Ben");
        return list;
    }

    public static void addProfile(Context context, String newProfile) {
        if (context == null || newProfile == null || newProfile.trim().isEmpty())
            return;
        List<String> profiles = getProfilesList(context);
        String trimmed = newProfile.trim();
        if (!profiles.contains(trimmed)) {
            profiles.add(trimmed);
            JSONArray arr = new JSONArray();
            for (String p : profiles)
                arr.put(p);
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_PROFILES_LIST, arr.toString()).apply();
            ProfileSyncManager.onLocalProfileChanged(context);
        }
    }

    public static final String KEY_PROFILE_AVATAR_PREFIX = "profile_avatar_";

    public static class AvatarTheme {
        public final String id;
        public final String label;
        public final int primaryColor;
        public final int gradientEnd;

        public AvatarTheme(String id, String label, int primaryColor, int gradientEnd) {
            this.id = id;
            this.label = label;
            this.primaryColor = primaryColor;
            this.gradientEnd = gradientEnd;
        }
    }

    public static final List<AvatarTheme> AVATAR_THEMES = Arrays.asList(
            new AvatarTheme("blue", "Electric Sapphire", 0xFF2563EB, 0xFF3B82F6),
            new AvatarTheme("coral", "Sunset Coral", 0xFFE11D48, 0xFFFB7185),
            new AvatarTheme("emerald", "Emerald Green", 0xFF059669, 0xFF10B981),
            new AvatarTheme("violet", "Royal Amethyst", 0xFF7C3AED, 0xFF8B5CF6),
            new AvatarTheme("amber", "Solar Amber", 0xFFD97706, 0xFFF59E0B),
            new AvatarTheme("cyan", "Cyan Frost", 0xFF0891B2, 0xFF06B6D4),
            new AvatarTheme("rose", "Rose Quartz", 0xFFDB2777, 0xFFF472B6),
            new AvatarTheme("slate", "Obsidian Slate", 0xFF475569, 0xFF64748B));

    public static AvatarTheme getAvatarTheme(String themeId, String profile) {
        if (themeId != null) {
            for (AvatarTheme t : AVATAR_THEMES) {
                if (t.id.equalsIgnoreCase(themeId))
                    return t;
            }
        }
        if (profile != null) {
            String lower = profile.toLowerCase().trim();
            if (lower.contains("ben"))
                return AVATAR_THEMES.get(0);
            if (lower.contains("jess"))
                return AVATAR_THEMES.get(1);
            int idx = Math.abs(profile.hashCode()) % AVATAR_THEMES.size();
            return AVATAR_THEMES.get(idx);
        }
        return AVATAR_THEMES.get(0);
    }

    public static String getProfileAvatar(Context context, String profile) {
        if (context == null || profile == null)
            return "blue";
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = sp.getString(KEY_PROFILE_AVATAR_PREFIX + profile.toLowerCase().trim(), null);
        if (saved != null && !saved.trim().isEmpty()) {
            for (AvatarTheme t : AVATAR_THEMES) {
                if (t.id.equalsIgnoreCase(saved.trim()))
                    return t.id;
            }
        }
        String lower = profile.toLowerCase().trim();
        if (lower.contains("ben"))
            return "blue";
        if (lower.contains("jess"))
            return "coral";
        int idx = Math.abs(profile.hashCode()) % AVATAR_THEMES.size();
        return AVATAR_THEMES.get(idx).id;
    }

    public static void setProfileAvatar(Context context, String profile, String avatarId) {
        if (context == null || profile == null || avatarId == null)
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PROFILE_AVATAR_PREFIX + profile.toLowerCase().trim(), avatarId.trim()).apply();
        ProfileSyncManager.onLocalProfileChanged(context);
    }

    public static class AvatarCharacter {
        public final String id;
        public final String label;
        public final String category;
        public final String description;
        public final int drawableRes;

        public AvatarCharacter(String id, String label, String category, String description, int drawableRes) {
            this.id = id;
            this.label = label;
            this.category = category;
            this.description = description;
            this.drawableRes = drawableRes;
        }
    }

    public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays.asList(
            // --- Category: Landscapes (10 items) ---
            new AvatarCharacter("land_amalfi", "Amalfi Coast", "Landscapes", "Sun-drenched cliffs of Positano",
                    R.drawable.ic_avatar_land_amalfi),
            new AvatarCharacter("land_aurora", "Aurora Borealis", "Landscapes", "Vibrant northern lights in night sky",
                    R.drawable.ic_avatar_land_aurora),
            new AvatarCharacter("land_bali", "Bali Terraces", "Landscapes", "Lush tropical emerald rice terraces",
                    R.drawable.ic_avatar_land_bali),
            new AvatarCharacter("land_fuji", "Mount Fuji", "Landscapes", "Majestic snow-capped volcanic peak",
                    R.drawable.ic_avatar_land_fuji),
            new AvatarCharacter("land_grandcanyon", "Grand Canyon", "Landscapes", "Vast red rock canyon panorama",
                    R.drawable.ic_avatar_land_grandcanyon),
            new AvatarCharacter("land_maldives", "Maldives Lagoon", "Landscapes", "Crystal turquoise atoll waters",
                    R.drawable.ic_avatar_land_maldives),
            new AvatarCharacter("land_matterhorn", "Matterhorn", "Landscapes", "Iconic jagged alpine mountain summit",
                    R.drawable.ic_avatar_land_matterhorn),
            new AvatarCharacter("land_patagonia", "Torres del Paine", "Landscapes",
                    "Dramatic granite horns and glacial lakes", R.drawable.ic_avatar_land_patagonia),
            new AvatarCharacter("land_sahara", "Sahara Dunes", "Landscapes", "Golden rolling desert sand dunes",
                    R.drawable.ic_avatar_land_sahara),
            new AvatarCharacter("land_yosemite", "Yosemite Valley", "Landscapes",
                    "Towering granite cliffs and misty pines", R.drawable.ic_avatar_land_yosemite),
            // --- Category: Cities (20 items) ---
            new AvatarCharacter("city_dubai", "Dubai", "Cities", "Futuristic skyline and Burj Khalifa",
                    R.drawable.ic_avatar_city_dubai),
            new AvatarCharacter("city_hongkong", "Hong Kong", "Cities", "Neon-lit Victoria Harbour skyline",
                    R.drawable.ic_avatar_city_hongkong),
            new AvatarCharacter("city_lasvegas", "Las Vegas", "Cities", "Dazzling illuminated casino strip",
                    R.drawable.ic_avatar_city_lasvegas),
            new AvatarCharacter("city_london", "London", "Cities", "Thames riverfront and historic skyline",
                    R.drawable.ic_avatar_city_london),
            new AvatarCharacter("city_nyc", "New York City", "Cities", "Iconic Manhattan towers at twilight",
                    R.drawable.ic_avatar_city_nyc),
            new AvatarCharacter("city_paris", "Paris", "Cities", "Haussmann boulevards and Parisian charm",
                    R.drawable.ic_avatar_city_paris),
            new AvatarCharacter("city_rome", "Rome", "Cities", "Ancient eternal city architecture",
                    R.drawable.ic_avatar_city_rome),
            new AvatarCharacter("city_sanfrancisco", "San Francisco", "Cities", "Foggy bay and Victorian hills",
                    R.drawable.ic_avatar_city_sanfrancisco),
            new AvatarCharacter("city_singapore", "Singapore", "Cities", "Lush Marina Bay architectural skyline",
                    R.drawable.ic_avatar_city_singapore),
            new AvatarCharacter("city_toronto", "Toronto", "Cities", "Lakefront skyline and bustling city center",
                    R.drawable.ic_avatar_city_toronto),
            new AvatarCharacter("landmark_bigben", "Big Ben", "Cities", "Gothic Westminster clock tower in London",
                    R.drawable.ic_avatar_landmark_bigben),
            new AvatarCharacter("landmark_cntower", "CN Tower", "Cities", "Iconic Toronto telecommunication spire",
                    R.drawable.ic_avatar_landmark_cntower),
            new AvatarCharacter("landmark_colosseum", "The Colosseum", "Cities",
                    "Ancient Roman gladiatorial amphitheatre", R.drawable.ic_avatar_landmark_colosseum),
            new AvatarCharacter("landmark_eiffel", "Eiffel Tower", "Cities", "Wrought-iron Parisian masterpiece",
                    R.drawable.ic_avatar_landmark_eiffel),
            new AvatarCharacter("landmark_goldengate", "Golden Gate Bridge", "Cities",
                    "Suspension bridge across San Francisco bay", R.drawable.ic_avatar_landmark_goldengate),
            new AvatarCharacter("landmark_liberty", "Statue of Liberty", "Cities",
                    "Beacon of freedom in New York Harbor", R.drawable.ic_avatar_landmark_liberty),
            new AvatarCharacter("landmark_machupicchu", "Machu Picchu", "Cities",
                    "Incan citadel nestled in Andean peaks", R.drawable.ic_avatar_landmark_machupicchu),
            new AvatarCharacter("landmark_operahouse", "Sydney Opera House", "Cities",
                    "Geometric sail-like harbour landmark", R.drawable.ic_avatar_landmark_operahouse),
            new AvatarCharacter("landmark_pyramids", "Great Pyramids", "Cities", "Ancient wonders of Giza plateau",
                    R.drawable.ic_avatar_landmark_pyramids),
            new AvatarCharacter("landmark_tajmahal", "Taj Mahal", "Cities", "Ivory-white marble mausoleum of Agra",
                    R.drawable.ic_avatar_landmark_tajmahal),
            // --- Category: Space (20 items) ---
            new AvatarCharacter("planet_earth", "Planet Earth", "Space", "Blue marble with swirling clouds",
                    R.drawable.ic_avatar_planet_earth),
            new AvatarCharacter("planet_gliesia", "Ice Giant", "Space", "Deep azure frozen gaseous atmosphere",
                    R.drawable.ic_avatar_planet_gliesia),
            new AvatarCharacter("planet_inferno", "Lava World", "Space", "Molten volcanic exoplanet surface",
                    R.drawable.ic_avatar_planet_inferno),
            new AvatarCharacter("planet_jupiter", "Jupiter", "Space", "Great red spot and gas giant storm bands",
                    R.drawable.ic_avatar_planet_jupiter),
            new AvatarCharacter("planet_kepler", "Goldilocks World", "Space", "Alien terrestrial habitable exoplanet",
                    R.drawable.ic_avatar_planet_kepler),
            new AvatarCharacter("planet_mars", "Mars", "Space", "Crimson rust deserts and polar ice caps",
                    R.drawable.ic_avatar_planet_mars),
            new AvatarCharacter("planet_moon", "The Moon", "Space", "Detailed lunar cratered surface",
                    R.drawable.ic_avatar_planet_moon),
            new AvatarCharacter("planet_neptune", "Neptune", "Space", "Deep ultramarine celestial sphere",
                    R.drawable.ic_avatar_planet_neptune),
            new AvatarCharacter("planet_saturn", "Saturn", "Space", "Magnificent golden rings in orbit",
                    R.drawable.ic_avatar_planet_saturn),
            new AvatarCharacter("planet_sun", "The Sun", "Space", "Fiery solar flares and coronal loops",
                    R.drawable.ic_avatar_planet_sun),
            new AvatarCharacter("space_andromeda", "Andromeda Galaxy", "Space", "Majestic spiral galaxy neighbor",
                    R.drawable.ic_avatar_space_andromeda),
            new AvatarCharacter("space_blackhole", "Black Hole", "Space", "Event horizon warping celestial light",
                    R.drawable.ic_avatar_space_blackhole),
            new AvatarCharacter("space_carina", "Carina Nebula", "Space", "Cosmic cliffs of star creation",
                    R.drawable.ic_avatar_space_carina),
            new AvatarCharacter("space_crab", "Crab Nebula", "Space", "Pulsar supernova remnant in deep space",
                    R.drawable.ic_avatar_space_crab),
            new AvatarCharacter("space_eclipse", "Total Solar Eclipse", "Space",
                    "Radiant solar corona shining behind moon", R.drawable.ic_avatar_space_eclipse),
            new AvatarCharacter("space_orion", "Orion Nebula", "Space", "Stellar nursery glowing in interstellar dust",
                    R.drawable.ic_avatar_space_orion),
            new AvatarCharacter("space_pillars", "Pillars of Creation", "Space", "Towering columns of gas and dust",
                    R.drawable.ic_avatar_space_pillars),
            new AvatarCharacter("space_ringnebula", "Ring Nebula", "Space", "Glowing colorful planetary nebula torus",
                    R.drawable.ic_avatar_space_ringnebula),
            new AvatarCharacter("space_sombrero", "Sombrero Galaxy", "Space", "Bright nucleus with broad dust ring",
                    R.drawable.ic_avatar_space_sombrero),
            new AvatarCharacter("space_whirlpool", "Whirlpool Galaxy", "Space",
                    "Grand design interacting spiral galaxy", R.drawable.ic_avatar_space_whirlpool),
            // --- Category: Animals (23 items) ---
            new AvatarCharacter("animal_bear", "Grizzly Bear", "Animals", "Powerful wild grizzly portrait",
                    R.drawable.ic_avatar_animal_bear),
            new AvatarCharacter("animal_eagle", "Bald Eagle", "Animals", "Fierce golden gaze of predatory raptor",
                    R.drawable.ic_avatar_animal_eagle),
            new AvatarCharacter("animal_fox", "Red Fox", "Animals", "Cunning scarlet coat in woodland snow",
                    R.drawable.ic_avatar_animal_fox),
            new AvatarCharacter("animal_gorilla", "Silverback Gorilla", "Animals", "Wise and dominant mountain primate",
                    R.drawable.ic_avatar_animal_gorilla),
            new AvatarCharacter("animal_leopard", "Snow Leopard", "Animals", "Ghost of the mountains with spotted coat",
                    R.drawable.ic_avatar_animal_leopard),
            new AvatarCharacter("animal_lion", "Male Lion", "Animals", "Majestic golden mane of the savanna king",
                    R.drawable.ic_avatar_animal_lion),
            new AvatarCharacter("animal_owl", "Great Horned Owl", "Animals", "Intense watchful night predator",
                    R.drawable.ic_avatar_animal_owl),
            new AvatarCharacter("animal_panda", "Giant Panda", "Animals", "Playful gentle bamboo bear",
                    R.drawable.ic_avatar_animal_panda),
            new AvatarCharacter("animal_tiger", "Bengal Tiger", "Animals",
                    "Fiery orange and black striped apex predator", R.drawable.ic_avatar_animal_tiger),
            new AvatarCharacter("animal_wolf", "Timber Wolf", "Animals", "Wild howling pack hunter",
                    R.drawable.ic_avatar_animal_wolf),
            new AvatarCharacter("pet_bengal", "Bengal Cat", "Animals", "Exotic leopard-spotted domestic feline",
                    R.drawable.ic_avatar_pet_bengal),
            new AvatarCharacter("pet_britishshorthair", "British Shorthair", "Animals",
                    "Plush blue-gray round-faced cat", R.drawable.ic_avatar_pet_britishshorthair),
            new AvatarCharacter("pet_cockatiel", "Cockatiel", "Animals", "Cheerful crested companion bird",
                    R.drawable.ic_avatar_pet_cockatiel),
            new AvatarCharacter("pet_corgi", "Pembroke Corgi", "Animals", "Spirited royal fox-eared herding dog",
                    R.drawable.ic_avatar_pet_corgi),
            new AvatarCharacter("pet_frenchie", "French Bulldog", "Animals", "Affectionate bat-eared companion pup",
                    R.drawable.ic_avatar_pet_frenchie),
            new AvatarCharacter("pet_germanshepherd", "German Shepherd", "Animals", "Loyal and noble working canine",
                    R.drawable.ic_avatar_pet_germanshepherd),
            new AvatarCharacter("pet_hamster", "Syrian Hamster", "Animals", "Cute fluffy cheek-pouching pet",
                    R.drawable.ic_avatar_pet_hamster),
            new AvatarCharacter("pet_horse", "Arabian Horse", "Animals", "Galloping noble equestrian beauty",
                    R.drawable.ic_avatar_pet_horse),
            new AvatarCharacter("pet_husky", "Siberian Husky", "Animals", "Blue-eyed arctic sled dog",
                    R.drawable.ic_avatar_pet_husky),
            new AvatarCharacter("pet_mainecoon", "Maine Coon", "Animals", "Gentle giant tufted-eared cat",
                    R.drawable.ic_avatar_pet_mainecoon),
            new AvatarCharacter("pet_retriever", "Golden Retriever", "Animals",
                    "Friendly warm-hearted family retriever", R.drawable.ic_avatar_pet_retriever),
            new AvatarCharacter("pet_siberian_husky", "Arctic Husky", "Animals",
                    "Silver-coated winter endurance runner", R.drawable.ic_avatar_pet_siberian_husky),
            new AvatarCharacter("pet_tulanne", "Toucan", "Animals", "Vibrant rainbow-billed tropical bird",
                    R.drawable.ic_avatar_pet_tulanne),
            // --- Category: Cars (10 items) ---
            new AvatarCharacter("car_amgone", "AMG One", "Cars", "Formula 1 hybrid hypercar powerhouse",
                    R.drawable.ic_avatar_car_amgone),
            new AvatarCharacter("car_aston", "Aston Martin", "Cars", "British handcrafted luxury supercar elegance",
                    R.drawable.ic_avatar_car_aston),
            new AvatarCharacter("car_bugatti", "Bugatti Chiron", "Cars", "Quad-turbo W16 record-breaking beast",
                    R.drawable.ic_avatar_car_bugatti),
            new AvatarCharacter("car_corvette", "Corvette Z06", "Cars", "Mid-engine American high-revving roar",
                    R.drawable.ic_avatar_car_corvette),
            new AvatarCharacter("car_ferrari", "Ferrari Daytona", "Cars", "Rosso Corsa sculpted Italian thoroughbred",
                    R.drawable.ic_avatar_car_ferrari),
            new AvatarCharacter("car_fordgt", "Ford GT", "Cars", "Carbon-fiber aerodynamic Le Mans winner",
                    R.drawable.ic_avatar_car_fordgt),
            new AvatarCharacter("car_koenigsegg", "Koenigsegg Jesko", "Cars", "Swedish megacar engineering triumph",
                    R.drawable.ic_avatar_car_koenigsegg),
            new AvatarCharacter("car_lambo", "Lamborghini Revuelto", "Cars", "V12 hybrid wedge aggressive hypercar",
                    R.drawable.ic_avatar_car_lambo),
            new AvatarCharacter("car_mclaren", "McLaren P1", "Cars", "Dihedral door aerodynamically honed racer",
                    R.drawable.ic_avatar_car_mclaren),
            new AvatarCharacter("car_porsche", "Porsche 911 GT3", "Cars", "Track-focused naturally aspirated icon",
                    R.drawable.ic_avatar_car_porsche),
            // --- Category: Pop Culture (24 items) ---
            new AvatarCharacter("game_chief", "Master Chief", "Pop Culture", "Spartan-117 legendary armored hero",
                    R.drawable.ic_avatar_game_chief),
            new AvatarCharacter("game_cloud", "Cloud Strife", "Pop Culture",
                    "Buster sword-wielding ex-SOLDIER hero of Gaia", R.drawable.ic_avatar_game_cloud),
            new AvatarCharacter("game_ellie", "Ellie Williams", "Pop Culture",
                    "Resilient survivor of the cordyceps outbreak", R.drawable.ic_avatar_game_ellie),
            new AvatarCharacter("game_geralt", "Geralt of Rivia", "Pop Culture",
                    "White Wolf monster hunter of Blaviken", R.drawable.ic_avatar_game_geralt),
            new AvatarCharacter("game_kratos", "Kratos", "Pop Culture", "Ghost of Sparta and Norse god of war",
                    R.drawable.ic_avatar_game_kratos),
            new AvatarCharacter("game_link", "Link", "Pop Culture", "Master Sword-bearing Hero of Hyrule",
                    R.drawable.ic_avatar_game_link),
            new AvatarCharacter("music_acdc", "Angus Young", "Pop Culture", "High-voltage electric guitar rock pioneer",
                    R.drawable.ic_avatar_music_acdc),
            new AvatarCharacter("music_billie", "Billie Eilish", "Pop Culture", "Genre-bending pop music visionary",
                    R.drawable.ic_avatar_music_billie),
            new AvatarCharacter("music_bowie", "David Bowie", "Pop Culture", "Ziggy Stardust glam rock alien icon",
                    R.drawable.ic_avatar_music_bowie),
            new AvatarCharacter("music_daftpunk", "Daft Punk", "Pop Culture", "Legendary chrome robotic electronic duo",
                    R.drawable.ic_avatar_music_daftpunk),
            new AvatarCharacter("music_freddie", "Freddie Mercury", "Pop Culture",
                    "Legendary Queen frontman and vocal titan", R.drawable.ic_avatar_music_freddie),
            new AvatarCharacter("music_jimi", "Jimi Hendrix", "Pop Culture", "Psychedelic electric guitar virtuoso",
                    R.drawable.ic_avatar_music_jimi),
            new AvatarCharacter("music_michael", "Michael Jackson", "Pop Culture", "King of Pop moonwalking phenomenon",
                    R.drawable.ic_avatar_music_michael),
            new AvatarCharacter("music_mileycyrus", "Miley Cyrus", "Pop Culture",
                    "Unapologetic rock and pop vocal powerhouse", R.drawable.ic_avatar_music_mileycyrus),
            new AvatarCharacter("pop_barbie", "Barbie", "Pop Culture", "Iconic fashionable cultural symbol",
                    R.drawable.ic_avatar_pop_barbie),
            new AvatarCharacter("pop_eleven", "Eleven", "Pop Culture", "Telekinetic girl who stopped the Upside Down",
                    R.drawable.ic_avatar_pop_eleven),
            new AvatarCharacter("pop_elvis", "Elvis Presley", "Pop Culture", "King of Rock and Roll charisma",
                    R.drawable.ic_avatar_pop_elvis),
            new AvatarCharacter("pop_godfather", "Vito Corleone", "Pop Culture",
                    "Patriarch of the Corleone crime empire", R.drawable.ic_avatar_pop_godfather),
            new AvatarCharacter("pop_indy", "Indiana Jones", "Pop Culture",
                    "Fedora-wearing whip-cracking archaeologist", R.drawable.ic_avatar_pop_indy),
            new AvatarCharacter("pop_jacksparrow", "Captain Jack Sparrow", "Pop Culture",
                    "Witty pirate lord of the Caribbean seas", R.drawable.ic_avatar_pop_jacksparrow),
            new AvatarCharacter("pop_marilyn", "Marilyn Monroe", "Pop Culture",
                    "Timeless Hollywood golden screen siren", R.drawable.ic_avatar_pop_marilyn),
            new AvatarCharacter("pop_wednesday", "Wednesday Addams", "Pop Culture", "Morbidly brilliant gothic sleuth",
                    R.drawable.ic_avatar_pop_wednesday),
            new AvatarCharacter("pop_wonka", "Willy Wonka", "Pop Culture", "Eccentric genius candymaker visionary",
                    R.drawable.ic_avatar_pop_wonka),
            new AvatarCharacter("scifi_caesar", "Caesar", "Pop Culture", "Evolved ape leader fighting for freedom",
                    R.drawable.ic_avatar_scifi_caesar),
            // --- Category: Superheroes (22 items) ---
            new AvatarCharacter("hero_deadpool", "Deadpool", "Superheroes", "Merc with a Mouth fourth-wall breaker",
                    R.drawable.ic_avatar_hero_deadpool),
            new AvatarCharacter("hero_hulk", "The Incredible Hulk", "Superheroes",
                    "Gamma-powered green powerhouse titan", R.drawable.ic_avatar_hero_hulk),
            new AvatarCharacter("hero_ironman", "Iron Man", "Superheroes", "Tony Stark armored genius philanthropist",
                    R.drawable.ic_avatar_hero_ironman),
            new AvatarCharacter("hero_panther", "Black Panther", "Superheroes", "King T-Challa protector of Wakanda",
                    R.drawable.ic_avatar_hero_panther),
            new AvatarCharacter("hero_spiderman", "Spider-Man", "Superheroes",
                    "Peter Parker friendly neighborhood webslinger", R.drawable.ic_avatar_hero_spiderman),
            new AvatarCharacter("hero_thor", "Thor Odinson", "Superheroes", "Asgardian god of thunder wielding Mjolnir",
                    R.drawable.ic_avatar_hero_thor),
            new AvatarCharacter("hero_wolverine", "Wolverine", "Superheroes",
                    "Logan adamantium-clawed mutant berserker", R.drawable.ic_avatar_hero_wolverine),
            new AvatarCharacter("dc_aquaman", "Aquaman", "Superheroes", "Arthur Curry King of the Seven Seas",
                    R.drawable.ic_avatar_dc_aquaman),
            new AvatarCharacter("dc_bane", "Bane", "Superheroes", "Tactical mastermind venom-fueled brute",
                    R.drawable.ic_avatar_dc_bane),
            new AvatarCharacter("dc_batman", "The Dark Knight", "Superheroes", "Gotham City caped crusader vigilante",
                    R.drawable.ic_avatar_dc_batman),
            new AvatarCharacter("dc_blackadam", "Black Adam", "Superheroes",
                    "Ancient champion empowered by Egyptian gods", R.drawable.ic_avatar_dc_blackadam),
            new AvatarCharacter("dc_flash", "The Flash", "Superheroes", "Barry Allen scarlet speedster of Central City",
                    R.drawable.ic_avatar_dc_flash),
            new AvatarCharacter("dc_harley", "Harley Quinn", "Superheroes", "Maid of mischief hammer-wielding rebel",
                    R.drawable.ic_avatar_dc_harley),
            new AvatarCharacter("dc_joker", "The Joker", "Superheroes", "Clown Prince of Crime chaotic genius",
                    R.drawable.ic_avatar_dc_joker),
            new AvatarCharacter("dc_peacemaker", "Peacemaker", "Superheroes",
                    "Christopher Smith peace at any cost zealot", R.drawable.ic_avatar_dc_peacemaker),
            new AvatarCharacter("dc_superman", "Superman", "Superheroes", "Kal-El Man of Steel from Krypton",
                    R.drawable.ic_avatar_dc_superman),
            new AvatarCharacter("dc_wonderwoman", "Wonder Woman", "Superheroes",
                    "Diana Prince Amazonian warrior of Themyscira", R.drawable.ic_avatar_dc_wonderwoman),
            new AvatarCharacter("anime_goku", "Goku", "Superheroes",
                    "Super Saiyan protector of Earth and universe champion", R.drawable.ic_avatar_anime_goku),
            new AvatarCharacter("anime_ichigo", "Ichigo Kurosaki", "Superheroes",
                    "Substitute Soul Reaper wielding Zangetsu", R.drawable.ic_avatar_anime_ichigo),
            new AvatarCharacter("anime_luffy", "Monkey D. Luffy", "Superheroes",
                    "Straw Hat pirate captain searching for One Piece", R.drawable.ic_avatar_anime_luffy),
            new AvatarCharacter("anime_naruto", "Naruto Uzumaki", "Superheroes",
                    "Nine-Tails Jinchuriki and Seventh Hokage", R.drawable.ic_avatar_anime_naruto),
            new AvatarCharacter("anime_tanjiro", "Tanjiro Kamado", "Superheroes",
                    "Sun breathing Demon Slayer and protective brother", R.drawable.ic_avatar_anime_tanjiro),
            // --- Category: Sci-Fi (15 items) ---
            new AvatarCharacter("scifi_bb8", "BB-8", "Sci-Fi", "Loyal astromech rolling sphere droid",
                    R.drawable.ic_avatar_scifi_bb8),
            new AvatarCharacter("scifi_chewbacca", "Chewbacca", "Sci-Fi", "Loyal Wookiee warrior and Falcon co-pilot",
                    R.drawable.ic_avatar_scifi_chewbacca),
            new AvatarCharacter("scifi_grogu", "Grogu", "Sci-Fi", "Force-sensitive adorable Foundling",
                    R.drawable.ic_avatar_scifi_grogu),
            new AvatarCharacter("scifi_luke", "Luke Skywalker", "Sci-Fi", "Jedi Knight who restored hope to the galaxy",
                    R.drawable.ic_avatar_scifi_luke),
            new AvatarCharacter("scifi_mando", "The Mandalorian", "Sci-Fi", "Din Djarin beskar-armored bounty hunter",
                    R.drawable.ic_avatar_scifi_mando),
            new AvatarCharacter("scifi_maul", "Darth Maul", "Sci-Fi", "Double-bladed lightsaber Sith warrior",
                    R.drawable.ic_avatar_scifi_maul),
            new AvatarCharacter("scifi_obiwan", "Obi-Wan Kenobi", "Sci-Fi",
                    "Wise Jedi Master and guardian of the Force", R.drawable.ic_avatar_scifi_obiwan),
            new AvatarCharacter("scifi_paul", "Paul Atreides", "Sci-Fi", "Muad-Dib prophesied savior of Arrakis",
                    R.drawable.ic_avatar_scifi_paul),
            new AvatarCharacter("scifi_r2d2", "R2-D2", "Sci-Fi", "Brave astromech droid and hero of the galaxy",
                    R.drawable.ic_avatar_scifi_r2d2),
            new AvatarCharacter("scifi_robocop", "RoboCop", "Sci-Fi", "Part man part machine all cop in Detroit",
                    R.drawable.ic_avatar_scifi_robocop),
            new AvatarCharacter("scifi_spock", "Mr. Spock", "Sci-Fi", "Logical Vulcan Starfleet first officer",
                    R.drawable.ic_avatar_scifi_spock),
            new AvatarCharacter("scifi_t800", "T-800 Terminator", "Sci-Fi", "Cyberdyne Systems Model 101 assassin",
                    R.drawable.ic_avatar_scifi_t800),
            new AvatarCharacter("scifi_vader", "Darth Vader", "Sci-Fi", "Dark Lord of the Sith ruler of the Empire",
                    R.drawable.ic_avatar_scifi_vader),
            new AvatarCharacter("scifi_xenomorph", "Xenomorph", "Sci-Fi", "Perfect apex biomechanical alien organism",
                    R.drawable.ic_avatar_scifi_xenomorph),
            new AvatarCharacter("scifi_yoda", "Master Yoda", "Sci-Fi", "Centuries-old Grand Master of the Jedi Order",
                    R.drawable.ic_avatar_scifi_yoda),
            // --- Category: Monsters (10 items) ---
            new AvatarCharacter("kaiju_ghidorah", "King Ghidorah", "Monsters", "Three-headed golden dragon false king",
                    R.drawable.ic_avatar_kaiju_ghidorah),
            new AvatarCharacter("kaiju_godzilla", "Godzilla", "Monsters", "Alpha predator King of the Monsters",
                    R.drawable.ic_avatar_kaiju_godzilla),
            new AvatarCharacter("kaiju_kong", "Kong", "Monsters", "King of Skull Island and Hollow Earth",
                    R.drawable.ic_avatar_kaiju_kong),
            new AvatarCharacter("kaiju_mechagodzilla", "Mechagodzilla", "Monsters",
                    "Apex biomechanical robotic titan weapon", R.drawable.ic_avatar_kaiju_mechagodzilla),
            new AvatarCharacter("kaiju_mothra", "Mothra", "Monsters", "Queen of the Monsters benevolent winged titan",
                    R.drawable.ic_avatar_kaiju_mothra),
            new AvatarCharacter("kaiju_muto", "MUTO", "Monsters", "Electromagnetic pulse parasite titan",
                    R.drawable.ic_avatar_kaiju_muto),
            new AvatarCharacter("kaiju_rodan", "Rodan", "Monsters", "Fire demon born of volcanic fury",
                    R.drawable.ic_avatar_kaiju_rodan),
            new AvatarCharacter("kaiju_shimo", "Shimo", "Monsters", "Ancient frost titan capable of an ice age",
                    R.drawable.ic_avatar_kaiju_shimo),
            new AvatarCharacter("kaiju_skarking", "Skar King", "Monsters",
                    "Cruel whip-wielding ruler of Hollow Earth apes", R.drawable.ic_avatar_kaiju_skarking),
            new AvatarCharacter("kaiju_skullcrawler", "Skullcrawler", "Monsters",
                    "Subterranean serpentine predator of Skull Island", R.drawable.ic_avatar_kaiju_skullcrawler),
            // --- Category: Abstract (10 items) ---
            new AvatarCharacter("abstract_amethyst", "Amethyst Crystal", "Abstract",
                    "Deep purple crystalline geometric fracture", R.drawable.ic_avatar_abstract_amethyst),
            new AvatarCharacter("abstract_chromesphere", "Chrome Sphere", "Abstract",
                    "Mirror liquid metal reflective sphere", R.drawable.ic_avatar_abstract_chromesphere),
            new AvatarCharacter("abstract_cyberpunk", "Cyber Matrix", "Abstract",
                    "Neon teal and magenta cybernetic grid", R.drawable.ic_avatar_abstract_cyberpunk),
            new AvatarCharacter("abstract_goldblack", "Gold & Obsidian", "Abstract",
                    "Flowing liquid gold on dark stone veins", R.drawable.ic_avatar_abstract_goldblack),
            new AvatarCharacter("abstract_iridescent", "Iridescent Flora", "Abstract",
                    "Shimmering pearlescent holographic petals", R.drawable.ic_avatar_abstract_iridescent),
            new AvatarCharacter("abstract_kintsugi", "Kintsugi Gold", "Abstract",
                    "Japanese pottery mended with liquid gold seams", R.drawable.ic_avatar_abstract_kintsugi),
            new AvatarCharacter("abstract_prism", "Prism Spectrum", "Abstract",
                    "Refracted glass dispersing white light rainbows", R.drawable.ic_avatar_abstract_prism),
            new AvatarCharacter("abstract_quantum", "Quantum Wave", "Abstract",
                    "Fluorescent particles in oscillating resonance", R.drawable.ic_avatar_abstract_quantum),
            new AvatarCharacter("abstract_silk", "Liquid Silk", "Abstract",
                    "Undulating midnight-blue and cyan fabric ripples", R.drawable.ic_avatar_abstract_silk),
            new AvatarCharacter("abstract_synthwave", "Synthwave Grid", "Abstract",
                    "Retro sunset wireframe horizon in neon violet", R.drawable.ic_avatar_abstract_synthwave));

    // Backward compatibility alias
    public static final List<AvatarCharacter> AVATAR_ICONS = AVATAR_CHARACTERS;

    public static final String KEY_PROFILE_ICON_PREFIX = "profile_icon_";

    public static String getProfileIcon(Context context, String profile) {
        if (context == null || profile == null)
            return "animal_lion";
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = sp.getString(KEY_PROFILE_ICON_PREFIX + profile.toLowerCase().trim(), null);
        if (saved != null && !saved.trim().isEmpty() && !saved.trim().startsWith("photo_")) {
            for (AvatarCharacter c : AVATAR_CHARACTERS) {
                if (c.id.equalsIgnoreCase(saved.trim()))
                    return c.id;
            }
        }
        String lower = profile.toLowerCase().trim();
        if (lower.contains("ben"))
            return "animal_lion";
        if (lower.contains("jess"))
            return "land_aurora";
        int idx = Math.abs(profile.hashCode()) % AVATAR_CHARACTERS.size();
        return AVATAR_CHARACTERS.get(idx).id;
    }

    public static void setProfileIcon(Context context, String profile, String iconId) {
        if (context == null || profile == null || iconId == null)
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PROFILE_ICON_PREFIX + profile.toLowerCase().trim(), iconId.trim()).apply();
        ProfileSyncManager.onLocalProfileChanged(context);
    }

    public static int getIconDrawableRes(String iconId) {
        if (iconId != null) {
            for (AvatarCharacter c : AVATAR_CHARACTERS) {
                if (c.id.equalsIgnoreCase(iconId))
                    return c.drawableRes;
            }
        }
        return 0;
    }

    private static final android.util.LruCache<String, android.graphics.drawable.Drawable> sAvatarCache = new android.util.LruCache<>(
            512);
    private static final java.util.concurrent.ExecutorService sAvatarDecodePool = java.util.concurrent.Executors
            .newFixedThreadPool(4);
    private static final android.os.Handler sMainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private static volatile boolean sPrewarmStarted = false;

    private static String getAvatarCacheKey(String profileName, String iconId, String themeId, int sizePx, int resId) {
        if (resId != 0 && iconId != null) {
            return "char_" + iconId.toLowerCase(java.util.Locale.ROOT) + "_" + sizePx;
        }
        return "mono_" + (profileName != null ? profileName : "") + "_" + sizePx + "_"
                + (themeId != null ? themeId : "");
    }

    public static android.graphics.drawable.Drawable getCachedAvatarDrawable(String iconId, int sizePx) {
        if (iconId == null)
            return null;
        return sAvatarCache.get("char_" + iconId.toLowerCase(java.util.Locale.ROOT) + "_" + sizePx);
    }

    public static android.graphics.drawable.Drawable createPlaceholderAvatarDrawable(Context context, int sizePx) {
        String key = "placeholder_" + sizePx;
        android.graphics.drawable.Drawable cached = sAvatarCache.get(key);
        if (cached != null) {
            return cached;
        }
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        gd.setColor(0xFF1A1E2E);
        gd.setStroke(Math.max(1, (int) (sizePx * 0.04f)), 0x35FFFFFF);
        gd.setSize(sizePx, sizePx);
        sAvatarCache.put(key, gd);
        return gd;
    }

    public static void prewarmAvatarsAsync(Context context, final int tileCharPx) {
        if (context == null || sPrewarmStarted)
            return;
        sPrewarmStarted = true;
        final Context appCtx = context.getApplicationContext();
        for (final AvatarCharacter character : AVATAR_CHARACTERS) {
            sAvatarDecodePool.execute(() -> {
                try {
                    if (getCachedAvatarDrawable(character.id, tileCharPx) == null) {
                        createProfileAvatarDrawable(appCtx, "Ben", character.id, "blue", tileCharPx, null);
                    }
                } catch (Throwable ignored) {
                }
            });
        }
    }

    public static void loadAvatarIntoViewAsync(final Context context, final String profileName,
            final String iconId, final String themeId, final int sizePx, final android.graphics.Typeface typeface,
            final android.widget.ImageView targetView) {
        if (targetView == null)
            return;
        android.graphics.drawable.Drawable cached = getCachedAvatarDrawable(iconId, sizePx);
        if (cached != null) {
            targetView.setImageDrawable(cached);
            return;
        }
        targetView.setImageDrawable(createPlaceholderAvatarDrawable(context, sizePx));
        targetView.setTag(iconId);
        final Context appCtx = context.getApplicationContext();
        sAvatarDecodePool.execute(() -> {
            final android.graphics.drawable.Drawable d = createProfileAvatarDrawable(
                    appCtx, profileName, iconId, themeId, sizePx, typeface);
            if (d != null) {
                sMainHandler.post(() -> {
                    if (iconId.equals(targetView.getTag())) {
                        targetView.setImageDrawable(d);
                    }
                });
            }
        });
    }

    private static android.graphics.Bitmap decodeSampledResourceBitmap(android.content.res.Resources res, int resId,
            int reqSizePx) {
        android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        android.graphics.BitmapFactory.decodeResource(res, resId, opts);
        int inSampleSize = 1;
        int minDim = Math.min(opts.outWidth, opts.outHeight);
        if (minDim > reqSizePx && reqSizePx > 0) {
            int halfDim = minDim / 2;
            while ((halfDim / inSampleSize) >= reqSizePx) {
                inSampleSize *= 2;
            }
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = inSampleSize;
        opts.inScaled = false;
        return android.graphics.BitmapFactory.decodeResource(res, resId, opts);
    }

    public static android.graphics.drawable.Drawable createProfileAvatarDrawable(Context context, String profileName,
            String iconId, String themeId, int sizePx, android.graphics.Typeface typeface) {
        int resId = getIconDrawableRes(iconId);
        String cacheKey = getAvatarCacheKey(profileName, iconId, themeId, sizePx, resId);
        android.graphics.drawable.Drawable cached = sAvatarCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx,
                android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        float radius = sizePx / 2f;

        if (resId != 0 && context != null) {
            try {
                android.graphics.Bitmap srcBmp = decodeSampledResourceBitmap(context.getResources(), resId, sizePx);
                if (srcBmp != null) {
                    android.graphics.BitmapShader shader = new android.graphics.BitmapShader(
                            srcBmp, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP);
                    android.graphics.Matrix matrix = new android.graphics.Matrix();
                    float scale = (float) sizePx / (float) Math.min(srcBmp.getWidth(), srcBmp.getHeight());
                    matrix.setScale(scale, scale);
                    float dx = (sizePx - srcBmp.getWidth() * scale) / 2f;
                    float dy = (sizePx - srcBmp.getHeight() * scale) / 2f;
                    matrix.postTranslate(dx, dy);
                    shader.setLocalMatrix(matrix);

                    android.graphics.Paint p = new android.graphics.Paint(
                            android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG);
                    p.setShader(shader);
                    canvas.drawCircle(radius, radius, radius, p);

                    // Premium subtle luminous rim
                    android.graphics.Paint borderPaint = new android.graphics.Paint(
                            android.graphics.Paint.ANTI_ALIAS_FLAG);
                    borderPaint.setStyle(android.graphics.Paint.Style.STROKE);
                    borderPaint.setStrokeWidth(Math.max(1.5f, sizePx * 0.05f));
                    borderPaint.setColor(0x4DFFFFFF);
                    canvas.drawCircle(radius, radius, radius - borderPaint.getStrokeWidth() / 2f, borderPaint);

                    android.graphics.drawable.BitmapDrawable resDrawable = new android.graphics.drawable.BitmapDrawable(
                            context.getResources(), bitmap);
                    sAvatarCache.put(cacheKey, resDrawable);
                    return resDrawable;
                }
            } catch (Throwable t) {
                Log.w(TAG, "Failed to load character bitmap: " + t.getMessage());
            }
        }

        // Monogram / Fallback with vibrant gradient
        AvatarTheme theme = getAvatarTheme(themeId, profileName);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        android.graphics.Shader shader = new android.graphics.LinearGradient(
                0, 0, sizePx, sizePx,
                theme.primaryColor, theme.gradientEnd,
                android.graphics.Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawCircle(radius, radius, radius, paint);

        android.graphics.Paint borderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(android.graphics.Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(1.2f, sizePx * 0.07f));
        borderPaint.setColor(0x35FFFFFF);
        canvas.drawCircle(radius, radius, radius - borderPaint.getStrokeWidth() / 2f, borderPaint);

        String initial = "U";
        if (profileName != null && !profileName.trim().isEmpty()) {
            initial = profileName.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        }
        android.graphics.Paint textPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(android.graphics.Color.WHITE);
        textPaint.setTextSize(sizePx * 0.44f);
        textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        if (typeface != null)
            textPaint.setTypeface(typeface);
        android.graphics.Paint.FontMetrics fm = textPaint.getFontMetrics();
        float baseline = (sizePx - (fm.descent + fm.ascent)) / 2f;
        canvas.drawText(initial, radius, baseline, textPaint);

        android.graphics.drawable.BitmapDrawable resDrawable = new android.graphics.drawable.BitmapDrawable(
                context.getResources(), bitmap);
        sAvatarCache.put(cacheKey, resDrawable);
        return resDrawable;
    }

    public static android.graphics.drawable.Drawable createMonogramDrawable(Context context, String profileName,
            String themeId, int sizePx, android.graphics.Typeface typeface) {
        String iconId = getProfileIcon(context, profileName);
        return createProfileAvatarDrawable(context, profileName, iconId, themeId, sizePx, typeface);
    }

    private static String getProfileStorageKey(Context context) {
        String profile = getCurrentProfile(context).toLowerCase().trim();
        return KEY_CUSTOM_ITEMS_PREFIX + profile;
    }

    public static List<WatchNextItem> loadWatchNext(Context context, Map<String, AppItem> appMap) {
        return loadWatchNext(context, appMap, "ALL", null);
    }

    public static List<WatchNextItem> loadWatchNext(Context context, Map<String, AppItem> appMap, String filterKey,
            List<String> dockOrder) {
        List<WatchNextItem> all = loadAllWatchNext(context, appMap, dockOrder);
        if (filterKey == null || "ALL".equalsIgnoreCase(filterKey)) {
            return all;
        }
        List<WatchNextItem> filtered = new ArrayList<>();
        for (WatchNextItem item : all) {
            if (filterKey.equalsIgnoreCase(item.packageName)
                    || filterKey.equalsIgnoreCase(item.appBadge)
                    || (filterKey.toLowerCase().contains("max")
                            && (item.packageName.contains("hbo") || item.packageName.contains("wbd")))
                    || (filterKey.toLowerCase().contains("disney") && item.packageName.contains("disney"))
                    || (filterKey.toLowerCase().contains("apple") && item.packageName.contains("apple"))
                    || (filterKey.toLowerCase().contains("prime") && (item.packageName.contains("firebat")
                            || item.packageName.contains("amazonvideo") || item.packageName.contains("avod")))) {
                filtered.add(item);
            }
        }
        return filtered.isEmpty() ? all : filtered;
    }

    public static boolean isWatchlistApp(String pkg) {
        if (pkg == null)
            return false;
        String p = pkg.toLowerCase();
        if (isYouTube(pkg, null, null))
            return false;
        return p.contains("disney")
                || p.contains("hbo") || p.contains("wbd")
                || p.contains("apple")
                || p.contains("amazonvideo") || p.contains("prime") || p.contains("firebat") || p.contains("avod")
                || p.contains("peacock")
                || p.contains("hulu")
                || p.contains("netflix")
                || p.contains("paramount") || p.contains("cbs");
    }

    public static List<WatchNextItem> loadAllWatchNext(Context context, Map<String, AppItem> appMap) {
        return loadAllWatchNext(context, appMap, null);
    }

    public static List<WatchNextItem> loadAllWatchNext(Context context, Map<String, AppItem> appMap,
            List<String> dockOrder) {
        List<WatchNextItem> rawList = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> dismissed = sp.getStringSet(KEY_DISMISSED, new HashSet<>());

        // 1. Load active profile custom / discovered continue watching shows
        List<WatchNextItem> customItems = loadCustomItems(context, appMap);
        for (WatchNextItem ci : customItems) {
            if (!dismissed.contains(ci.id) && isPlausibleShowTitle(ci.showTitle) && isWatchlistApp(ci.packageName)
                    && !isYouTube(ci.packageName, ci.appBadge, ci.showTitle)) {
                rawList.add(ci);
            }
        }

        // 2. Native Android TV EPG ContentProvider (Filtered: NO YouTube, with Real
        // Show Artwork)
        try {
            Uri epgUri = Uri.parse("content://android.media.tv/watch_next_program");
            Cursor cursor = context.getContentResolver().query(epgUri, null, null, null,
                    "last_engagement_time_utc_millis DESC");
            if (cursor != null) {
                while (cursor.moveToNext() && rawList.size() < 24) {
                    try {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                        String idStr = "epg_" + id;
                        if (dismissed.contains(idStr))
                            continue;

                        String pkg = cursor.getString(cursor.getColumnIndexOrThrow("package_name"));
                        if (pkg == null || !isWatchlistApp(pkg))
                            continue;

                        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        if (title == null || !isPlausibleShowTitle(title) || isYouTube(null, null, title))
                            continue;

                        boolean dup = false;
                        for (WatchNextItem existing : rawList) {
                            if (existing.showTitle.equalsIgnoreCase(title.trim())) {
                                dup = true;
                                break;
                            }
                        }
                        if (dup)
                            continue;

                        String epTitle = null;
                        int epIdx = cursor.getColumnIndex("episode_title");
                        if (epIdx >= 0)
                            epTitle = cursor.getString(epIdx);

                        long pos = 0;
                        int posIdx = cursor.getColumnIndex("last_playback_position_millis");
                        if (posIdx >= 0)
                            pos = cursor.getLong(posIdx);

                        long dur = 1;
                        int durIdx = cursor.getColumnIndex("duration_millis");
                        if (durIdx >= 0)
                            dur = Math.max(1, cursor.getLong(durIdx));

                        int pct = (int) Math.min(100, Math.max(0, (pos * 100) / dur));
                        String intentUri = null;
                        int intentIdx = cursor.getColumnIndex("intent_uri");
                        if (intentIdx >= 0)
                            intentUri = cursor.getString(intentIdx);

                        Bitmap posterBmp = null;
                        String posterUriStr = null;
                        int posterIdx = cursor.getColumnIndex("poster_art_uri");
                        if (posterIdx < 0)
                            posterIdx = cursor.getColumnIndex("thumbnail_uri");
                        if (posterIdx >= 0)
                            posterUriStr = cursor.getString(posterIdx);

                        if (posterUriStr != null) {
                            try {
                                Uri u = Uri.parse(posterUriStr);
                                InputStream is = context.getContentResolver().openInputStream(u);
                                if (is != null) {
                                    posterBmp = BitmapFactory.decodeStream(is);
                                    is.close();
                                }
                            } catch (Throwable ignored) {
                            }
                        }

                        AppItem app = (appMap != null) ? appMap.get(pkg) : null;
                        String badge = app != null ? app.label : resolveBadgeFromPackage(pkg);
                        int brandColor = app != null ? app.themeColor : resolveBrandColor(pkg);
                        int drawableId = resolveThumbnailForShow(title, badge);

                        WatchNextItem item = new WatchNextItem(
                                idStr,
                                pkg,
                                badge,
                                title,
                                epTitle != null ? epTitle : "Resume Playback",
                                false,
                                pct > 0 ? pct : 45,
                                brandColor,
                                intentUri,
                                null,
                                drawableId,
                                posterBmp);
                        rawList.add(item);
                    } catch (Throwable inner) {
                        Log.w(TAG, "Error parsing EPG row: " + inner.getMessage());
                    }
                }
                cursor.close();
            }
        } catch (Throwable t) {
            Log.d(TAG, "Native EPG query: " + t.getMessage());
        }

        // 3. Saved Notification Recommendations (Fire TV & Google TV live feeds)
        try {
            List<WatchNextItem> notifItems = WatchNextNotificationListener.loadNotificationRecommendations(context);
            for (WatchNextItem ni : notifItems) {
                if (!dismissed.contains(ni.id) && isPlausibleShowTitle(ni.showTitle) && isWatchlistApp(ni.packageName)
                        && !isYouTube(ni.packageName, ni.appBadge, ni.showTitle)) {
                    boolean dup = false;
                    for (WatchNextItem existing : rawList) {
                        if (existing.showTitle.equalsIgnoreCase(ni.showTitle)) {
                            dup = true;
                            break;
                        }
                    }
                    if (!dup) {
                        rawList.add(ni);
                    }
                }
            }
        } catch (Throwable t) {
            Log.d(TAG, "Notification recommendations query: " + t.getMessage());
        }

        // 4. Organize by App and Sort strictly matching the Dock order from left to
        // right
        if (dockOrder != null && !dockOrder.isEmpty()) {
            Map<String, List<WatchNextItem>> appGroups = new LinkedHashMap<>();
            for (WatchNextItem item : rawList) {
                String key = getAppKey(item.packageName);
                if (!appGroups.containsKey(key)) {
                    appGroups.put(key, new ArrayList<>());
                }
                appGroups.get(key).add(item);
            }

            List<WatchNextItem> sortedList = new ArrayList<>();
            // Pull apps in the exact dock order from left to right
            for (String dockPkg : dockOrder) {
                if (dockPkg == null || isYouTube(dockPkg, null, null))
                    continue;
                String dockKey = getAppKey(dockPkg);
                List<WatchNextItem> group = appGroups.remove(dockKey);
                if (group != null && !group.isEmpty()) {
                    sortedList.addAll(group);
                }
            }
            // Add any remaining items from installed apps not in dock
            for (List<WatchNextItem> remaining : appGroups.values()) {
                sortedList.addAll(remaining);
            }
            return sortedList;
        }

        return rawList;
    }

    public static boolean isPlausibleShowTitle(String text) {
        if (text == null)
            return false;
        String t = text.trim();
        if (t.length() < 2 || t.length() > 65)
            return false;
        String lower = t.toLowerCase();
        if (lower.contains("remove from") || lower.contains("add to") || lower.contains("continue watching")
                || lower.contains("watch next") || lower.contains("resume") || lower.contains("dismiss")
                || lower.contains("option") || lower.contains("manage") || lower.contains("clear")
                || lower.contains("delete") || lower.contains("details") || lower.contains("episodes")
                || lower.contains("more info") || lower.contains("see all") || lower.contains("switch")
                || lower.contains("restart") || lower.contains("suggested") || lower.contains("extras")
                || lower.contains("audio description") || lower.contains("trailer") || lower.contains("search by")
                || lower.contains("sign in") || lower.contains("sign out") || lower.contains("loading")) {
            return false;
        }
        return true;
    }

    public static void saveDynamicWatchlistShow(Context context, String pkg, String title, String ep) {
        if (context == null || title == null || !isPlausibleShowTitle(title))
            return;
        if (isYouTube(pkg, null, title))
            return;

        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String storageKey = getProfileStorageKey(context);
            String raw = sp.getString(storageKey, "[]");
            JSONArray arr = new JSONArray(raw);

            JSONArray newArr = new JSONArray();
            JSONObject newObj = new JSONObject();
            newObj.put("id", "dynamic_" + (pkg != null ? pkg.hashCode() : 0) + "_" + title.trim().hashCode());
            newObj.put("packageName", pkg != null ? pkg : "");
            newObj.put("appBadge", resolveBadgeFromPackage(pkg));
            newObj.put("showTitle", title.trim());
            newObj.put("episodeInfo", ep != null && !ep.isEmpty() ? ep.trim() : "Continue Watching");
            newObj.put("progressPercent", 60);
            newObj.put("brandColor", resolveBrandColor(pkg));
            newObj.put("intentUri", resolveDefaultIntentUri(pkg, title, ep));
            newObj.put("timestamp", System.currentTimeMillis());

            newArr.put(newObj);

            for (int i = 0; i < arr.length() && newArr.length() < 20; i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!title.trim().equalsIgnoreCase(obj.optString("showTitle"))) {
                    newArr.put(obj);
                }
            }

            sp.edit().putString(storageKey, newArr.toString()).apply();
            Log.i(TAG, "Saved dynamic watchlist show: " + title + " (" + pkg + ")");
        } catch (Throwable t) {
            Log.e(TAG, "saveDynamicWatchlistShow error", t);
        }
    }

    public static String getAppKey(String pkg) {
        if (pkg == null)
            return "";
        String p = pkg.toLowerCase();
        if (p.contains("disney"))
            return "disney";
        if (p.contains("hbo") || p.contains("wbd"))
            return "max";
        if (p.contains("apple"))
            return "apple";
        if (p.contains("prime") || p.contains("amazonvideo") || p.contains("firebat") || p.contains("avod"))
            return "prime";
        if (p.contains("peacock"))
            return "peacock";
        if (p.contains("hulu"))
            return "hulu";
        if (p.contains("netflix"))
            return "netflix";
        if (p.contains("cbs") || p.contains("paramount"))
            return "paramount";
        return p;
    }

    public static boolean isYouTube(String pkg, String badge, String title) {
        if (pkg != null) {
            String p = pkg.toLowerCase();
            if (p.contains("youtube") || p.contains("yt_") || p.contains("smarttube"))
                return true;
        }
        if (badge != null) {
            String b = badge.toLowerCase();
            if (b.contains("youtube") || b.contains("yt"))
                return true;
        }
        if (title != null) {
            String t = title.toLowerCase();
            if (t.contains("youtube"))
                return true;
        }
        return false;
    }

    public static List<AppFilter> getAppFilters(Context context, List<WatchNextItem> allItems, List<String> dockOrder) {
        List<AppFilter> filters = new ArrayList<>();
        if (allItems == null || allItems.isEmpty())
            return filters;

        filters.add(new AppFilter("ALL", "All", allItems.size(), Color.parseColor("#00E5FF")));

        Map<String, List<WatchNextItem>> groups = new LinkedHashMap<>();
        for (WatchNextItem item : allItems) {
            String badge = item.appBadge != null ? item.appBadge : "Other";
            if (!groups.containsKey(badge)) {
                groups.put(badge, new ArrayList<>());
            }
            groups.get(badge).add(item);
        }

        // Order filters by dock apps
        if (dockOrder != null) {
            for (String dockPkg : dockOrder) {
                if (dockPkg == null || isYouTube(dockPkg, null, null))
                    continue;
                String badge = resolveBadgeFromPackage(dockPkg);
                List<WatchNextItem> items = groups.remove(badge);
                if (items != null && !items.isEmpty()) {
                    int color = items.get(0).brandColor;
                    filters.add(new AppFilter(badge, badge, items.size(), color));
                }
            }
        }

        // Remaining
        for (Map.Entry<String, List<WatchNextItem>> entry : groups.entrySet()) {
            String badge = entry.getKey();
            List<WatchNextItem> items = entry.getValue();
            int color = items.get(0).brandColor;
            filters.add(new AppFilter(badge, badge, items.size(), color));
        }

        return filters;
    }

    public static void saveCustomShow(Context context, String pkg, String badge, String title, String ep, int pct,
            int brandColor) {
        if (context == null || title == null || !isPlausibleShowTitle(title))
            return;
        if (isYouTube(pkg, badge, title))
            return;

        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String storageKey = getProfileStorageKey(context);
            String raw = sp.getString(storageKey, "[]");
            JSONArray arr = new JSONArray(raw);

            JSONObject obj = new JSONObject();
            obj.put("id", "custom_" + System.currentTimeMillis());
            obj.put("packageName", pkg != null ? pkg : "");
            obj.put("appBadge", badge != null ? badge : resolveBadgeFromPackage(pkg));
            obj.put("showTitle", title.trim());
            obj.put("episodeInfo", ep != null ? ep.trim() : "Resume Watching");
            obj.put("progressPercent", pct);
            obj.put("brandColor", brandColor != 0 ? brandColor : resolveBrandColor(pkg));
            obj.put("intentUri", resolveDefaultIntentUri(pkg, title, ep));

            arr.put(0, obj);
            sp.edit().putString(storageKey, arr.toString()).apply();
        } catch (Throwable t) {
            Log.e(TAG, "saveCustomShow error", t);
        }
    }

    public static void editShow(Context context, String id, String newTitle, String newEp, int newPct) {
        if (context == null || id == null)
            return;
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String storageKey = getProfileStorageKey(context);
            String raw = sp.getString(storageKey, "[]");
            JSONArray arr = new JSONArray(raw);
            boolean updated = false;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (id.equals(obj.optString("id"))) {
                    obj.put("showTitle", newTitle);
                    obj.put("episodeInfo", newEp);
                    obj.put("progressPercent", newPct);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("packageName", resolvePackageFromId(id));
                obj.put("appBadge", resolveBadgeFromId(id));
                obj.put("showTitle", newTitle);
                obj.put("episodeInfo", newEp);
                obj.put("progressPercent", newPct);
                obj.put("brandColor", resolveBrandColorFromId(id));
                arr.put(0, obj);
            }

            sp.edit().putString(storageKey, arr.toString()).apply();
        } catch (Throwable t) {
            Log.e(TAG, "editShow error", t);
        }
    }

    private static List<WatchNextItem> loadCustomItems(Context context, Map<String, AppItem> appMap) {
        List<WatchNextItem> list = new ArrayList<>();
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String storageKey = getProfileStorageKey(context);
            String raw = sp.getString(storageKey, null);

            // If active profile has no saved data yet, initialize with authentic shows for Ben
            if (raw == null) {
                String profile = getCurrentProfile(context);
                if ("Ben".equalsIgnoreCase(profile) || "Benjamin".equalsIgnoreCase(profile)) {
                    JSONArray seed = buildBenAuthenticShowsSeed(appMap);
                    raw = seed.toString();
                    sp.edit().putString(storageKey, raw).apply();
                } else {
                    raw = "[]";
                    sp.edit().putString(storageKey, raw).apply();
                }
            }

            JSONArray arr = new JSONArray(raw);
            JSONArray cleanedArr = new JSONArray();
            boolean hadCorrupted = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.optString("id");
                String pkg = obj.optString("packageName");
                String badge = obj.optString("appBadge");
                String title = obj.optString("showTitle");

                if (!isPlausibleShowTitle(title) || (id != null && id.contains("RFCW")) || (title != null && title.toLowerCase().contains("remove from"))) {
                    hadCorrupted = true;
                    continue;
                }
                if (isYouTube(pkg, badge, title)) {
                    hadCorrupted = true;
                    continue;
                }

                String ep = obj.optString("episodeInfo");
                int pct = obj.optInt("progressPercent", 50);
                int color = obj.optInt("brandColor", resolveBrandColor(pkg));
                int drawableId = resolveThumbnailForShow(title, badge);
                String intentUri = obj.optString("intentUri", null);
                String canonicalUri = resolveDefaultIntentUri(pkg, title, ep);
                if (canonicalUri != null && (intentUri == null || !intentUri.equals(canonicalUri))) {
                    intentUri = canonicalUri;
                    obj.put("intentUri", intentUri);
                    hadCorrupted = true;
                } else if (intentUri == null || intentUri.trim().isEmpty() || intentUri.contains("/search") || intentUri.contains("search?")) {
                    intentUri = canonicalUri;
                    obj.put("intentUri", intentUri);
                    hadCorrupted = true;
                }
                cleanedArr.put(obj);

                list.add(new WatchNextItem(
                        id,
                        pkg,
                        badge,
                        title,
                        ep,
                        false,
                        pct,
                        color,
                        intentUri,
                        null,
                        drawableId,
                        null
                ));
            }
            if (hadCorrupted) {
                sp.edit().putString(storageKey, cleanedArr.toString()).apply();
            }
        } catch (Throwable t) {
            Log.e(TAG, "loadCustomItems error", t);
        }
        return list;
    }

    private static JSONArray buildBenAuthenticShowsSeed(Map<String, AppItem> appMap) {
        JSONArray arr = new JSONArray();
        try {
            String disneyPkg = findInstalled(appMap, "com.disney.disneyplus");
            if (disneyPkg == null)
                disneyPkg = "com.disney.disneyplus";

            // 1. VisionQuest (Disney+ Ben Profile)
            JSONObject s1 = new JSONObject();
            s1.put("id", "disney_ben_visionquest");
            s1.put("packageName", disneyPkg);
            s1.put("appBadge", "DISNEY+");
            s1.put("showTitle", "VisionQuest");
            s1.put("episodeInfo", "S1:E4 Malware");
            s1.put("progressPercent", 45);
            s1.put("brandColor", Color.parseColor("#1B45F5"));
            s1.put("intentUri", "https://www.disneyplus.com/browse/entity-8744ec55-8d14-4631-99b4-c889a2d6013f");
            arr.put(s1);

            // 2. Bleach: Thousand-Year Blood War (Disney+ Ben Profile)
            JSONObject s2 = new JSONObject();
            s2.put("id", "disney_ben_bleach");
            s2.put("packageName", disneyPkg);
            s2.put("appBadge", "DISNEY+");
            s2.put("showTitle", "Bleach: Thousand-Year Blood War");
            s2.put("episodeInfo", "S4:E9 (Sub) THE BLADE");
            s2.put("progressPercent", 80);
            s2.put("brandColor", Color.parseColor("#1B45F5"));
            s2.put("intentUri", "https://www.disneyplus.com/browse/entity-02a3c8c0-4f1d-4610-bbb4-5b8e9468d7b1");
            arr.put(s2);

            // 3. Rick and Morty (Disney+ Ben Profile)
            JSONObject s3 = new JSONObject();
            s3.put("id", "disney_ben_rickmorty");
            s3.put("packageName", disneyPkg);
            s3.put("appBadge", "DISNEY+");
            s3.put("showTitle", "Rick and Morty");
            s3.put("episodeInfo", "S7:E3 Air Force Wong");
            s3.put("progressPercent", 60);
            s3.put("brandColor", Color.parseColor("#1B45F5"));
            s3.put("intentUri", "https://www.disneyplus.com/browse/entity-4e0f6374-fc81-4da2-b7a9-f7f8c29e7acc");
            arr.put(s3);

            // 4. Abbott Elementary (Disney+ Ben Profile)
            JSONObject s4 = new JSONObject();
            s4.put("id", "disney_ben_abbott");
            s4.put("packageName", disneyPkg);
            s4.put("appBadge", "DISNEY+");
            s4.put("showTitle", "Abbott Elementary");
            s4.put("episodeInfo", "S5:E1 Team Building");
            s4.put("progressPercent", 30);
            s4.put("brandColor", Color.parseColor("#1B45F5"));
            s4.put("intentUri", "https://www.disneyplus.com/browse/entity-7c33eeb2-5d16-4a10-ad9e-ee31f9fff15c");
            arr.put(s4);

            String maxPkg = findInstalled(appMap, "com.hbo.hbonow", "com.wbd.stream");
            if (maxPkg == null)
                maxPkg = "com.hbo.hbonow";

            // 5. Georgie & Mandy's First Marriage (Max Benjamin Profile)
            JSONObject s5 = new JSONObject();
            s5.put("id", "max_ben_georgie");
            s5.put("packageName", maxPkg);
            s5.put("appBadge", "MAX");
            s5.put("showTitle", "Georgie & Mandy's First Marriage");
            s5.put("episodeInfo", "S1 E13 McAllister Auto Loves the Ladies");
            s5.put("progressPercent", 90);
            s5.put("brandColor", Color.parseColor("#8028F5"));
            s5.put("intentUri", "https://play.max.com/show/2c59e4b1-b024-4725-bf07-398f6225b653");
            arr.put(s5);

            // 6. Peacemaker (Max Benjamin Profile)
            JSONObject s6 = new JSONObject();
            s6.put("id", "max_ben_peacemaker");
            s6.put("packageName", maxPkg);
            s6.put("appBadge", "MAX");
            s6.put("showTitle", "Peacemaker");
            s6.put("episodeInfo", "S2 E8 Full Nelson");
            s6.put("progressPercent", 10);
            s6.put("brandColor", Color.parseColor("#8028F5"));
            s6.put("intentUri", "https://play.max.com/show/a939d96b-7ffb-4481-96f6-472838d104ca");
            arr.put(s6);

            // 7. Stuart Fails to Save the Universe (Max Benjamin Profile)
            JSONObject s7 = new JSONObject();
            s7.put("id", "max_ben_stuart");
            s7.put("packageName", maxPkg);
            s7.put("appBadge", "MAX");
            s7.put("showTitle", "Stuart Fails to Save the Universe");
            s7.put("episodeInfo", "S1 E10 Spoiler: Filmed Before a Live Studio Audience");
            s7.put("progressPercent", 35);
            s7.put("brandColor", Color.parseColor("#8028F5"));
            s7.put("intentUri", "https://play.max.com/show/1130c5ba-e71f-43e0-8de3-487dfdb49c11");
            arr.put(s7);

        } catch (Throwable t) {
            Log.e(TAG, "buildBenAuthenticShowsSeed error", t);
        }
        return arr;
    }

    public static String toShowSlug(String title) {
        if (title == null)
            return "";
        String s = title.trim().toLowerCase();
        s = s.replace("&", "and");
        s = s.replaceAll("['\":!?.,()\\[\\]]", "");
        s = s.replaceAll("\\s+", "-");
        s = s.replaceAll("-+", "-");
        return s;
    }

    public static String resolveCanonicalUuidUri(String pkg, String title) {
        if (title == null || title.trim().isEmpty())
            return null;
        String t = title.trim().toLowerCase();
        String p = pkg != null ? pkg.toLowerCase() : "";

        if (p.contains("disney")) {
            if (t.contains("visionquest") || t.contains("vision quest")) {
                return "https://www.disneyplus.com/browse/entity-8744ec55-8d14-4631-99b4-c889a2d6013f";
            }
            if (t.contains("bleach")) {
                return "https://www.disneyplus.com/browse/entity-02a3c8c0-4f1d-4610-bbb4-5b8e9468d7b1";
            }
            if (t.contains("rick") && t.contains("morty")) {
                return "https://www.disneyplus.com/browse/entity-4e0f6374-fc81-4da2-b7a9-f7f8c29e7acc";
            }
            if (t.contains("abbott")) {
                return "https://www.disneyplus.com/browse/entity-7c33eeb2-5d16-4a10-ad9e-ee31f9fff15c";
            }
        }
        if (p.contains("hbo") || p.contains("wbd")) {
            if (t.contains("peacemaker")) {
                return "https://play.max.com/show/a939d96b-7ffb-4481-96f6-472838d104ca";
            }
            if (t.contains("georgie") || t.contains("mandy")) {
                return "https://play.max.com/show/2c59e4b1-b024-4725-bf07-398f6225b653";
            }
            if (t.contains("stuart")) {
                return "https://play.max.com/show/1130c5ba-e71f-43e0-8de3-487dfdb49c11";
            }
            if (t.contains("rick") && t.contains("morty")) {
                return "https://play.max.com/show/ab553cdc-e15d-4597-b65f-bec9201fd2dd";
            }
        }
        return null;
    }

    public static String resolveDefaultIntentUri(String pkg, String title, String ep) {
        if (title == null || title.trim().isEmpty())
            return null;
        String canonical = resolveCanonicalUuidUri(pkg, title);
        if (canonical != null)
            return canonical;

        String slug = toShowSlug(title);
        String p = pkg != null ? pkg.toLowerCase() : "";

        // Disney+
        if (p.contains("disney")) {
            return "https://www.disneyplus.com/series/" + slug;
        }
        // Max / HBO
        if (p.contains("hbo") || p.contains("wbd")) {
            return "https://play.max.com/show/" + slug;
        }
        // Peacock
        if (p.contains("peacock")) {
            return "https://www.peacocktv.com/watch/asset/tv/" + slug;
        }
        // Hulu
        if (p.contains("hulu")) {
            return "https://www.hulu.com/series/" + slug;
        }
        // Apple TV
        if (p.contains("apple")) {
            return "https://tv.apple.com/show/" + slug;
        }
        // Prime Video
        if (p.contains("prime") || p.contains("amazonvideo") || p.contains("firebat") || p.contains("avod")) {
            return "https://app.primevideo.com/detail?gti=" + slug;
        }
        // Netflix
        if (p.contains("netflix")) {
            return "https://www.netflix.com/title/" + slug;
        }
        // Paramount+ / CBS
        if (p.contains("paramount") || p.contains("cbs")) {
            return "https://www.paramountplus.com/shows/" + slug + "/";
        }

        return null;
    }

    public static Intent buildPlaybackIntent(Context context, WatchNextItem item) {
        if (context == null || item == null) return null;
        PackageManager pm = context.getPackageManager();

        // 1. Always prefer verified canonical entity UUID deep link if known for this show
        String canonicalUuidLink = resolveCanonicalUuidUri(item.packageName, item.showTitle);
        String targetUri = canonicalUuidLink;

        // 2. Otherwise use explicit item.intentUri if non-empty and not a search fallback
        if ((targetUri == null || targetUri.isEmpty())
                && item.intentUri != null && !item.intentUri.trim().isEmpty()
                && !item.intentUri.contains("/search") && !item.intentUri.contains("search?")) {
            targetUri = item.intentUri.trim();
        }

        // 3. Otherwise fallback to default URI resolver
        if (targetUri == null || targetUri.isEmpty()) {
            targetUri = resolveDefaultIntentUri(item.packageName, item.showTitle, item.episodeInfo);
        }

        if (targetUri != null && !targetUri.isEmpty()) {
            try {
                Intent parsed;
                if (targetUri.startsWith("intent:")) {
                    parsed = Intent.parseUri(targetUri, Intent.URI_INTENT_SCHEME);
                } else {
                    parsed = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUri));
                }
                if (parsed != null) {
                    parsed.addCategory(Intent.CATEGORY_DEFAULT);
                    parsed.addCategory(Intent.CATEGORY_BROWSABLE);
                    if (item.packageName != null && !item.packageName.isEmpty()) {
                        parsed.setPackage(item.packageName);
                    }
                    android.content.pm.ResolveInfo ri = pm.resolveActivity(parsed, 0);
                    if (ri != null && ri.activityInfo != null) {
                        parsed.setComponent(new android.content.ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                        parsed.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        return parsed;
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Failed parsing targetUri: " + targetUri, t);
            }
        }

        // Fallback: Launch the app's main launcher activity
        if (item.packageName != null && !item.packageName.isEmpty()) {
            try {
                Intent launch = pm.getLeanbackLaunchIntentForPackage(item.packageName);
                if (launch == null) launch = pm.getLaunchIntentForPackage(item.packageName);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    return launch;
                }
            } catch (Throwable t) {
                Log.w(TAG, "Failed getting launch intent for: " + item.packageName, t);
            }
        }

        return null;
    }

    public static int resolveThumbnailForShow(String title, String badge) {
        if (title == null)
            return 0;
        String t = title.toLowerCase();
        if (t.contains("visionquest") || t.contains("vision quest"))
            return R.drawable.thumb_visionquest;
        if (t.contains("bleach"))
            return R.drawable.thumb_bleach;
        if (t.contains("rick") && t.contains("morty"))
            return R.drawable.thumb_rickmorty;
        if (t.contains("abbott"))
            return R.drawable.thumb_abbott;
        if (t.contains("georgie") || t.contains("mandy"))
            return R.drawable.thumb_georgie;
        if (t.contains("peacemaker"))
            return R.drawable.thumb_peacemaker;
        if (t.contains("stuart"))
            return R.drawable.thumb_stuart;
        return 0;
    }

    public static void recordAppWatch(Context context, String packageName) {
        if (packageName == null)
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_LAST_WATCHED_PKG, packageName).apply();
    }

    public static void dismissItem(Context context, String id) {
        if (id == null)
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet(KEY_DISMISSED, new HashSet<>()));
        set.add(id);
        sp.edit().putStringSet(KEY_DISMISSED, set).apply();
    }

    public static void resetAllContinueWatching(Context context) {
        if (context == null)
            return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.remove(KEY_DISMISSED);
        ed.remove(KEY_LAST_WATCHED_PKG);
        ed.remove("notif_recommendations_json");
        for (String k : sp.getAll().keySet()) {
            if (k.startsWith(KEY_CUSTOM_ITEMS_PREFIX) || k.equals("custom_shows_json")) {
                ed.remove(k);
            }
        }
        ed.apply();
    }

    private static String findInstalled(Map<String, AppItem> appMap, String... candidates) {
        if (appMap == null)
            return null;
        for (String c : candidates) {
            if (appMap.containsKey(c))
                return c;
        }
        return null;
    }

    public static String resolveBadgeFromPackage(String pkg) {
        if (pkg == null)
            return "STREAM";
        String p = pkg.toLowerCase();
        if (p.contains("hbo") || p.contains("wbd"))
            return "MAX";
        if (p.contains("disney"))
            return "DISNEY+";
        if (p.contains("apple"))
            return "APPLE TV+";
        if (p.contains("peacock"))
            return "PEACOCK";
        if (p.contains("hulu"))
            return "HULU";
        if (p.contains("netflix"))
            return "NETFLIX";
        if (p.contains("prime") || p.contains("amazonvideo") || p.contains("firebat") || p.contains("avod"))
            return "PRIME VIDEO";
        if (p.contains("cbs") || p.contains("paramount"))
            return "PARAMOUNT+";
        return "TV";
    }

    public static int resolveBrandColor(String pkg) {
        if (pkg == null)
            return Color.parseColor("#8028F5");
        String p = pkg.toLowerCase();
        if (p.contains("hbo") || p.contains("wbd"))
            return Color.parseColor("#8028F5");
        if (p.contains("disney"))
            return Color.parseColor("#1B45F5");
        if (p.contains("apple"))
            return Color.parseColor("#00E5FF");
        if (p.contains("peacock"))
            return Color.parseColor("#00B074");
        if (p.contains("hulu"))
            return Color.parseColor("#1CE783");
        if (p.contains("netflix"))
            return Color.parseColor("#E50914");
        if (p.contains("prime") || p.contains("amazonvideo") || p.contains("firebat") || p.contains("avod"))
            return Color.parseColor("#00A8E1");
        if (p.contains("cbs") || p.contains("paramount"))
            return Color.parseColor("#0064FF");
        return Color.parseColor("#6C5CE7");
    }

    private static String resolvePackageFromId(String id) {
        if (id.startsWith("max_"))
            return "com.hbo.hbonow";
        if (id.startsWith("disney_"))
            return "com.disney.disneyplus";
        if (id.startsWith("apple_"))
            return "com.apple.atve.amazon.appletv";
        if (id.startsWith("prime_"))
            return "com.amazon.firebat";
        return "";
    }

    private static String resolveBadgeFromId(String id) {
        if (id.startsWith("max_"))
            return "MAX";
        if (id.startsWith("disney_"))
            return "DISNEY+";
        if (id.startsWith("apple_"))
            return "APPLE TV+";
        if (id.startsWith("prime_"))
            return "PRIME VIDEO";
        return "STREAM";
    }

    private static int resolveBrandColorFromId(String id) {
        if (id.startsWith("max_"))
            return Color.parseColor("#8028F5");
        if (id.startsWith("disney_"))
            return Color.parseColor("#1B45F5");
        if (id.startsWith("apple_"))
            return Color.parseColor("#00E5FF");
        if (id.startsWith("prime_"))
            return Color.parseColor("#00A8E1");
        return Color.parseColor("#6C5CE7");
    }
}
