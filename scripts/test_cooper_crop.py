import os
import PIL.Image

img = PIL.Image.open("launcher/build/cooper2.png").convert("RGBA")
w, h = img.size
# Crop on Cooper's head and helmet
crop = img.crop((160, 0, 480, h)) # 320 x 268
cw, ch = crop.size
# Pad top and bottom to make 320x320
sq = PIL.Image.new("RGBA", (320, 320), (10, 15, 20, 255))
sq.paste(crop, (0, (320 - ch) // 2))

out_path = os.path.join("launcher", "app", "src", "main", "res", "drawable", "ic_avatar_scifi_cooper.png")
sq.save(out_path, format="PNG", optimize=True)
print("Saved authentic Cooper still to ic_avatar_scifi_cooper.png")
