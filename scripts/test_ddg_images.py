import urllib.request
import urllib.parse
import json
import re

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def ddg_image(query):
    try:
        url = "https://duckduckgo.com/?q=" + urllib.parse.quote(query) + "&t=h_&iax=images&ia=images"
        req = urllib.request.Request(url, headers=HEADERS)
        html = urllib.request.urlopen(req, timeout=6).read().decode("utf-8")
        m = re.search(r'vqd=([\d-]+)', html)
        if not m:
            print("No vqd for", query)
            return None
        vqd = m.group(1)
        api_url = f"https://duckduckgo.com/i.js?l=us-en&o=json&q={urllib.parse.quote(query)}&vqd={vqd}&f=,,,"
        api_req = urllib.request.Request(api_url, headers=HEADERS)
        data = json.loads(urllib.request.urlopen(api_req, timeout=6).read())
        results = data.get("results", [])
        if results:
            img_url = results[0]["image"]
            print(f"FOUND for '{query}':\n  {img_url}")
            return img_url
        else:
            print(f"No results for {query}")
    except Exception as e:
        print(f"ERR for {query}: {e}")
    return None

ddg_image("Ferrari SF90 Stradale front view")
ddg_image("Paul Atreides Dune 2 face portrait")
ddg_image("Golden Retriever smiling face portrait")
