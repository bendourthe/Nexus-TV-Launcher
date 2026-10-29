import urllib.request
import urllib.parse
import json

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

def search_wikimedia(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&format=json&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&srlimit=5"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            results = []
            for item in data.get('query', {}).get('search', []):
                title = item['title']
                results.append(title)
            return results
    except Exception as e:
        print("Error searching wikimedia:", e)
        return []

def get_wikimedia_image_url(file_title):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&format=json&titles={urllib.parse.quote(file_title)}&prop=imageinfo&iiprop=url|size"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            pages = data.get('query', {}).get('pages', {})
            for pid, pdata in pages.items():
                if 'imageinfo' in pdata and pdata['imageinfo']:
                    return pdata['imageinfo'][0]['url']
    except Exception as e:
        print("Error getting wikimedia url:", e)
        return None

def search_fandom(wiki, query):
    url = f"https://{wiki}.fandom.com/api.php?action=query&format=json&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&srlimit=5"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            results = []
            for item in data.get('query', {}).get('search', []):
                results.append(item['title'])
            return results
    except Exception as e:
        print(f"Error searching fandom ({wiki}):", e)
        return []

def get_fandom_image_url(wiki, file_title):
    url = f"https://{wiki}.fandom.com/api.php?action=query&format=json&titles={urllib.parse.quote(file_title)}&prop=imageinfo&iiprop=url|size"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            pages = data.get('query', {}).get('pages', {})
            for pid, pdata in pages.items():
                if 'imageinfo' in pdata and pdata['imageinfo']:
                    return pdata['imageinfo'][0]['url']
    except Exception as e:
        print("Error getting fandom url:", e)
        return None

print("Testing Spider-Man search on marvelcinematicuniverse:")
spidey_files = search_fandom("marvelcinematicuniverse", "Spider-Man No Way Home suit portrait")
print(spidey_files)
if spidey_files:
    print(get_fandom_image_url("marvelcinematicuniverse", spidey_files[0]))

print("Testing RoboCop on robocop fandom:")
robo_files = search_fandom("robocop", "RoboCop suit helmet")
print(robo_files)
if robo_files:
    print(get_fandom_image_url("robocop", robo_files[0]))

print("Testing Eiffel tower on Wikimedia:")
eiffel_files = search_wikimedia("Eiffel Tower Paris vertical sunset")
print(eiffel_files)
if eiffel_files:
    print(get_wikimedia_image_url(eiffel_files[0]))
