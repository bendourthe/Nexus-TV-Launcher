import os
import io
import time
import urllib.request
import urllib.parse
import PIL.Image
import PIL.ImageDraw

DRAWABLE_DIR = os.path.join(os.path.dirname(__file__), "..", "launcher", "app", "src", "main", "res", "drawable")
USER_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\brain\57236744-3d86-4ee8-aa16-e55136f26fe3\.user_uploaded"

HEADERS = {
    "User-Agent": "CleanTVLauncher/1.0 (contact: admin@cleantv.org)"
}

def create_circular_preview(img_320):
    mask = PIL.Image.new("L", (320, 320), 0)
    draw = PIL.ImageDraw.Draw(mask)
    draw.ellipse((0, 0, 320, 320), fill=255)
    preview = PIL.Image.new("RGBA", (320, 320), (0, 0, 0, 0))
    preview.paste(img_320, (0, 0), mask=mask)
    draw_p = PIL.ImageDraw.Draw(preview)
    draw_p.ellipse((2, 2, 318, 318), outline=(255, 255, 255, 70), width=4)
    return preview

# 1. Kratos (from user upload) - ensure head is NOT cropped
kratos_path = os.path.join(USER_DIR, "media_1792982658615.png")
kratos_raw = PIL.Image.open(kratos_path).convert("RGBA")
kw, kh = kratos_raw.size
target_w = 260
scale = target_w / kw
target_h = int(kh * scale)
kratos_scaled = kratos_raw.resize((target_w, target_h), PIL.Image.LANCZOS)

kratos_canvas = PIL.Image.new("RGBA", (320, 320), (20, 23, 31, 255))
paste_x = (320 - target_w) // 2
paste_y = 24 # 24px margin above head crown ensures it never touches avatar circular rim
kratos_canvas.paste(kratos_scaled, (paste_x, paste_y), mask=kratos_scaled)
kratos_canvas.save(os.path.join(DRAWABLE_DIR, "ic_avatar_game_kratos.png"), format="PNG", optimize=True)
print("Saved ic_avatar_game_kratos.png")

# 2. Ichigo (from user upload) - head & bust composition
ichigo_path = os.path.join(USER_DIR, "media_1792982736063.png")
ichigo_raw = PIL.Image.open(ichigo_path).convert("RGBA")
iw, ih = ichigo_raw.size
crop_size = min(iw, ih) # 300
crop_x = (iw - crop_size) // 2
crop_y = 8
ichigo_cropped = ichigo_raw.crop((crop_x, crop_y, crop_x + crop_size, crop_y + crop_size))
ichigo_scaled = ichigo_cropped.resize((290, 290), PIL.Image.LANCZOS)
ichigo_canvas = PIL.Image.new("RGBA", (320, 320), (235, 225, 175, 255))
ichigo_canvas.paste(ichigo_scaled, (15, 15))
ichigo_canvas.save(os.path.join(DRAWABLE_DIR, "ic_avatar_anime_ichigo.png"), format="PNG", optimize=True)
print("Saved ic_avatar_anime_ichigo.png")

# 3. Master Chief (Halo Infinite helmet & bust on dark slate studio)
chief_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/en/4/42/Master_chief_halo_infinite.png',
    headers=HEADERS
)).read()
chief_img = PIL.Image.open(io.BytesIO(chief_raw)).convert("RGBA")
# Crop helmet & chest armor: (15, 10, 215, 210)
chief_crop = chief_img.crop((15, 10, 215, 210))
cw, ch = chief_crop.size
chief_scaled = chief_crop.resize((270, 270), PIL.Image.LANCZOS)
chief_canvas = PIL.Image.new("RGBA", (320, 320), (20, 23, 31, 255))
chief_canvas.paste(chief_scaled, (25, 25), mask=chief_scaled)
chief_canvas.save(os.path.join(DRAWABLE_DIR, "ic_avatar_game_chief.png"), format="PNG", optimize=True)
print("Saved ic_avatar_game_chief.png")

# 4. Tanjiro (head & bust with scar, earrings, haori)
tanjiro_raw = urllib.request.urlopen(urllib.request.Request(
    'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/0/05/Tanjiro_anime_right_face.png/revision/latest',
    headers=HEADERS
)).read()
tanjiro_img = PIL.Image.open(io.BytesIO(tanjiro_raw)).convert("RGBA")
tanjiro_crop = tanjiro_img.crop((110, 10, 560, 460))
tanjiro_final = tanjiro_crop.resize((320, 320), PIL.Image.LANCZOS)
tanjiro_final.save(os.path.join(DRAWABLE_DIR, "ic_avatar_anime_tanjiro.png"), format="PNG", optimize=True)
print("Saved ic_avatar_anime_tanjiro.png")

# 5. Nezuko (head & bust with bamboo muzzle and pink eyes)
nezuko_raw = urllib.request.urlopen(urllib.request.Request(
    'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/0/0e/Nezuko_anime_right_face.png/revision/latest',
    headers=HEADERS
)).read()
nezuko_img = PIL.Image.open(io.BytesIO(nezuko_raw)).convert("RGBA")
nezuko_crop = nezuko_img.crop((90, 20, 540, 470))
nezuko_final = nezuko_crop.resize((320, 320), PIL.Image.LANCZOS)
nezuko_final.save(os.path.join(DRAWABLE_DIR, "ic_avatar_anime_nezuko.png"), format="PNG", optimize=True)
print("Saved ic_avatar_anime_nezuko.png")

# 6. Captain America (Chris Evans helmet bust)
cap_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/en/6/6b/Chris_Evans_as_Steve_Rogers_Captain_America.jpg',
    headers=HEADERS
)).read()
cap_img = PIL.Image.open(io.BytesIO(cap_raw)).convert("RGBA")
cw, ch = cap_img.size
c_size = min(cw, ch)
cap_crop = cap_img.crop(((cw - c_size) // 2, 0, (cw - c_size) // 2 + c_size, c_size))
cap_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_marvel_captain.png"), format="PNG", optimize=True)
print("Saved ic_avatar_marvel_captain.png")

# 7. Loki (Tom Hiddleston MCU face bust)
loki_raw = urllib.request.urlopen(urllib.request.Request(
    'https://static.wikia.nocookie.net/marvelcinematicuniverse/images/b/b5/Loki_Infobox.png/revision/latest',
    headers=HEADERS
)).read()
loki_img = PIL.Image.open(io.BytesIO(loki_raw)).convert("RGBA")
lw, lh = loki_img.size
l_size = min(lw, lh)
loki_crop = loki_img.crop(((lw - l_size) // 2, 0, (lw - l_size) // 2 + l_size, l_size))
loki_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_marvel_loki.png"), format="PNG", optimize=True)
print("Saved ic_avatar_marvel_loki.png")

# 8. Luke Skywalker (Jedi Master face portrait)
luke_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/en/2/2d/Luke_Skywalker_episode_8.jpg',
    headers=HEADERS
)).read()
luke_img = PIL.Image.open(io.BytesIO(luke_raw)).convert("RGBA")
lw, lh = luke_img.size
l_size = min(lw, lh)
luke_crop = luke_img.crop(((lw - l_size) // 2, int(0.08 * (lh - l_size)), (lw - l_size) // 2 + l_size, int(0.08 * (lh - l_size)) + l_size))
luke_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_sw_luke.png"), format="PNG", optimize=True)
print("Saved ic_avatar_sw_luke.png")

# 9. Siberian Husky (close-up icy blue eyes face)
husky_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/commons/3/38/Siberian_husky_ice_blue_eyes_detail.jpg',
    headers=HEADERS
)).read()
husky_img = PIL.Image.open(io.BytesIO(husky_raw)).convert("RGBA")
hw, hh = husky_img.size
h_size = min(hw, hh)
husky_crop = husky_img.crop(((hw - h_size) // 2, (hh - h_size) // 2, (hw - h_size) // 2 + h_size, (hh - h_size) // 2 + h_size))
husky_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_pet_husky.png"), format="PNG", optimize=True)
print("Saved ic_avatar_pet_husky.png")

# 10. German Shepherd (close-up alert face)
gs_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/commons/d/d0/German_Shepherd_-_DSC_0346_%2810096362833%29.jpg',
    headers=HEADERS
)).read()
gs_img = PIL.Image.open(io.BytesIO(gs_raw)).convert("RGBA")
gs_crop = gs_img.crop((40, 25, 340, 325))
gs_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_pet_germanshepherd.png"), format="PNG", optimize=True)
print("Saved ic_avatar_pet_germanshepherd.png")

# 11. Golden Retriever (close-up smiling face)
gr_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/commons/b/bf/D%C3%BClmen%2C_Hausd%C3%BClmen%2C_Golden_Retriever_--_2022_--_5945.jpg',
    headers=HEADERS
)).read()
gr_img = PIL.Image.open(io.BytesIO(gr_raw)).convert("RGBA")
gw, gh = gr_img.size
g_size = min(gw, gh)
gr_crop = gr_img.crop(((gw - g_size) // 2, int(0.12 * (gh - g_size)), (gw - g_size) // 2 + g_size, int(0.12 * (gh - g_size)) + g_size))
gr_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_pet_retriever.png"), format="PNG", optimize=True)
print("Saved ic_avatar_pet_retriever.png")

# 12. French Bulldog (close-up fawn face)
fb_raw = urllib.request.urlopen(urllib.request.Request(
    'https://upload.wikimedia.org/wikipedia/commons/6/68/Bulldog_franc%C3%A9s_fawn.jpg',
    headers=HEADERS
)).read()
fb_img = PIL.Image.open(io.BytesIO(fb_raw)).convert("RGBA")
fw, fh = fb_img.size
f_size = min(fw, fh)
fb_crop = fb_img.crop(((fw - f_size) // 2, int(0.15 * (fh - f_size)), (fw - f_size) // 2 + f_size, int(0.15 * (fh - f_size)) + f_size))
fb_crop.resize((320, 320), PIL.Image.LANCZOS).save(os.path.join(DRAWABLE_DIR, "ic_avatar_pet_frenchie.png"), format="PNG", optimize=True)
print("Saved ic_avatar_pet_frenchie.png")

print("All avatar portraits perfected!")
