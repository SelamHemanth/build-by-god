#!/usr/bin/env python3
"""Add a 'powered by hemanth' tagline to the master app icon and regenerate
all launcher assets (adaptive foreground, squircle, and round) at every density.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MASTER = os.path.join(ROOT, "app_icon_master.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

FONT_CANDIDATES = [
    "/usr/share/fonts/google-noto-vf/NotoSans-VF.ttf",
    "/usr/share/fonts/google-droid-sans-fonts/DroidSans-Bold.ttf",
    "/usr/share/fonts/liberation-mono/LiberationMono-Bold.ttf",
]

# density -> (legacy launcher size, adaptive foreground size)
DENSITIES = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}

TAGLINE = "powered by hemanth"
GOLD = (244, 178, 70)


def load_font(size):
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def add_tagline(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    w, h = img.size
    font = load_font(int(h * 0.052))

    # measure text
    tmp = ImageDraw.Draw(img)
    bbox = tmp.textbbox((0, 0), TAGLINE, font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    x = (w - tw) / 2 - bbox[0]
    y = int(h * 0.855)

    # soft glow layer
    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.text((x, y), TAGLINE, font=font, fill=(255, 200, 110, 180))
    glow = glow.filter(ImageFilter.GaussianBlur(int(h * 0.012)))
    img = Image.alpha_composite(img, glow)

    # dark shadow for contrast, then gold fill
    d = ImageDraw.Draw(img)
    off = max(1, int(h * 0.0025))
    d.text((x + off, y + off), TAGLINE, font=font, fill=(0, 0, 0, 200))
    d.text((x, y), TAGLINE, font=font, fill=GOLD + (255,))
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
    master = Image.open(MASTER)
    master = add_tagline(master)
    master.convert("RGB").save(MASTER)
    print(f"updated {MASTER}")

    for dens, (legacy, fg) in DENSITIES.items():
        d = os.path.join(RES, f"mipmap-{dens}")

        # adaptive foreground: full-bleed master
        master.resize((fg, fg), Image.LANCZOS).save(os.path.join(d, "ic_launcher_foreground.png"))

        base = master.resize((legacy, legacy), Image.LANCZOS)

        sq = base.copy()
        sq.putalpha(rounded_mask(legacy, int(legacy * 0.18)))
        sq.save(os.path.join(d, "ic_launcher.png"))

        rd = base.copy()
        rd.putalpha(circle_mask(legacy))
        rd.save(os.path.join(d, "ic_launcher_round.png"))
        print(f"  {dens}: launcher {legacy}px, foreground {fg}px")


if __name__ == "__main__":
    main()
