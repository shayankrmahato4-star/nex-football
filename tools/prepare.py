import re, base64, io, os
from PIL import Image, ImageDraw
from collections import deque

src = open('index.html', encoding='utf-8').read()

# 1) add the web-app tags + service worker registration (once)
if 'manifest.webmanifest' not in src:
    head = ('<link rel="manifest" href="manifest.webmanifest">\n'
            '<meta name="theme-color" content="#030220">\n'
            '<meta name="mobile-web-app-capable" content="yes">\n'
            '<meta name="apple-mobile-web-app-capable" content="yes">\n'
            '<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">\n'
            '<meta name="apple-mobile-web-app-title" content="NEX FOOTBALL">\n'
            '<link rel="apple-touch-icon" href="icons/apple-touch-icon.png">\n'
            '<link rel="icon" type="image/png" sizes="192x192" href="icons/icon-192.png">\n')
    i = src.lower().find('<head>')
    src = src[:src.find('>', i) + 1] + "\n" + head + src[src.find('>', i) + 1:]
    sw = ("<script>\nif('serviceWorker' in navigator){window.addEventListener('load',"
          "function(){navigator.serviceWorker.register('sw.js').catch(function(){});});}\n</script>\n")
    j = src.rfind('</body>')
    src = src[:j] + sw + src[j:]
    open('index.html', 'w', encoding='utf-8').write(src)
    print('web-app tags added')

# 2) app icons from the logo already inside the app
m = re.search(r"APP_ICON_URL='data:image/webp;base64,([A-Za-z0-9+/=]+)'", src)
if m:
    im = Image.open(io.BytesIO(base64.b64decode(m.group(1)))).convert('RGBA')
    w, h = im.size
    px = im.load()
    TH = 95

    def is_bg(p):
        return max(p[0], p[1], p[2]) < TH

    seen = [[False] * w for _ in range(h)]
    dq = deque()
    for x in range(w):
        for y in (0, h - 1):
            if is_bg(px[x, y]) and not seen[y][x]:
                seen[y][x] = True
                dq.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            if is_bg(px[x, y]) and not seen[y][x]:
                seen[y][x] = True
                dq.append((x, y))
    while dq:
        x, y = dq.popleft()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < w and 0 <= ny < h and not seen[ny][nx] and is_bg(px[nx, ny]):
                seen[ny][nx] = True
                dq.append((nx, ny))
    logo = im.copy()
    op = logo.load()
    for y in range(h):
        for x in range(w):
            if seen[y][x]:
                r, g, b, a = op[x, y]
                op[x, y] = (r, g, b, 0)

    os.makedirs('icons', exist_ok=True)

    def icon(size, pad, path, rounded=True):
        can = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        dr = ImageDraw.Draw(can)
        if rounded:
            dr.rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22), fill=(8, 12, 45, 255))
        else:
            dr.rectangle([0, 0, size - 1, size - 1], fill=(8, 12, 45, 255))
        inner = int(size * pad)
        can.alpha_composite(logo.resize((inner, inner), Image.LANCZOS), ((size - inner) // 2, (size - inner) // 2))
        can.save(path)

    icon(192, 0.78, 'icons/icon-192.png')
    icon(512, 0.78, 'icons/icon-512.png')
    icon(512, 0.62, 'icons/icon-maskable-512.png', rounded=False)
    icon(180, 0.78, 'icons/apple-touch-icon.png')
    print('icons built')
else:
    print('no logo found in app')
