import urllib.request
import urllib.parse
import re
import json
import io
import PIL.Image

def search_bing_images(query):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
    }
    url = f"https://www.bing.com/images/search?q={urllib.parse.quote(query)}&form=HDRSC2"
    req = urllib.request.Request(url, headers=headers)
    html = urllib.request.urlopen(req, timeout=10).read().decode('utf-8', errors='ignore')
    
    murls = re.findall(r'murl&quot;:&quot;(http[^&]+)&quot;', html)
    return murls

def download_and_verify(url):
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
    req = urllib.request.Request(url, headers=headers)
    try:
        data = urllib.request.urlopen(req, timeout=8).read()
        im = PIL.Image.open(io.BytesIO(data))
        return im.size, len(data)
    except Exception as e:
        return None, str(e)

if __name__ == '__main__':
    queries = [
        "Iron Man marvel helmet character portrait avatar",
        "Spider-Man ps5 portrait avatar",
        "Batman arkham cowl portrait avatar",
        "Darth Vader star wars helmet portrait avatar",
        "Master Chief halo helmet avatar portrait",
        "Planet Earth NASA blue marble square",
        "Bugatti Chiron supercar studio front",
    ]
    for q in queries:
        urls = search_bing_images(q)
        print(f"\nQuery: {q}")
        print(f"Found {len(urls)} candidates")
        for u in urls[:5]:
            size, info = download_and_verify(u)
            if size:
                print(f"  SUCCESS: {u[:80]}... size={size}")
                break
            else:
                print(f"  FAIL: {u[:60]}... ({info})")
