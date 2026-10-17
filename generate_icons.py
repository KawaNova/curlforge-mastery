#!/usr/bin/env python3
"""
Generate high quality app icons for 036_curl-converter (CurlForge)
Brand theme: Midnight Slate (#0F172A) + Terminal Card + cURL Request Arrow + Multi-Language Badges + 'CurlForge'
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont

def create_curlforge_icon(size=512):
    img = Image.new("RGBA", (size, size), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)
    scale = size / 512.0

    cx, cy = size // 2, int(185 * scale)

    # 1. Subtle glowing radar grid / tech rings
    for r, width, alpha in [(int(145 * scale), 1, 35), (int(125 * scale), 2, 60)]:
        bbox = (cx - r, cy - r, cx + r, cy + r)
        draw.ellipse(bbox, outline=(56, 189, 248, alpha), width=max(1, int(width * scale)))

    # 2. Main Terminal Card
    card_w = int(224 * scale)
    card_h = int(175 * scale)
    card_x = cx - card_w // 2
    card_y = cy - int(90 * scale)
    card_r = int(16 * scale)

    # Card shadow / outer border
    draw.rounded_rectangle(
        [card_x - int(2 * scale), card_y - int(2 * scale), card_x + card_w + int(2 * scale), card_y + card_h + int(2 * scale)],
        radius=card_r + 2,
        fill=(30, 41, 59, 220),
        outline=(56, 189, 248, 140),
        width=max(1, int(2 * scale))
    )
    # Card surface
    draw.rounded_rectangle(
        [card_x, card_y, card_x + card_w, card_y + card_h],
        radius=card_r,
        fill=(20, 29, 47, 255)
    )

    # Top terminal bar
    bar_h = int(32 * scale)
    draw.rounded_rectangle(
        [card_x, card_y, card_x + card_w, card_y + bar_h],
        radius=card_r,
        fill=(30, 41, 59, 255)
    )
    draw.rectangle(
        [card_x, card_y + bar_h - int(8 * scale), card_x + card_w, card_y + bar_h],
        fill=(30, 41, 59, 255)
    )

    # Status LEDs
    dot_y = card_y + bar_h // 2
    for i, color in enumerate([(239, 68, 68), (245, 158, 11), (16, 185, 129)]):
        dx = card_x + int((16 + i * 14) * scale)
        r = int(4 * scale)
        draw.ellipse([dx - r, dot_y - r, dx + r, dot_y + r], fill=color)

    # Method badge on right: POST
    try:
        font_post = ImageFont.truetype("arialbd.ttf", int(11 * scale))
    except:
        font_post = ImageFont.load_default()

    method_txt = "POST"
    bbox_m = draw.textbbox((0, 0), method_txt, font=font_post)
    mw = bbox_m[2] - bbox_m[0] + int(12 * scale)
    mh = int(18 * scale)
    mx = card_x + card_w - mw - int(12 * scale)
    my = dot_y - mh // 2
    draw.rounded_rectangle([mx, my, mx + mw, my + mh], radius=int(4 * scale), fill=(16, 185, 129, 200))
    draw.text((mx + int(6 * scale), my + int(2 * scale)), method_txt, fill=(255, 255, 255, 255), font=font_post)

    # 3. Inside Terminal Card: Prompt & Target Code
    try:
        font_code = ImageFont.truetype("consola.ttf", int(12 * scale))
        font_pill = ImageFont.truetype("arialbd.ttf", int(11 * scale))
    except:
        font_code = ImageFont.load_default()
        font_pill = ImageFont.load_default()

    # Terminal command lines
    line1 = "$ curl -X POST https://api.io"
    line2 = "  -H 'Authorization: Bearer'"
    draw.text((card_x + int(14 * scale), card_y + int(42 * scale)), line1, fill=(56, 189, 248, 255), font=font_code)
    draw.text((card_x + int(14 * scale), card_y + int(66 * scale)), line2, fill=(148, 163, 184, 255), font=font_code)

    # Converter Down Arrow
    arrow_y = card_y + int(96 * scale)
    draw.line([(cx, arrow_y), (cx, arrow_y + int(14 * scale))], fill=(16, 185, 129, 255), width=max(2, int(3 * scale)))
    draw.polygon([
        (cx - int(6 * scale), arrow_y + int(10 * scale)),
        (cx + int(6 * scale), arrow_y + int(10 * scale)),
        (cx, arrow_y + int(18 * scale))
    ], fill=(16, 185, 129, 255))

    # Multi-language badge pills: [Python] [JS] [Go] [Kotlin]
    pills = [
        ("Py", (56, 189, 248)),
        ("JS", (245, 158, 11)),
        ("Go", (6, 182, 212)),
        ("Kt", (168, 85, 247)),
        ("PHP", (99, 102, 241)),
    ]

    p_y = card_y + int(128 * scale)
    p_w = int(36 * scale)
    p_h = int(22 * scale)
    total_w = len(pills) * p_w + (len(pills) - 1) * int(6 * scale)
    p_start_x = cx - total_w // 2

    for i, (label, col) in enumerate(pills):
        px = p_start_x + i * (p_w + int(6 * scale))
        draw.rounded_rectangle([px, p_y, px + p_w, p_y + p_h], radius=int(4 * scale), fill=col)
        bbox_l = draw.textbbox((0, 0), label, font=font_pill)
        lw = bbox_l[2] - bbox_l[0]
        draw.text((px + (p_w - lw) // 2, p_y + int(3 * scale)), label, fill=(15, 23, 42, 255), font=font_pill)

    # 4. Floating Lightning / Converter Badge on bottom-right
    badge_cx = card_x + card_w - int(22 * scale)
    badge_cy = card_y + card_h - int(12 * scale)
    bw = int(32 * scale)
    draw.ellipse([badge_cx - bw, badge_cy - bw, badge_cx + bw, badge_cy + bw], fill=(15, 23, 42, 255), outline=(245, 158, 11, 255), width=max(1, int(2 * scale)))
    draw.ellipse([badge_cx - bw + int(3 * scale), badge_cy - bw + int(3 * scale), badge_cx + bw - int(3 * scale), badge_cy + bw - int(3 * scale)], fill=(245, 158, 11, 240))

    # White lightning bolt in badge
    bolt_pts = [
        (badge_cx + int(2 * scale), badge_cy - int(16 * scale)),
        (badge_cx - int(10 * scale), badge_cy + int(2 * scale)),
        (badge_cx, badge_cy + int(2 * scale)),
        (badge_cx - int(2 * scale), badge_cy + int(16 * scale)),
        (badge_cx + int(10 * scale), badge_cy - int(2 * scale)),
        (badge_cx, badge_cy - int(2 * scale))
    ]
    draw.polygon(bolt_pts, fill=(255, 255, 255, 255))

    # 5. Text Area: "CurlForge" + "CURL & CODE GENERATOR"
    font_logo = None
    font_sub = None
    try:
        font_logo = ImageFont.truetype("arialbd.ttf", int(42 * scale))
        font_sub = ImageFont.truetype("arialbd.ttf", int(14 * scale))
    except:
        try:
            font_logo = ImageFont.truetype("DejaVuSans-Bold.ttf", int(42 * scale))
            font_sub = ImageFont.truetype("DejaVuSans.ttf", int(14 * scale))
        except:
            font_logo = ImageFont.load_default()
            font_sub = ImageFont.load_default()

    logo_text = "CurlForge"
    bbox_logo = draw.textbbox((0, 0), logo_text, font=font_logo)
    logo_w = bbox_logo[2] - bbox_logo[0]
    draw.text((cx - logo_w // 2, int(370 * scale)), logo_text, fill=(255, 255, 255, 255), font=font_logo)

    # Sub-text badge
    sub_text = "CURL & CODE GENERATOR"
    bbox_sub = draw.textbbox((0, 0), sub_text, font=font_sub)
    sub_w = bbox_sub[2] - bbox_sub[0]
    sub_box_w = sub_w + int(24 * scale)
    sub_box_h = int(24 * scale)
    sub_box_x = cx - sub_box_w // 2
    sub_box_y = int(426 * scale)

    draw.rounded_rectangle(
        [sub_box_x, sub_box_y, sub_box_x + sub_box_w, sub_box_y + sub_box_h],
        radius=int(6 * scale),
        fill=(30, 41, 59, 220),
        outline=(56, 189, 248, 120),
        width=max(1, int(1 * scale))
    )
    draw.text((cx - sub_w // 2, sub_box_y + int(4 * scale)), sub_text, fill=(56, 189, 248, 255), font=font_sub)

    return img

def create_round_icon(base_img):
    size = base_img.size[0]
    mask = Image.new("L", (size, size), 0)
    draw_mask = ImageDraw.Draw(mask)
    draw_mask.ellipse((0, 0, size, size), fill=255)

    round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    round_img.paste(base_img, (0, 0), mask=mask)
    return round_img

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    res_dir = os.path.join(base_dir, "app", "src", "main", "res")

    icon_512 = create_curlforge_icon(512)
    icon_512.save(os.path.join(base_dir, "app_icon_512.png"))

    artifact_dir = r"C:\Users\Kawamoto\.gemini\antigravity\brain\85f914b0-2e74-4dde-88b5-7aefe5547cc8"
    if os.path.exists(artifact_dir):
        icon_512.save(os.path.join(artifact_dir, "curlforge_icon_test.png"))
        print(f"Saved test artifact to {os.path.join(artifact_dir, 'curlforge_icon_test.png')}")

    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, size in densities.items():
        folder_path = os.path.join(res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)

        scaled_icon = create_curlforge_icon(size)
        scaled_icon.save(os.path.join(folder_path, "ic_launcher.png"))

        round_icon = create_round_icon(scaled_icon)
        round_icon.save(os.path.join(folder_path, "ic_launcher_round.png"))

        print(f"Generated {folder}: {size}x{size}")

    print("CurlForge icons successfully generated!")

if __name__ == "__main__":
    main()
