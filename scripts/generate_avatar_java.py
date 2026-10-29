import os
import sys

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from scripts.audit_and_grade_all_220 import CATEGORIES

lines = ["    public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays.asList("]
total = sum(len(items) for _, items in CATEGORIES)
count = 0

for cat, items in CATEGORIES:
    lines.append(f"            // --- Category: {cat} (10 items) ---")
    for id_val, lbl, desc in items:
        count += 1
        comma = "," if count < total else ""
        lines.append(f'            new AvatarCharacter("{id_val}", "{lbl}", "{cat}", "{desc}", R.drawable.ic_avatar_{id_val}){comma}')

lines.append("    );")

out_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "launcher", "build", "avatar_characters.java.txt")
with open(out_path, "w", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")

print(f"Generated {count} avatar definitions into {out_path}")
