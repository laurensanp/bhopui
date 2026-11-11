"""Draws the BhopUI icon: a jumper chaining bunny hops along a slider that fills like a speed meter.
Run: python -I icon/make_icon.py"""
from pathlib import Path
from PIL import Image, ImageDraw

PANEL, INACTIVE, ACCENT, WHITE = "#201a0e", "#312c29", "#f1bea6", "#ffffff"
S = 2048  # drawn at 4x of 512, then downscaled for smooth edges
OUT = Path(__file__).parent
GROUND = 1600


def hop(d, x0, x1, height, dots, alpha, until=1.0):
    """Dotted jump arc from x0 to x1 on the ground, drawn up to fraction `until`."""
    for i in range(1, dots):
        t = i / dots
        if t > until:
            break
        x, y = x0 + (x1 - x0) * t, GROUND - 90 - height * 4 * t * (1 - t)
        r = 20 + 10 * alpha
        d.ellipse((x - r, y - r, x + r, y + r), fill=(255, 255, 255, int(255 * alpha)))


def limb(d, pts, w):
    d.line(pts, fill=WHITE, width=w)
    for x, y in pts:
        d.ellipse((x - w / 2, y - w / 2, x + w / 2, y + w / 2), fill=WHITE)


def jumper():
    """Side view, crouched mid-air (the bhop jump), facing right."""
    L = Image.new("RGBA", (900, 900), (0, 0, 0, 0))
    d = ImageDraw.Draw(L)
    limb(d, [(450, 330), (320, 440), (210, 370)], 80)          # arm swung back
    d.rounded_rectangle((380, 280, 520, 570), radius=45, fill=WHITE)  # torso
    limb(d, [(450, 540), (650, 470), (610, 690)], 115)         # legs tucked to the chest
    limb(d, [(490, 340), (640, 330)], 75)                      # arm forward
    d.rounded_rectangle((360, 70, 560, 260), radius=50, fill=WHITE)   # head
    return L.rotate(-10, resample=Image.BICUBIC)               # lean into the jump


def draw():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, S - 1, S - 1), radius=380, fill=PANEL)

    # Slider track as the ground; the accent fill is the speed gained so far
    d.rounded_rectangle((260, GROUND - 70, 1788, GROUND + 70), radius=42, fill=INACTIVE)
    d.rounded_rectangle((260, GROUND - 70, 1380, GROUND + 70), radius=42, fill=ACCENT)

    # Each hop longer and higher than the last
    hop(d, 300, 620, 220, 5, 0.35)
    hop(d, 620, 1050, 340, 7, 0.6)
    hop(d, 1050, 1720, 520, 12, 0.9, until=0.42)  # the jump in progress, up to the jumper's feet

    # Landing squashes where the hops touched the slider
    for x in (620, 1050):
        d.ellipse((x - 70, GROUND - 105, x + 70, GROUND - 75), fill=WHITE)

    # Speed lines behind the jumper
    for y, x0, a in ((560, 520, 110), (680, 400, 160), (800, 560, 90)):
        d.rounded_rectangle((x0, y - 18, x0 + 380, y + 18), radius=18, fill=(255, 255, 255, a))

    img.alpha_composite(jumper(), (790, 300))
    return img


if __name__ == "__main__":
    big = draw()
    for size, name in ((512, "icon-512.png"), (128, "icon-128.png")):
        big.resize((size, size), Image.LANCZOS).save(OUT / name)
    print("ok")
