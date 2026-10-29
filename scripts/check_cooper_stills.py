import urllib.request
import io
import PIL.Image

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CleanTVLauncher/1.0",
    "Referer": "https://interstellarfilm.fandom.com/"
}

def check_img(url, out_name):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=10) as resp:
        img = PIL.Image.open(io.BytesIO(resp.read()))
        print(f"[{out_name}] Size: {img.size}")
        img.save(f"launcher/build/{out_name}.png")

check_img("https://static.wikia.nocookie.net/interstellarfilm/images/d/de/Josephcooper1.jpg/revision/latest", "cooper1")
check_img("https://static.wikia.nocookie.net/interstellarfilm/images/b/b6/Josephcooper2.jpg/revision/latest", "cooper2")
