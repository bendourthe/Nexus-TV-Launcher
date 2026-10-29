import urllib.request
import json
import urllib.parse

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
}

def check_pages(api_base, titles):
    headers = dict(HEADERS)
    headers["Referer"] = api_base.split("/api.php")[0] + "/"
    for t in titles:
        url = f"{api_base}?action=query&titles={urllib.parse.quote(t)}&prop=pageimages&pithumbsize=800&format=json"
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=5) as r:
                data = json.loads(r.read())
                pages = data.get("query", {}).get("pages", {})
                for pid, p in pages.items():
                    thumb = p.get("thumbnail", {}).get("source", "NO_THUMB")
                    print(f"[{api_base.split('.')[0].replace('https://','')}] {t} -> {thumb[:80]}")
        except Exception as e:
            print(f"[{t}] Error: {e}")

print("--- Testing Harry Potter ---")
check_pages("https://harrypotter.fandom.com/api.php", [
    "Harry Potter", "Hermione Granger", "Ron Weasley", "Albus Dumbledore", "Severus Snape",
    "Tom Riddle", "Sirius Black", "Rubeus Hagrid", "Draco Malfoy", "Dobby"
])

print("\n--- Testing Game of Thrones ---")
check_pages("https://gameofthrones.fandom.com/api.php", [
    "Jon Snow", "Daenerys Targaryen", "Tyrion Lannister", "Arya Stark", "Cersei Lannister",
    "Jaime Lannister", "Night King", "Eddard Stark", "Brienne of Tarth", "Drogon"
])

print("\n--- Testing Science ---")
check_pages("https://en.wikipedia.org/w/api.php", [
    "Albert Einstein", "Marie Curie", "Isaac Newton", "Nikola Tesla", "Stephen Hawking",
    "Carl Sagan", "Galileo Galilei", "Charles Darwin", "DNA", "Atom"
])

print("\n--- Testing Pop Culture ---")
check_pages("https://en.wikipedia.org/w/api.php", [
    "Vito Corleone", "Barbie (film)", "Oppenheimer (film)", "Marilyn Monroe", "Elvis Presley",
    "Indiana Jones (character)", "Wednesday Addams", "Willy Wonka", "Pete Mitchell (Top Gun)", "Ghostbusters"
])
