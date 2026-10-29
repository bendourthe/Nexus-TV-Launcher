import urllib.request
import json
import urllib.parse

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: support@cleantv.org)"
}

def search_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&format=json&srlimit=3"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            results = data.get("query", {}).get("search", [])
            print(f"Results for '{query}':")
            for item in results:
                print(f"  {item['title']}")
    except Exception as e:
        print(f"Failed {query}: {e}")

search_commons("Margot Robbie Barbie")
search_commons("J. Robert Oppenheimer photo")
search_commons("Jenna Ortega Wednesday")
search_commons("Gene Wilder Willy Wonka")
