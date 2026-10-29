import urllib.request
import urllib.parse
import json

def find_pages(wiki, search_term):
    url = f"https://{wiki}.fandom.com/api.php?action=opensearch&search={urllib.parse.quote(search_term)}&limit=5&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        data = json.loads(urllib.request.urlopen(req, timeout=5).read())
        print(f"[{wiki}] search '{search_term}': {data[1]}")
    except Exception as e:
        print(f"[{wiki}] error: {e}")

find_pages("dcextendeduniverse", "Batman")
find_pages("dcextendeduniverse", "Superman")
find_pages("dcextendeduniverse", "Wonder Woman")
find_pages("dcextendeduniverse", "Joker")
find_pages("dcextendeduniverse", "Harley Quinn")
find_pages("dcextendeduniverse", "Peacemaker")
find_pages("starwars", "Darth Vader")
find_pages("starwars", "Kylo Ren")
find_pages("marvelcinematicuniverse", "Spider-Man")
find_pages("marvelcinematicuniverse", "Doctor Strange")
find_pages("marvelcinematicuniverse", "Deadpool")
find_pages("marvelcinematicuniverse", "Wolverine")
find_pages("dune", "Paul Atreides")
find_pages("dune", "Chani")
find_pages("matrix", "Neo")
find_pages("terminator", "T-800")
find_pages("avp", "Xenomorph")
find_pages("avp", "Ellen Ripley")
find_pages("bladerunner", "K")
find_pages("planetoftheapes", "Caesar")
