import urllib.request, urllib.parse, json

items = [
    ("dcextendeduniverse", "Batman"),
    ("dcextendeduniverse", "Superman"),
    ("dcextendeduniverse", "Wonder_Woman"),
    ("dcextendeduniverse", "Joker"),
    ("dcextendeduniverse", "Harley_Quinn"),
    ("dcextendeduniverse", "Peacemaker"),
    ("marvelcinematicuniverse", "Spider-Man"),
    ("marvelcinematicuniverse", "Doctor_Strange"),
    ("marvelcinematicuniverse", "Deadpool"),
    ("marvelcinematicuniverse", "Wolverine"),
    ("dune", "Paul_Atreides"),
    ("dune", "Chani_Kynes"),
    ("matrix", "Neo"),
    ("terminator", "T-800"),
    ("avp", "Xenomorph_XX121_(Alien)"),
    ("avp", "Ellen_Ripley"),
    ("bladerunner", "K"),
    ("planetoftheapes", "Caesar"),
    ("starwars", "Ben_Solo"),
]

for wiki, title in items:
    url = f"https://{wiki}.fandom.com/api.php?action=query&titles={urllib.parse.quote(title)}&prop=pageimages&format=json&pithumbsize=600"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        data = json.loads(urllib.request.urlopen(req, timeout=5).read())
        pages = data.get("query", {}).get("pages", {})
        page = list(pages.values())[0]
        thumb = page.get("thumbnail", {}).get("source")
        print(f"{wiki} / {title}: {'FOUND' if thumb else 'NO THUMB'}")
        if thumb:
            print(f"   {thumb[:80]}...")
    except Exception as e:
        print(f"ERR {wiki} / {title}: {e}")
