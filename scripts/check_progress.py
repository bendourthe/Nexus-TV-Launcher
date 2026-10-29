import os
import glob

d = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
files = glob.glob(os.path.join(d, "ic_avatar_*.png"))
print(f"Total ic_avatar files present: {len(files)}")
hp = glob.glob(os.path.join(d, "ic_avatar_hp_*.png"))
print(f"Harry Potter files: {len(hp)} -> {[os.path.basename(f) for f in hp]}")
got = glob.glob(os.path.join(d, "ic_avatar_got_*.png"))
print(f"Game of Thrones files: {len(got)} -> {[os.path.basename(f) for f in got]}")
sci = glob.glob(os.path.join(d, "ic_avatar_sci_*.png"))
print(f"Science files: {len(sci)} -> {[os.path.basename(f) for f in sci]}")
pop = glob.glob(os.path.join(d, "ic_avatar_pop_*.png"))
print(f"Pop Culture files: {len(pop)} -> {[os.path.basename(f) for f in pop]}")
