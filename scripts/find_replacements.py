import urllib.request
import urllib.parse
import json

HEADERS = {"User-Agent": "CleanTVLauncher/1.0"}

def search_wiki(wiki, query):
    q_enc = urllib.parse.quote(query)
    url = f"https://{wiki}.fandom.com/api.php?action=query&generator=search&gsrsearch={q_enc}&gsrlimit=5&prop=pageimages&pithumbsize=600&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            print(f"=== {wiki} : {query} ===")
            for pid, p in pages.items():
                thumb = p.get("thumbnail", {}).get("source", "No thumb")
                print(f"[{p['title']}] -> {thumb}")
    except Exception as e:
        print(f"Error {wiki}: {e}")

search_wiki("memory-alpha", "Spock")
search_wiki("planetoftheapes", "Caesar")
search_wiki("james-camerons-avatar", "Jake Sully")
search_wiki("avp", "Big Chap")
search_wiki("godzilla", "Godzilla")
search_wiki("godzilla", "King Kong")
search_wiki("godzilla", "King Ghidorah")
search_wiki("godzilla", "Mothra")
search_wiki("godzilla", "Mechagodzilla")
search_wiki("godzilla", "Rodan")
