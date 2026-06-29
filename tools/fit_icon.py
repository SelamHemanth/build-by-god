#!/usr/bin/env python3
"""Regenerate launcher icons so the full 'Build By God' name survives the adaptive mask.

The adaptive foreground gets the artwork scaled into the safe zone over a matching
vertical gradient (so corners stay filled and nothing is cropped). Legacy square/round
icons keep the full-bleed artwork with their own masks.
"""
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MASTER = os.path.join(ROOT, "app_icon_master.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

# density -> (legacy launcher size, adaptive foreground size)
DENSITIES = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}

# Fraction of the tile the artwork occupies inside the adaptive foreground.
# 0.74 keeps the centered name well within the 0.667 safe circle.
CONTENT_SCALE = 0.70


def vgradient(size, top, bottom):
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        t = y / max(1, size - 1)
        r = int(top[0] + (bottom[0] - top[0]) * t)
        g = int(top[1] + (bottom[1] - top[1]) * t)
        b = int(top[2] + (bottom[2] - top[2]) * t)
        for x in range(size):
            px[x, y] = (r, g, b, 255)
    return img


def rounded_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m


def circle_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def main():
    master = Image.open(MASTER).convert("RGBA")
    top = master.getpixel((master.width // 2, 4))
    bottom = master.getpixel((master.width // 2, master.height - 5))
    print(f"gradient top={top[:3]} bottom={bottom[:3]}")

    for dens, (legacy, fg) in DENSITIES.items():
        d = os.path.join(RES, f"mipmap-{dens}")

        # Adaptive foreground: gradient fill + inset artwork (name stays inside safe zone)
        base = vgradient(fg, top, bottom)
        cs = int(fg * CONTENT_SCALE)
        art = master.resize((cs, cs), Image.LANCZOS)
        off = (fg - cs) // 2
        base.alpha_composite(art, (off, off))
        base.save(os.path.join(d, "ic_launcher_foreground.png"))

        # Legacy icons: full-bleed artwork with squircle / circle masks
        full = master.resize((legacy, legacy), Image.LANCZOS)
        sq = full.copy(); sq.putalpha(rounded_mask(legacy, int(legacy * 0.18)))
        sq.save(os.path.join(d, "ic_launcher.png"))
        rd = full.copy(); rd.putalpha(circle_mask(legacy))
        rd.save(os.path.join(d, "ic_launcher_round.png"))
        print(f"  {dens}: foreground {fg} (art {cs}), legacy {legacy}")


if __name__ == "__main__":
    main()
