import urllib.request
import urllib.parse
import json

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (https://cleantv.org; contact: support@cleantv.org)"
}

def resolve_commons_file(file_title):
    api = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.parse.quote(file_title)}&prop=imageinfo&iiprop=url&format=json"
    req = urllib.request.Request(api, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            data = json.loads(r.read())
            pages = data.get("query", {}).get("pages", {})
            for pid, p in pages.items():
                if "imageinfo" in p:
                    print(f"[{file_title}] -> {p['imageinfo'][0]['url']}")
                    return p['imageinfo'][0]['url']
    except Exception as e:
        print(f"Error {file_title}: {e}")
    return None

resolve_commons_file("File:Margot Robbie Barbie Premiere (53012600685).png")
resolve_commons_file("File:J. Robert Oppenheimer 1926 Alt.jpg")
resolve_commons_file("File:Jenna Ortega 2025.jpg")
resolve_commons_file("File:Stylised atom with three Bohr model orbits and stylised nucleus.png")
