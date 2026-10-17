#!/usr/bin/env python3
"""
Generate 1080x1920 high-res store screenshots for 036_curl-converter (CurlForge)
"""
import os
from PIL import Image, ImageDraw, ImageFont

def get_font(name, size, bold=False):
    win_font = f"C:/Windows/Fonts/{name}"
    if os.path.exists(win_font):
        return ImageFont.truetype(win_font, size)
    try:
        return ImageFont.truetype(name, size)
    except Exception:
        return ImageFont.load_default()

def create_phone_mockup(title, subtitle, badge_text, screen_content_func, width=1080, height=1920):
    img = Image.new("RGBA", (width, height), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)

    # Top gradient
    for y in range(height):
        ratio = y / float(height)
        r = int(15 + ratio * 6)
        g = int(23 + ratio * 8)
        b = int(42 + ratio * 16)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))

    font_badge = get_font("arialbd.ttf", 30)
    font_title = get_font("meiryo.ttc", 50, bold=True)
    font_sub = get_font("meiryo.ttc", 30)

    # Badge
    bbox = draw.textbbox((0, 0), badge_text, font=font_badge)
    bw = bbox[2] - bbox[0] + 48
    bx = (width - bw) // 2
    draw.rounded_rectangle([bx, 60, bx + bw, 114], radius=12, fill=(30, 41, 59, 255), outline=(6, 182, 212, 255), width=2)
    draw.text((bx + 24, 70), badge_text, fill=(6, 182, 212, 255), font=font_badge)

    # Title
    bbox_t = draw.textbbox((0, 0), title, font=font_title)
    draw.text(((width - (bbox_t[2] - bbox_t[0])) // 2, 136), title, fill=(255, 255, 255, 255), font=font_title)

    # Subtitle
    bbox_s = draw.textbbox((0, 0), subtitle, font=font_sub)
    draw.text(((width - (bbox_s[2] - bbox_s[0])) // 2, 210), subtitle, fill=(148, 163, 184, 255), font=font_sub)

    # Phone Frame
    phone_x = 90
    phone_y = 285
    phone_w = 900
    phone_h = 1590

    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=44, fill=(10, 15, 28, 255), outline=(51, 65, 85, 255), width=4)
    screen_x = phone_x + 18
    screen_y = phone_y + 18
    screen_w = phone_w - 36
    screen_h = phone_h - 36
    draw.rounded_rectangle([screen_x, screen_y, screen_x + screen_w, screen_y + screen_h], radius=34, fill=(13, 17, 23, 255))

    # Camera Notch
    draw.rounded_rectangle([screen_x + screen_w // 2 - 70, screen_y + 12, screen_x + screen_w // 2 + 70, screen_y + 40], radius=14, fill=(0, 0, 0, 255))

    screen_content_func(draw, screen_x, screen_y + 55, screen_w, screen_h - 55)
    return img

def draw_screen_1(draw, sx, sy, sw, sh):
    font_bold = get_font("arialbd.ttf", 32)
    font_code = get_font("consola.ttf", 22)
    font_sm = get_font("arialbd.ttf", 20)
    font_txt = get_font("meiryo.ttc", 20)

    # App header
    draw.text((sx + 30, sy + 20), "CurlForge", fill=(255, 255, 255), font=font_bold)
    draw.rounded_rectangle([sx + sw - 240, sy + 18, sx + sw - 30, sy + 62], radius=8, fill=(30, 41, 59), outline=(6, 182, 212), width=1)
    draw.text((sx + sw - 220, sy + 26), "cURL to Multi-Lang", fill=(6, 182, 212), font=font_sm)

    # Terminal cURL box
    cur_y = sy + 85
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 240], radius=14, fill=(22, 27, 34), outline=(48, 54, 61), width=2)
    # dots
    draw.ellipse([sx + 45, cur_y + 15, sx + 57, cur_y + 27], fill=(244, 63, 94))
    draw.ellipse([sx + 65, cur_y + 15, sx + 77, cur_y + 27], fill=(245, 158, 11))
    draw.ellipse([sx + 85, cur_y + 15, sx + 97, cur_y + 27], fill=(16, 185, 129))
    draw.text((sx + 120, cur_y + 13), "cURL Command Input", fill=(139, 148, 158), font=font_sm)

    # cURL lines
    curl_lines = [
        ("$ curl -X POST https://api.openai.com/v1/chat \\", (88, 166, 255)),
        ("  -H \"Content-Type: application/json\" \\", (210, 168, 255)),
        ("  -H \"Authorization: Bearer sk-api-key\" \\", (210, 168, 255)),
        ("  -d '{\"model\": \"gpt-4o-mini\", \"messages\": [", (165, 214, 255)),
        ("    {\"role\": \"user\", \"content\": \"Hello!\"}]}'", (165, 214, 255))
    ]
    snip_y = cur_y + 50
    for line, col in curl_lines:
        draw.text((sx + 45, snip_y), line, fill=col, font=font_code)
        snip_y += 35

    # Target language chips
    cur_y += 265
    draw.text((sx + 30, cur_y), "Target Language & Framework", fill=(203, 213, 225), font=font_sm)
    cur_y += 35
    langs = [
        ("Python requests", True),
        ("httpx async", False),
        ("JS Fetch", False),
        ("Node Axios", False),
        ("Go net/http", False)
    ]
    chip_x = sx + 30
    for lang, active in langs:
        bg = (6, 182, 212) if active else (30, 41, 59)
        tc = (15, 23, 42) if active else (226, 232, 240)
        draw.rounded_rectangle([chip_x, cur_y, chip_x + 145, cur_y + 45], radius=22, fill=bg, outline=(6, 182, 212) if active else (51, 65, 85), width=1)
        draw.text((chip_x + 16, cur_y + 12), lang, fill=tc, font=font_sm)
        chip_x += 155

    # Generated Code Card
    cur_y += 70
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 480], radius=14, fill=(22, 27, 34), outline=(6, 182, 212), width=2)
    draw.text((sx + 50, cur_y + 20), "Generated Python Code", fill=(248, 250, 252), font=font_bold)
    
    # Copy button
    draw.rounded_rectangle([sx + sw - 170, cur_y + 15, sx + sw - 50, cur_y + 60], radius=8, fill=(6, 182, 212))
    draw.text((sx + sw - 150, cur_y + 24), "Copy Code", fill=(15, 23, 42), font=font_sm)

    py_lines = [
        ("import requests", (255, 123, 114)),
        ("", (255, 255, 255)),
        ("url = \"https://api.openai.com/v1/chat\"", (121, 192, 255)),
        ("headers = {", (230, 237, 243)),
        ("    \"Content-Type\": \"application/json\",", (165, 214, 255)),
        ("    \"Authorization\": \"Bearer sk-api-key\"", (165, 214, 255)),
        ("}", (230, 237, 243)),
        ("payload = {", (230, 237, 243)),
        ("    \"model\": \"gpt-4o-mini\",", (165, 214, 255)),
        ("    \"messages\": [{\"role\": \"user\", \"content\": \"Hello!\"}]", (165, 214, 255)),
        ("}", (230, 237, 243)),
        ("res = requests.post(url, headers=headers, json=payload)", (210, 168, 255)),
        ("print(res.status_code, res.text)", (230, 237, 243))
    ]
    py_y = cur_y + 80
    for l, c in py_lines:
        if l:
            draw.text((sx + 50, py_y), l, fill=c, font=font_code)
        py_y += 30

def draw_screen_2(draw, sx, sy, sw, sh):
    font_bold = get_font("arialbd.ttf", 32)
    font_sm = get_font("arialbd.ttf", 22)
    font_code = get_font("consola.ttf", 22)

    draw.text((sx + 30, sy + 20), "Visual Request Builder", fill=(255, 255, 255), font=font_bold)
    draw.text((sx + 30, sy + 65), "Dynamically updates cURL command in real-time", fill=(148, 163, 184), font=font_sm)

    cur_y = sy + 115
    # HTTP Method selector
    draw.text((sx + 30, cur_y), "HTTP Method", fill=(203, 213, 225), font=font_sm)
    cur_y += 35
    methods = [("GET", False), ("POST", True), ("PUT", False), ("DELETE", False), ("PATCH", False)]
    mx = sx + 30
    for m, active in methods:
        bg = (6, 182, 212) if active else (30, 41, 59)
        tc = (15, 23, 42) if active else (226, 232, 240)
        draw.rounded_rectangle([mx, cur_y, mx + 110, cur_y + 45], radius=10, fill=bg)
        draw.text((mx + 25, cur_y + 12), m, fill=tc, font=font_sm)
        mx += 120

    # URL field
    cur_y += 70
    draw.text((sx + 30, cur_y), "Endpoint URL", fill=(203, 213, 225), font=font_sm)
    cur_y += 35
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 60], radius=10, fill=(30, 41, 59), outline=(51, 65, 85), width=1)
    draw.text((sx + 45, cur_y + 16), "https://api.example.com/v1/users", fill=(248, 250, 252), font=font_code)

    # Headers list
    cur_y += 85
    draw.text((sx + 30, cur_y), "Custom Headers (2)", fill=(203, 213, 225), font=font_sm)
    cur_y += 35
    headers = [
        ("Content-Type", "application/json"),
        ("Authorization", "Bearer eyJhbGciOiJIUz...")
    ]
    for k, v in headers:
        draw.rounded_rectangle([sx + 30, cur_y, sx + 340, cur_y + 50], radius=8, fill=(30, 41, 59), outline=(51, 65, 85), width=1)
        draw.text((sx + 45, cur_y + 14), k, fill=(56, 189, 248), font=font_code)
        draw.rounded_rectangle([sx + 360, cur_y, sx + sw - 30, cur_y + 50], radius=8, fill=(30, 41, 59), outline=(51, 65, 85), width=1)
        draw.text((sx + 375, cur_y + 14), v, fill=(248, 250, 252), font=font_code)
        cur_y += 65

    # Request Body
    cur_y += 20
    draw.text((sx + 30, cur_y), "JSON Body Payload", fill=(203, 213, 225), font=font_sm)
    cur_y += 35
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 160], radius=10, fill=(22, 27, 34), outline=(48, 54, 61), width=1)
    body_txt = [
        ("{\n  \"name\": \"Alice Developer\",", (165, 214, 255)),
        ("  \"email\": \"alice@example.com\",", (165, 214, 255)),
        ("  \"role\": \"lead-architect\"\n}", (165, 214, 255))
    ]
    by = cur_y + 20
    for bt, bc in body_txt:
        draw.text((sx + 45, by), bt, fill=bc, font=font_code)
        by += 32

    # Resulting live cURL
    cur_y += 190
    draw.text((sx + 30, cur_y), "Live Synced cURL Command", fill=(16, 185, 129), font=font_sm)
    cur_y += 35
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 150], radius=10, fill=(13, 17, 23), outline=(16, 185, 129), width=1)
    draw.text((sx + 45, cur_y + 20), "curl -X POST https://api.example.com/v1/users \\\n  -H \"Content-Type: application/json\" \\\n  -d '{\"name\":\"Alice Developer\",...}'", fill=(230, 237, 243), font=font_code)

def draw_screen_3(draw, sx, sy, sw, sh):
    font_bold = get_font("arialbd.ttf", 32)
    font_sm = get_font("arialbd.ttf", 22)
    font_code = get_font("consola.ttf", 20)
    font_txt = get_font("meiryo.ttc", 20)

    draw.text((sx + 30, sy + 20), "API Presets & Templates", fill=(255, 255, 255), font=font_bold)
    draw.text((sx + 30, sy + 65), "One-tap load battle-tested API requests", fill=(148, 163, 184), font=font_sm)

    presets = [
        ("POST", "OpenAI Chat Completions", "AI / LLM", "curl https://api.openai.com/v1/chat/completions \\\n  -H \"Authorization: Bearer sk-...\""),
        ("GET", "GitHub Get Authenticated User", "Developer Tools", "curl -X GET https://api.github.com/user \\\n  -H \"Authorization: Bearer ghp-...\""),
        ("POST", "Stripe Create PaymentIntent", "Payment", "curl https://api.stripe.com/v1/payment_intents \\\n  -u \"sk_test_...:\" -d \"amount=2000\""),
        ("POST", "Slack Incoming Webhook", "Messaging", "curl -X POST https://hooks.slack.com/services/... \\\n  -d '{\"text\": \"Build Succeeded!\"}'")
    ]

    cur_y = sy + 115
    for method, title, cat, code in presets:
        draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 195], radius=12, fill=(30, 41, 59), outline=(51, 65, 85), width=1)
        
        # Method badge
        m_col = (16, 185, 129) if method == "GET" else (37, 99, 235)
        draw.rounded_rectangle([sx + 45, cur_y + 16, sx + 115, cur_y + 50], radius=6, fill=(m_col[0], m_col[1], m_col[2], 50), outline=m_col, width=1)
        draw.text((sx + 55, cur_y + 22), method, fill=m_col, font=font_sm)

        draw.text((sx + 130, cur_y + 22), title, fill=(248, 250, 252), font=font_bold)
        
        # Category
        draw.rounded_rectangle([sx + sw - 160, cur_y + 16, sx + sw - 45, cur_y + 46], radius=4, fill=(15, 23, 42))
        draw.text((sx + sw - 145, cur_y + 22), cat, fill=(148, 163, 184), font=get_font("arial.ttf", 18))

        # Code box
        draw.rounded_rectangle([sx + 45, cur_y + 65, sx + sw - 45, cur_y + 140], radius=6, fill=(13, 17, 23), outline=(48, 54, 61), width=1)
        draw.text((sx + 55, cur_y + 75), code, fill=(203, 213, 225), font=font_code)

        # Action buttons
        draw.rounded_rectangle([sx + sw - 180, cur_y + 150, sx + sw - 45, cur_y + 185], radius=6, fill=(6, 182, 212))
        draw.text((sx + sw - 155, cur_y + 156), "Load Request", fill=(15, 23, 42), font=get_font("arialbd.ttf", 18))

        cur_y += 215

def draw_screen_4(draw, sx, sy, sw, sh):
    font_bold = get_font("arialbd.ttf", 32)
    font_sm = get_font("arialbd.ttf", 22)
    font_code = get_font("consola.ttf", 20)
    font_txt = get_font("meiryo.ttc", 20)

    draw.text((sx + 30, sy + 20), "cURL Flags & Options Glossary", fill=(255, 255, 255), font=font_bold)
    draw.text((sx + 30, sy + 65), "Comprehensive reference with practical examples", fill=(148, 163, 184), font=font_sm)

    # Search bar
    cur_y = sy + 115
    draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 55], radius=10, fill=(30, 41, 59), outline=(6, 182, 212), width=1)
    draw.text((sx + 50, cur_y + 16), "🔍 Search flags (e.g. -H, --data, -L, timeout)...", fill=(148, 163, 184), font=font_txt)

    flags = [
        ("-X", "--request", "HTTP Method", "Specify request method (GET, POST, PUT, DELETE)", "curl -X POST https://api.example.com"),
        ("-H", "--header", "Headers", "Pass custom HTTP headers such as Auth or Content-Type", "curl -H \"Authorization: Bearer token\" ..."),
        ("-d", "--data", "Request Body", "Sends specified POST data or JSON payload", "curl -d '{\"key\":\"val\"}' -H \"Content-Type:...\""),
        ("-L", "--location", "Navigation", "Automatically follow HTTP 301/302 redirects", "curl -L https://short.link/endpoint"),
        ("-u", "--user", "Auth", "Specify Basic authentication user:password", "curl -u admin:secret https://api.example.com")
    ]

    cur_y += 80
    for short_f, long_f, cat, desc, eg in flags:
        draw.rounded_rectangle([sx + 30, cur_y, sx + sw - 30, cur_y + 175], radius=12, fill=(30, 41, 59), outline=(51, 65, 85), width=1)
        
        # Flag pill
        draw.rounded_rectangle([sx + 45, cur_y + 14, sx + 105, cur_y + 46], radius=6, fill=(6, 182, 212, 40), outline=(6, 182, 212), width=1)
        draw.text((sx + 55, cur_y + 20), short_f, fill=(6, 182, 212), font=font_bold)

        draw.text((sx + 120, cur_y + 20), long_f, fill=(203, 213, 225), font=font_code)
        
        # Category
        draw.rounded_rectangle([sx + sw - 150, cur_y + 14, sx + sw - 45, cur_y + 44], radius=4, fill=(15, 23, 42))
        draw.text((sx + sw - 135, cur_y + 20), cat, fill=(148, 163, 184), font=get_font("arial.ttf", 18))

        draw.text((sx + 45, cur_y + 60), desc, fill=(248, 250, 252), font=font_txt)

        # Example
        draw.rounded_rectangle([sx + 45, cur_y + 98, sx + sw - 45, cur_y + 155], radius=6, fill=(13, 17, 23), outline=(48, 54, 61), width=1)
        draw.text((sx + 55, cur_y + 115), eg, fill=(88, 166, 255), font=font_code)

        cur_y += 195

def generate_all_screenshots():
    out_dir = "store_assets/play"
    os.makedirs(out_dir, exist_ok=True)
    art_dir = r"C:\Users\Kawamoto\.gemini\antigravity\brain\85f914b0-2e74-4dde-88b5-7aefe5547cc8"

    screens = [
        ("screenshot_1.png", "cURLを各種言語コードへ瞬時変換", "Python, JS, Go, Kotlin, PHP, Rust など10言語対応", "MULTI-LANGUAGE CODE GENERATOR", draw_screen_1),
        ("screenshot_2.png", "GUIで直感作成 ＆ cURL逆生成", "メソッド・ヘッダー・JSONボディを即時双方向同期", "REVERSE cURL REQUEST BUILDER", draw_screen_2),
        ("screenshot_3.png", "OpenAI / Stripe / GitHub プリセット", "主要Web APIの公式リクエストをワンタップで即ロード", "PRACTICAL API TEMPLATES", draw_screen_3),
        ("screenshot_4.png", "cURL コマンドオプション完全辞典", "-X, -H, -d, -L など30以上の主要フラグを網羅解説", "COMPLETE cURL FLAGS HANDBOOK", draw_screen_4)
    ]

    for fname, title, sub, badge, drawer in screens:
        img = create_phone_mockup(title, sub, badge, drawer)
        path = os.path.join(out_dir, fname)
        img.save(path, "PNG")
        print(f"Saved: {path}")

        if os.path.isdir(art_dir):
            art_path = os.path.join(art_dir, f"curlforge_{fname}")
            img.save(art_path, "PNG")
            print(f"Artifact copy saved: {art_path}")

if __name__ == "__main__":
    generate_all_screenshots()
