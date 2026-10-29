import urllib.request
import json
import urllib.parse

def search_wiki(api_base, query):
    headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0"}
    url = f"{api_base}?action=opensearch&search={urllib.parse.quote(query)}&limit=5&format=json"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            print(f"Search '{query}' in {api_base}: {data[1]}")
            return data[1]
    except Exception as e:
        print(f"Search failed for {query}: {e}")
        return []

search_wiki("https://dccu.fandom.com/api.php", "Superman")
search_wiki("https://dccu.fandom.com/api.php", "Batman")
search_wiki("https://dccu.fandom.com/api.php", "Peacemaker")
search_wiki("https://dccu.fandom.com/api.php", "Harley Quinn")
search_wiki("https://en.wikipedia.org/w/api.php", "Marilyn Monroe")
search_wiki("https://en.wikipedia.org/w/api.php", "Oppenheimer")
search_wiki("https://en.wikipedia.org/w/api.php", "Indiana Jones")
search_wiki("https://en.wikipedia.org/w/api.php", "Marie Curie")
