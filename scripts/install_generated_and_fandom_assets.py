import os
import glob
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
BRAIN_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3"
TEST_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "build", "test_renders")

def copy_generated(pattern, dest_item_id, target_w=256, top_pad=20):
    files = glob.glob(os.path.join(BRAIN_DIR, pattern))
    if files:
        latest = max(files, key=os.path.getmtime)
        img = PIL.Image.open(latest).convert("RGBA")
        w, h = img.size
        # Center crop square if not already square
        min_dim = min(w, h)
        cx, cy = (w - min_dim) // 2, (h - min_dim) // 2
        sq = img.crop((cx, cy, cx + min_dim, cy + min_dim)).resize((320, 320), PIL.Image.LANCZOS)
        out = os.path.join(DRAWABLE_DIR, f"ic_avatar_{dest_item_id}.png")
        sq.save(out, format="PNG", optimize=True)
        print(f"Installed generated asset -> ic_avatar_{dest_item_id}.png from {os.path.basename(latest)}")
        return True
    else:
        print(f"Pattern not found: {pattern}")
        return False

# Install generated assets
copy_generated("barbie_portrait_*.jpg", "pop_barbie")
copy_generated("oppenheimer_portrait_*.jpg", "pop_oppenheimer")
copy_generated("wednesday_portrait_*.jpg", "pop_wednesday")
copy_generated("willy_wonka_portrait_*.jpg", "pop_wonka")
copy_generated("indiana_jones_portrait_*.jpg", "pop_indy")
copy_generated("ghostbusters_portrait_*.jpg", "pop_ghostbusters")
copy_generated("dna_helix_portrait_*.jpg", "sci_dna")

# Verify Wolverine, Spider-Man, Superman, RoboCop from test_renders
verified_tests = [
    ("test_wolverine_perfect_circ.png", "marvel_wolverine"),
    ("test_spiderman_integrated_circ.png", "marvel_spiderman"),
    ("test_superman_cavill_circ.png", "dc_superman"),
    ("test_robocop_head_circ.png", "scifi_robocop"),
]
for src_fn, dest_id in verified_tests:
    sp = os.path.join(TEST_DIR, src_fn)
    if os.path.exists(sp):
        img = PIL.Image.open(sp).convert("RGBA").resize((320, 320), PIL.Image.LANCZOS)
        dp = os.path.join(DRAWABLE_DIR, f"ic_avatar_{dest_id}.png")
        img.save(dp, format="PNG", optimize=True)
        print(f"Installed verified test -> ic_avatar_{dest_id}.png")

print("Install completed.")
