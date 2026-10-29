import re
import collections

with open("launcher/app/src/main/java/com/cleantv/launcher/WatchNextManager.java", encoding="utf-8") as f:
    text = f.read()

cats = re.findall(r'new AvatarCharacter\([^,]+,\s*[^,]+,\s*"([^"]+)"', text)
counts = collections.Counter(cats)
print("Detected Categories and counts:")
for cat, count in counts.items():
    print(f"  {cat}: {count}")

expected_cats = [
    "Landscapes",
    "Space",
    "Planets",
    "Animals",
    "Pets",
    "Cars",
    "Music",
    "Abstract",
    "Marvel",
    "DC Comics",
    "Star Wars",
    "Lord of the Rings",
    "Kaijus",
    "Sci-Fi",
    "Video Games",
    "Anime"
]

assert list(counts.keys()) == expected_cats, f"Category order mismatch: {list(counts.keys())} vs {expected_cats}"
assert all(v == 10 for v in counts.values()), "Some categories don't have exactly 10 avatars!"
print("ALL 16 CATEGORIES VERIFIED WITH EXACTLY 10 AVATARS EACH IN EXACT REQUESTED SEQUENCE!")
