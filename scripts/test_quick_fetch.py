import urllib.request
import time

url = "https://static.wikia.nocookie.net/marvelcinematicuniverse/images/d/d7/CapAmerica-EndgameProfile.jpg/revision/latest"
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Referer": "https://marvelcinematicuniverse.fandom.com/",
    "Accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
}
t0 = time.time()
print("Starting request with Referer...")
try:
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=8) as r:
        data = r.read()
        print(f"Success! Fetched {len(data)} bytes in {time.time()-t0:.2f}s")
except Exception as e:
    print(f"Error in {time.time()-t0:.2f}s: {e}")
