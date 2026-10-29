import urllib.request
import urllib.parse
import json

def test_search(wiki, query):
    url = f"https://{wiki}.fandom.com/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrlimit=1&prop=pageimages&pithumbsize=600&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        data = json.loads(urllib.request.urlopen(req, timeout=5).read())
        pages = data.get("query", {}).get("pages", {})
        if not pages:
            print(f"FAILED: {wiki} / {query} - no pages")
            return None
        page = list(pages.values())[0]
        title = page.get("title")
        thumb = page.get("thumbnail", {}).get("source")
        print(f"OK: {wiki} / '{query}' -> '{title}': {'FOUND' if thumb else 'NO THUMB'}")
        if thumb:
            print(f"   {thumb[:90]}...")
        return thumb
    except Exception as e:
        print(f"ERR: {wiki} / {query}: {e}")
        return None

queries = [
    ("marvelcinematicuniverse", "Iron Man"),
    ("marvelcinematicuniverse", "Captain America"),
    ("marvelcinematicuniverse", "Thor"),
    ("marvelcinematicuniverse", "Spider-Man"),
    ("marvelcinematicuniverse", "Black Panther"),
    ("marvelcinematicuniverse", "Doctor Strange"),
    ("marvelcinematicuniverse", "Thanos"),
    ("marvelcinematicuniverse", "Loki"),
    ("marvelcinematicuniverse", "Deadpool"),
    ("marvelcinematicuniverse", "Wolverine"),
    ("dcextendeduniverse", "Batman"),
    ("dcextendeduniverse", "Superman"),
    ("dcextendeduniverse", "Wonder Woman"),
    ("dcextendeduniverse", "Joker"),
    ("dcextendeduniverse", "Peacemaker"),
    ("starwars", "Darth Vader"),
    ("starwars", "Din Djarin"),
    ("starwars", "Grogu"),
    ("starwars", "Ahsoka Tano"),
    ("starwars", "Boba Fett"),
    ("lotr", "Aragorn"),
    ("lotr", "Gandalf"),
    ("lotr", "Legolas"),
    ("lotr", "Gimli"),
    ("lotr", "Frodo Baggins"),
    ("godzilla", "Godzilla"),
    ("godzilla", "Kong"),
    ("godzilla", "King Ghidorah"),
    ("godofwar", "Kratos"),
    ("witcher", "Geralt of Rivia"),
    ("halo", "Master Chief"),
    ("thelastofus", "Ellie"),
    ("dragonball", "Goku"),
    ("onepiece", "Monkey D. Luffy"),
    ("naruto", "Naruto Uzumaki"),
    ("bleach", "Ichigo Kurosaki"),
    ("kimetsu-no-yaiba", "Tanjiro Kamado"),
    ("jujutsu-kaisen", "Satoru Gojo"),
]

for wiki, q in queries:
    test_search(wiki, q)
