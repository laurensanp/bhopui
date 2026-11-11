"""Draws the BhopUI icon: a springy menu with a ball hopping over it. Run: python -I icon/make_icon.py"""
from pathlib import Path
from PIL import Image, ImageDraw

PANEL, INACTIVE, ACCENT, WHITE = "#201a0e", "#312c29", "#f1bea6", "#ffffff"
S = 2048  # drawn at 4x of 512, then downscaled for smooth edges
OUT = Path(__file__).parent


def pill(d, cx, cy, w, h, fill):
    d.rounded_rectangle((cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2), radius=h * 0.3, fill=fill)


def draw():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, S - 1, S - 1), radius=380, fill=PANEL)

    # Menu buttons being dealt in: settled, hovered (104%+), still arriving (lower, faded)
    pill(d, 1024, 1060, 1180, 230, INACTIVE)
    pill(d, 1024, 1380, 1290, 260, ACCENT)
    fade = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    pill(ImageDraw.Draw(fade), 1024, 1740, 1080, 210, INACTIVE)
    fade.putalpha(fade.getchannel("A").point(lambda a: a * 0.55))
    img.alpha_composite(fade)

    # Hop: dotted parabola from the left edge up to the ball at its peak
    x0, x1, peak, base = 430, 1024, 430, 900
    for i in range(5):
        t = i / 6.5  # stop short of the ball
        x = x0 + (x1 - x0) * t
        y = base - (base - peak) * (1 - (1 - t) ** 2)
        r = 34 + 22 * t
        d.ellipse((x - r, y - r, x + r, y + r), fill=(255, 255, 255, int(70 + 120 * t)))
    d.ellipse((1024 - 135, peak - 135, 1024 + 135, peak + 135), fill=WHITE)
    return img


if __name__ == "__main__":
    big = draw()
    for size, name in ((512, "icon-512.png"), (128, "icon-128.png")):
        big.resize((size, size), Image.LANCZOS).save(OUT / name)
    print("ok")
