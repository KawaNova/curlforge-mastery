#!/usr/bin/env python3
"""
Generate 1024x500 Feature Graphic for 036_curl-converter (CurlForge)
Refined with crisp vector-like graphics and robust fonts
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont

def create_feature_graphic():
    w, h = 1024, 500
    img = Image.new("RGBA", (w, h), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)

    for y in range(h):
        ratio = y / float(h)
        r = int(15 + ratio * 8)
        g = int(23 + ratio * 12)
        b = int(42 + ratio * 20)
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    # Background subtle tech mesh on the right
    cx_grid, cy_grid = 800, 250
    for r in [280, 200, 130, 70]:
        draw.ellipse([cx_grid - r, cy_grid - r, cx_grid + r, cy_grid + r], outline=(6, 182, 212, 25), width=1)
    for angle_deg in range(0, 360, 30):
        rad = math.radians(angle_deg)
        x2 = cx_grid + 280 * math.cos(rad)
        y2 = cy_grid + 280 * math.sin(rad)
        draw.line([(cx_grid, cy_grid), (x2, y2)], fill=(56, 189, 248, 20), width=1)

    # Fonts
    font_badge = None
    font_title = None
    font_sub = None
    font_tag = None
    font_code = None
    font_card = None
    try:
        font_badge = ImageFont.truetype("arialbd.ttf", 14)
        font_title = ImageFont.truetype("arialbd.ttf", 46)
        font_sub = ImageFont.truetype("arial.ttf", 20)
        font_tag = ImageFont.truetype("arialbd.ttf", 13)
        font_code = ImageFont.truetype("consola.ttf", 14)
        font_card = ImageFont.truetype("arialbd.ttf", 14)
    except Exception:
        font_badge = ImageFont.load_default()
        font_title = ImageFont.load_default()
        font_sub = ImageFont.load_default()
        font_tag = ImageFont.load_default()
        font_code = ImageFont.load_default()
        font_card = ImageFont.load_default()

    # Left content: Hero branding
    x_left = 60
    y_badge = 65

    # Top Pill Badge
    draw.rounded_rectangle([x_left, y_badge, x_left + 330, y_badge + 34], radius=8, fill=(30, 41, 59, 255), outline=(6, 182, 212, 255), width=1)
    # Mini bolt icon
    draw.polygon([(x_left + 16, y_badge + 18), (x_left + 22, y_badge + 10), (x_left + 20, y_badge + 17), (x_left + 26, y_badge + 17), (x_left + 18, y_badge + 26), (x_left + 20, y_badge + 19)], fill=(6, 182, 212, 255))
    draw.text((x_left + 34, y_badge + 8), "cURL TO MULTI-LANGUAGE ENGINE", fill=(6, 182, 212, 255), font=font_badge)

    # Title
    draw.text((x_left, y_badge + 48), "CurlForge", fill=(248, 250, 252, 255), font=font_title)

    # Subtitle
    draw.text((x_left, y_badge + 108), "cURL to Python, JS, Go, Kotlin & Rust", fill=(56, 189, 248, 255), font=font_sub)
    draw.text((x_left, y_badge + 138), "Instant API code generator with visual request builder", fill=(148, 163, 184, 255), font=font_sub)

    # Feature tags
    tags = [
        ("CODE", "10+ Target Languages & Frameworks", (6, 182, 212, 255)),
        ("BUILD", "Visual Reverse Builder & Syntax Sync", (16, 185, 129, 255)),
        ("DOCS", "30+ cURL Flags & Options Glossary", (245, 158, 11, 255)),
        ("LOCAL", "100% On-Device / Zero Tracking & Safe", (168, 85, 247, 255))
    ]
    cur_y = y_badge + 185
    for badge_txt, tag_txt, col in tags:
        draw.rounded_rectangle([x_left, cur_y, x_left + 350, cur_y + 32], radius=6, fill=(30, 41, 59, 200), outline=(51, 65, 85, 255), width=1)
        # Small colored badge
        draw.rounded_rectangle([x_left + 6, cur_y + 5, x_left + 60, cur_y + 27], radius=4, fill=(col[0], col[1], col[2], 40), outline=(col[0], col[1], col[2], 120), width=1)
        draw.text((x_left + 14, cur_y + 8), badge_txt, fill=col, font=font_tag)
        draw.text((x_left + 70, cur_y + 8), tag_txt, fill=(241, 245, 249, 255), font=font_tag)
        cur_y += 40

    # Right side: Terminal Window Card
    card_x = 520
    card_y = 65
    card_w = 445
    card_h = 370

    draw.rounded_rectangle([card_x, card_y, card_x + card_w, card_y + card_h], radius=14, fill=(13, 17, 23, 255), outline=(48, 54, 61, 255), width=2)

    # Terminal Header dots
    dot_y = card_y + 18
    draw.ellipse([card_x + 18, dot_y, card_x + 28, dot_y + 10], fill=(244, 63, 94, 255))
    draw.ellipse([card_x + 36, dot_y, card_x + 46, dot_y + 10], fill=(245, 158, 11, 255))
    draw.ellipse([card_x + 54, dot_y, card_x + 64, dot_y + 10], fill=(16, 185, 129, 255))
    draw.text((card_x + 85, dot_y - 2), "terminal ~ curl to python", fill=(139, 148, 158, 255), font=font_card)

    # cURL input block inside terminal
    code_box_y = card_y + 44
    draw.rounded_rectangle([card_x + 14, code_box_y, card_x + card_w - 14, code_box_y + 90], radius=8, fill=(22, 27, 34, 255), outline=(48, 54, 61, 255), width=1)
    curl_snippet = [
        ("$ curl -X POST https://api.openai.com/v1/chat \\", (88, 166, 255, 255)),
        ("  -H \"Authorization: Bearer sk-...\" \\", (210, 168, 255, 255)),
        ("  -d '{\"model\": \"gpt-4o-mini\"}'", (165, 214, 255, 255))
    ]
    snip_y = code_box_y + 10
    for line, col in curl_snippet:
        draw.text((card_x + 24, snip_y), line, fill=col, font=font_code)
        snip_y += 24

    # Conversion arrow indicator
    draw.line([(card_x + card_w // 2, code_box_y + 98), (card_x + card_w // 2, code_box_y + 118)], fill=(6, 182, 212, 255), width=2)
    draw.polygon([
        (card_x + card_w // 2 - 6, code_box_y + 118),
        (card_x + card_w // 2 + 6, code_box_y + 118),
        (card_x + card_w // 2, code_box_y + 128)
    ], fill=(6, 182, 212, 255))

    # Converted Python / JS code block inside terminal
    py_box_y = code_box_y + 135
    draw.rounded_rectangle([card_x + 14, py_box_y, card_x + card_w - 14, card_y + card_h - 16], radius=8, fill=(22, 27, 34, 255), outline=(6, 182, 212, 180), width=1)
    
    # Python code tab
    draw.rounded_rectangle([card_x + 24, py_box_y + 8, card_x + 155, py_box_y + 26], radius=4, fill=(56, 189, 248, 30), outline=(56, 189, 248, 150), width=1)
    draw.text((card_x + 32, py_box_y + 10), "Python (requests)", fill=(56, 189, 248, 255), font=font_tag)
    
    py_code = [
        ("import requests", (255, 123, 114, 255)),
        ("response = requests.post(", (230, 237, 243, 255)),
        ("    'https://api.openai.com/v1/chat',", (165, 214, 255, 255)),
        ("    headers={'Authorization': 'Bearer ...'},", (210, 168, 255, 255)),
        ("    json={'model': 'gpt-4o-mini'}", (121, 192, 255, 255)),
        (")", (230, 237, 243, 255))
    ]
    py_y = py_box_y + 36
    for line, col in py_code:
        draw.text((card_x + 24, py_y), line, fill=col, font=font_code)
        py_y += 20

    # Save outputs
    out_dir = "store_assets/play"
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, "feature_graphic.png")
    img.save(out_path, "PNG")
    print(f"Feature graphic saved to {out_path}")

    # Artifact directory copy
    art_dir = r"C:\Users\Kawamoto\.gemini\antigravity\brain\85f914b0-2e74-4dde-88b5-7aefe5547cc8"
    if os.path.isdir(art_dir):
        art_path = os.path.join(art_dir, "curlforge_feature_graphic.png")
        img.save(art_path, "PNG")
        print(f"Artifact copy saved to {art_path}")

if __name__ == "__main__":
    create_feature_graphic()
