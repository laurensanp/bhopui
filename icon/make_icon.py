"""Draws the BhopUI icon: a bunny hopping off a springy menu button. Run: python -I icon/make_icon.py"""
from pathlib import Path
from PIL import Image, ImageDraw

PANEL, INACTIVE, ACCENT, WHITE = "#201a0e", "#312c29", "#f1bea6", "#ffffff"
S = 2048  # drawn at 4x of 512, then downscaled for smooth edges
OUT = Path(__file__).parent


def bar(img, cx, cy, w, h, fill, alpha=1.0):
    layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(layer).rounded_rectangle((cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2), radius=h * 0.3, fill=fill)
    if alpha < 1:
        layer.putalpha(layer.getchannel("A").point(lambda a: int(a * alpha)))
    img.alpha_composite(layer)


def oval(img, cx, cy, w, h, angle, fill):
    """Ellipse rotated counter-clockwise by angle degrees around its centre."""
    n = int(max(w, h)) + 8
    layer = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(((n - w) / 2, (n - h) / 2, (n + w) / 2, (n + h) / 2), fill=fill)
    layer = layer.rotate(angle, resample=Image.BICUBIC)
    img.alpha_composite(layer, (int(cx - n / 2), int(cy - n / 2)))


def draw():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(img).rounded_rectangle((0, 0, S - 1, S - 1), radius=380, fill=PANEL)

    # Menu: the accent button the bunny just pushed off (squashed wide), then buttons still being dealt in
    bar(img, 1024, 1390, 1340, 200, ACCENT)
    bar(img, 1024, 1650, 1180, 190, INACTIVE)
    bar(img, 1024, 1880, 1060, 170, INACTIVE, 0.5)

    # Hop trail from the button up to the bunny's back foot
    d = ImageDraw.Draw(img)
    for i, (x, y) in enumerate(((400, 1250), (460, 1140), (540, 1045), (630, 975))):
        r = 26 + 9 * i
        d.ellipse((x - r, y - r, x + r, y + r), fill=(255, 255, 255, 80 + 45 * i))

    # Bunny mid-hop, nose up, stretched out
    bx, by = 1130, 830
    oval(img, bx + 170, by - 470, 120, 400, 28, WHITE)        # back ear
    oval(img, bx + 290, by - 480, 120, 420, 12, WHITE)        # front ear
    oval(img, bx + 290, by - 470, 50, 280, 12, ACCENT)        # inner ear
    oval(img, bx - 330, by + 120, 420, 140, 25, WHITE)        # back leg, kicked out
    oval(img, bx, by, 660, 360, 20, WHITE)                    # body
    oval(img, bx + 250, by + 120, 230, 100, -35, WHITE)       # front paws
    oval(img, bx + 310, by - 230, 320, 290, 10, WHITE)        # head
    oval(img, bx - 320, by - 30, 160, 160, 0, WHITE)          # tail
    oval(img, bx + 380, by - 260, 52, 60, 0, PANEL)           # eye
    oval(img, bx + 470, by - 190, 44, 36, 0, ACCENT)          # nose
    return img


if __name__ == "__main__":
    big = draw()
    for size, name in ((512, "icon-512.png"), (128, "icon-128.png")):
        big.resize((size, size), Image.LANCZOS).save(OUT / name)
    print("ok")
