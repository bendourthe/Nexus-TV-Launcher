import urllib.request
import urllib.parse
import json

TESTS = [
    # Marvel (MCU)
    ("marvelcinematicuniverse", "Iron_Man", "Iron Man"),
    ("marvelcinematicuniverse", "Steve_Rogers", "Captain America"),
    ("marvelcinematicuniverse", "Thor", "Thor"),
    ("marvelcinematicuniverse", "Peter_Parker_(Earth-199999)", "Spider-Man"),
    ("marvelcinematicuniverse", "T%27Challa", "Black Panther"),
    ("marvelcinematicuniverse", "Stephen_Strange", "Doctor Strange"),
    ("marvelcinematicuniverse", "Thanos", "Thanos"),
    ("marvelcinematicuniverse", "Loki", "Loki"),
    ("marvelcinematicuniverse", "Wade_Wilson_(Earth-10005)", "Deadpool"),
    ("marvelcinematicuniverse", "James_Howlett_(Earth-10005)", "Wolverine"),

    # DC Comics (DCEU)
    ("dcextendeduniverse", "Bruce_Wayne_(The_Batman)", "The Batman"),
    ("dcextendeduniverse", "Clark_Kent_(DC_Extended_Universe)", "Superman"),
    ("dcextendeduniverse", "Diana_Prince_(DC_Extended_Universe)", "Wonder Woman"),
    ("dcextendeduniverse", "Joker_(The_Dark_Knight)", "Joker"),
    ("dcextendeduniverse", "Christopher_Smith_(DC_Extended_Universe)", "Peacemaker"),
    ("dcextendeduniverse", "Harleen_Quinzel_(DC_Extended_Universe)", "Harley Quinn"),
    ("dcextendeduniverse", "Arthur_Curry_(DC_Extended_Universe)", "Aquaman"),
    ("dcextendeduniverse", "Barry_Allen_(DC_Extended_Universe)", "The Flash"),
    ("dcextendeduniverse", "Teth-Adam_(DC_Extended_Universe)", "Black Adam"),
    ("dcextendeduniverse", "Bane_(The_Dark_Knight_Rises)", "Bane"),

    # Star Wars
    ("starwars", "Darth_Vader", "Darth Vader"),
    ("starwars", "Din_Djarin", "The Mandalorian"),
    ("starwars", "Din_Grogu", "Grogu"),
    ("starwars", "Ahsoka_Tano", "Ahsoka Tano"),
    ("starwars", "Obi-Wan_Kenobi", "Obi-Wan Kenobi"),
    ("starwars", "Boba_Fett", "Boba Fett"),
    ("starwars", "Maul", "Darth Maul"),
    ("starwars", "Kylo_Ren", "Kylo Ren"),
    ("starwars", "Stormtrooper", "Stormtrooper"),
    ("starwars", "Luke_Skywalker", "Luke Skywalker"),

    # Lord of the Rings
    ("lotr", "Aragorn_II", "Aragorn"),
    ("lotr", "Gandalf", "Gandalf"),
    ("lotr", "Legolas", "Legolas"),
    ("lotr", "Gimli", "Gimli"),
    ("lotr", "Frodo_Baggins", "Frodo"),
    ("lotr", "Gollum", "Gollum"),
    ("lotr", "Sauron", "Sauron"),
    ("lotr", "Galadriel", "Galadriel"),
    ("lotr", "Samwise_Gamgee", "Sam"),
    ("lotr", "Witch-king_of_Angmar", "Witch-king"),

    # Godzilla
    ("godzilla", "Godzilla_(MonsterVerse)", "Godzilla"),
    ("godzilla", "King_Kong_(MonsterVerse)", "Kong"),
    ("godzilla", "King_Ghidorah_(MonsterVerse)", "King Ghidorah"),
    ("godzilla", "Mothra_(MonsterVerse)", "Mothra"),
    ("godzilla", "Rodan_(MonsterVerse)", "Rodan"),
    ("godzilla", "Mechagodzilla_(MonsterVerse)", "Mechagodzilla"),
    ("godzilla", "Shimo", "Shimo"),
    ("godzilla", "Skar_King", "Skar King"),
    ("godzilla", "M.U.T.O.", "MUTO"),
    ("godzilla", "Destoroyah", "Destoroyah"),

    # Video Games
    ("godofwar", "Kratos", "Kratos"),
    ("witcher", "Geralt_of_Rivia", "Geralt"),
    ("halo", "John-117", "Master Chief"),
    ("reddead", "Arthur_Morgan", "Arthur Morgan"),
    ("thelastofus", "Ellie_Williams", "Ellie"),
    ("thelastofus", "Joel_Miller", "Joel"),
    ("ghostoftsushima", "Jin_Sakai", "Jin Sakai"),
    ("cyberpunk", "Johnny_Silverhand", "Johnny Silverhand"),
    ("finalfantasy", "Cloud_Strife", "Cloud Strife"),
    ("zelda", "Link", "Link"),

    # Anime
    ("dragonball", "Goku", "Goku"),
    ("onepiece", "Monkey_D._Luffy", "Luffy"),
    ("naruto", "Naruto_Uzumaki", "Naruto"),
    ("bleach", "Ichigo_Kurosaki", "Ichigo"),
    ("kimetsu-no-yaiba", "Tanjiro_Kamado", "Tanjiro"),
    ("kimetsu-no-yaiba", "Nezuko_Kamado", "Nezuko"),
    ("jujutsu-kaisen", "Satoru_Gojo", "Gojo"),
    ("shingekinokyojin", "Eren_Jaeger", "Eren"),
    ("shingekinokyojin", "Levi_Ackerman", "Levi"),
    ("onepunchman", "Saitama", "Saitama"),
]

def check(wiki, title, label):
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={title}&prop=pageimages&format=json&pithumbsize=600"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        data = json.loads(urllib.request.urlopen(req, timeout=5).read())
        pages = data.get("query", {}).get("pages", {})
        if "-1" in pages:
            print(f"MISSING: {label} ({wiki} / {title})")
            return False
        page = list(pages.values())[0]
        thumb = page.get("thumbnail", {}).get("source")
        if thumb:
            print(f"OK: {label} -> {thumb[:70]}...")
            return True
        else:
            print(f"NO_THUMB: {label} ({wiki} / {title})")
            return False
    except Exception as e:
        print(f"ERR: {label}: {e}")
        return False

success = 0
for wiki, title, label in TESTS:
    if check(wiki, title, label):
        success += 1
print(f"\nResult: {success} / {len(TESTS)} success")
