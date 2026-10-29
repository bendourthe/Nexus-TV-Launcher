import os
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

TARGET_DIR = r"C:\Users\bdour\.gemini\antigravity-ide\scratch\android-tv-clean-launcher\launcher\app\src\main\res\drawable"
SIZE = 320

def create_base(bg_color=(15, 17, 24)):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((0, 0, SIZE, SIZE), fill=bg_color)
    return img, draw

def add_rim(draw, color=(255, 255, 255, 60), width=4):
    draw.ellipse((width//2, width//2, SIZE - width//2, SIZE - width//2), outline=color, width=width)

def save_avatar(img, name):
    mask = Image.new("L", (SIZE, SIZE), 0)
    m_draw = ImageDraw.Draw(mask)
    m_draw.ellipse((0, 0, SIZE, SIZE), fill=255)
    img.putalpha(mask)
    path = os.path.join(TARGET_DIR, f"{name}.png")
    img.save(path, "PNG")
    print(f"Generated {name}.png")

def draw_gradient_circle(draw, c_start, c_end, cx=160, cy=160, r=150):
    for i in range(r, 0, -1):
        ratio = 1.0 - (i / r)
        c = tuple(int(c_start[k] * (1 - ratio) + c_end[k] * ratio) for k in range(3))
        draw.ellipse((cx - i, cy - i, cx + i, cy + i), fill=c)

# 1. PLANETS
def gen_planets():
    planets = [
        ("ic_avatar_planet_earth", (10, 45, 120), (30, 160, 240), (40, 180, 80), "Earth"),
        ("ic_avatar_planet_mars", (160, 30, 15), (230, 90, 40), (200, 160, 120), "Mars"),
        ("ic_avatar_planet_jupiter", (140, 80, 30), (220, 160, 90), (180, 60, 20), "Jupiter"),
        ("ic_avatar_planet_saturn", (180, 150, 70), (230, 210, 130), (140, 110, 50), "Saturn"),
        ("ic_avatar_planet_neptune", (15, 30, 140), (20, 120, 240), (160, 230, 255), "Neptune"),
        ("ic_avatar_planet_moon", (40, 45, 55), (170, 175, 185), (90, 95, 105), "Moon"),
        ("ic_avatar_planet_sun", (220, 60, 10), (255, 200, 20), (255, 255, 200), "Sun"),
        ("ic_avatar_planet_kepler", (40, 10, 80), (130, 40, 190), (0, 230, 200), "Kepler"),
        ("ic_avatar_planet_gliesia", (20, 70, 110), (90, 200, 230), (230, 250, 255), "Gliesia"),
        ("ic_avatar_planet_inferno", (100, 10, 10), (240, 50, 10), (255, 200, 30), "Inferno"),
    ]
    for name, c1, c2, c3, label in planets:
        img, draw = create_base((8, 10, 18))
        draw_gradient_circle(draw, c1, c2, 160, 160, 140)
        # Add surface atmospheric bands / features
        for y in range(40, 280, 18):
            alpha = int(45 + 30 * math.sin(y * 0.15))
            draw.chord((30, y, 290, y + 25), start=0, end=180, fill=c3 + (alpha,))
        if "saturn" in name:
            # Draw planetary rings
            draw.ellipse((-20, 135, 340, 185), outline=(240, 220, 160, 180), width=14)
            draw.ellipse((0, 142, 320, 178), outline=(200, 170, 100, 130), width=6)
        add_rim(draw, (c2[0], c2[1], c2[2], 120), 4)
        save_avatar(img, name)

# 2. SUPERHERO
def gen_superheroes():
    heroes = [
        ("ic_avatar_hero_iron", (160, 20, 20), (240, 190, 40), (0, 240, 255)),
        ("ic_avatar_hero_spider", (190, 20, 30), (20, 40, 140), (255, 255, 255)),
        ("ic_avatar_hero_bat", (20, 22, 28), (60, 65, 75), (230, 200, 50)),
        ("ic_avatar_hero_solar", (20, 50, 160), (220, 30, 30), (255, 230, 60)),
        ("ic_avatar_hero_thunder", (50, 70, 110), (180, 200, 230), (0, 230, 255)),
        ("ic_avatar_hero_amazon", (180, 130, 20), (190, 30, 40), (255, 230, 120)),
        ("ic_avatar_hero_shield", (20, 40, 140), (220, 30, 30), (240, 240, 255)),
        ("ic_avatar_hero_panther", (15, 15, 20), (45, 45, 60), (170, 80, 255)),
        ("ic_avatar_hero_speed", (190, 20, 20), (255, 210, 30), (255, 255, 255)),
        ("ic_avatar_hero_mystic", (15, 50, 45), (180, 110, 20), (0, 255, 180)),
    ]
    for name, c1, c2, c3 in heroes:
        img, draw = create_base((12, 14, 20))
        draw_gradient_circle(draw, c1, c2, 160, 160, 135)
        # Emblem / Visor polygon
        points = [(160, 60), (230, 130), (160, 250), (90, 130)]
        draw.polygon(points, fill=c3 + (170,), outline=(255, 255, 255, 200))
        draw.ellipse((135, 135, 185, 185), fill=(255, 255, 255, 230))
        add_rim(draw, (c3[0], c3[1], c3[2], 140), 4)
        save_avatar(img, name)

# 3. FANTASY
def gen_fantasy():
    fantasy = [
        ("ic_avatar_fantasy_dragon", (160, 30, 20), (240, 90, 20), (255, 210, 50)),
        ("ic_avatar_fantasy_archmage", (40, 20, 90), (120, 60, 210), (100, 220, 255)),
        ("ic_avatar_fantasy_elf", (20, 80, 40), (60, 180, 90), (230, 240, 180)),
        ("ic_avatar_fantasy_paladin", (60, 70, 90), (190, 200, 220), (255, 220, 80)),
        ("ic_avatar_fantasy_phoenix", (190, 50, 10), (255, 140, 20), (255, 250, 140)),
        ("ic_avatar_fantasy_dwarf", (100, 60, 30), (170, 100, 50), (180, 220, 240)),
        ("ic_avatar_fantasy_rogue", (20, 25, 35), (50, 60, 80), (140, 90, 220)),
        ("ic_avatar_fantasy_valkyrie", (40, 80, 120), (110, 180, 240), (255, 240, 200)),
        ("ic_avatar_fantasy_druid", (30, 60, 30), (90, 140, 50), (220, 180, 100)),
        ("ic_avatar_fantasy_griffin", (140, 100, 30), (210, 160, 60), (240, 240, 240)),
    ]
    for name, c1, c2, c3 in fantasy:
        img, draw = create_base((14, 16, 24))
        draw_gradient_circle(draw, c1, c2, 160, 160, 135)
        # Fantasy crest / diamond
        draw.polygon([(160, 70), (235, 160), (160, 250), (85, 160)], fill=c3 + (140,), outline=(255, 255, 255, 180))
        draw.polygon([(160, 100), (210, 160), (160, 220), (110, 160)], fill=(255, 255, 255, 190))
        add_rim(draw, (c3[0], c3[1], c3[2], 130), 4)
        save_avatar(img, name)

# 4. SCI-FI
def gen_scifi():
    scifi = [
        ("ic_avatar_scifi_cyborg", (20, 30, 45), (40, 80, 120), (0, 230, 255)),
        ("ic_avatar_scifi_pilot", (30, 50, 70), (70, 130, 180), (255, 140, 40)),
        ("ic_avatar_scifi_android", (40, 45, 60), (130, 145, 175), (0, 255, 180)),
        ("ic_avatar_scifi_mech", (50, 55, 65), (110, 120, 135), (255, 60, 40)),
        ("ic_avatar_scifi_alien", (25, 15, 50), (90, 30, 150), (180, 90, 255)),
        ("ic_avatar_scifi_hacker", (15, 30, 25), (30, 90, 60), (0, 255, 120)),
        ("ic_avatar_scifi_trooper", (35, 40, 50), (90, 100, 120), (0, 190, 255)),
        ("ic_avatar_scifi_bounty", (50, 40, 30), (120, 90, 60), (240, 170, 40)),
        ("ic_avatar_scifi_drone", (25, 35, 50), (60, 95, 140), (0, 240, 255)),
        ("ic_avatar_scifi_commander", (20, 30, 60), (50, 80, 150), (240, 210, 80)),
    ]
    for name, c1, c2, c3 in scifi:
        img, draw = create_base((10, 12, 18))
        draw_gradient_circle(draw, c1, c2, 160, 160, 135)
        # Tech hex visor / HUD grid
        for angle in range(0, 360, 60):
            rad = math.radians(angle)
            x1 = 160 + int(70 * math.cos(rad))
            y1 = 160 + int(70 * math.sin(rad))
            x2 = 160 + int(70 * math.cos(rad + math.pi/3))
            y2 = 160 + int(70 * math.sin(rad + math.pi/3))
            draw.line([(x1, y1), (x2, y2)], fill=c3 + (200,), width=3)
        draw.ellipse((140, 140, 180, 180), fill=c3 + (240,))
        add_rim(draw, (c3[0], c3[1], c3[2], 150), 4)
        save_avatar(img, name)

# 5. VEHICLES
def gen_vehicles():
    vehicles = [
        ("ic_avatar_vehicle_supercar", (150, 15, 25), (230, 40, 50), (255, 230, 50)),
        ("ic_avatar_vehicle_jet", (40, 60, 85), (100, 140, 180), (0, 220, 255)),
        ("ic_avatar_vehicle_spaceshuttle", (25, 30, 50), (70, 85, 130), (240, 240, 255)),
        ("ic_avatar_vehicle_motorcycle", (20, 35, 45), (40, 90, 120), (0, 240, 180)),
        ("ic_avatar_vehicle_offroad", (70, 55, 30), (140, 100, 50), (230, 160, 40)),
        ("ic_avatar_vehicle_yacht", (20, 70, 110), (40, 160, 210), (255, 255, 255)),
        ("ic_avatar_vehicle_train", (40, 50, 70), (120, 140, 180), (240, 60, 50)),
        ("ic_avatar_vehicle_helicopter", (140, 70, 20), (220, 120, 30), (255, 240, 200)),
        ("ic_avatar_vehicle_vessel", (15, 35, 60), (30, 90, 130), (240, 200, 40)),
        ("ic_avatar_vehicle_rover", (120, 40, 20), (190, 80, 40), (240, 220, 150)),
    ]
    for name, c1, c2, c3 in vehicles:
        img, draw = create_base((12, 14, 20))
        draw_gradient_circle(draw, c1, c2, 160, 160, 135)
        # Aerodynamic silhouette
        draw.polygon([(80, 175), (160, 95), (240, 175), (160, 225)], fill=c3 + (150,), outline=(255, 255, 255, 190))
        draw.line([(80, 175), (240, 175)], fill=(255, 255, 255, 220), width=3)
        add_rim(draw, (c3[0], c3[1], c3[2], 140), 4)
        save_avatar(img, name)

if __name__ == "__main__":
    gen_planets()
    gen_superheroes()
    gen_fantasy()
    gen_scifi()
    gen_vehicles()
    print("ALL 50 AVATARS GENERATED SUCCESSFULLY!")
