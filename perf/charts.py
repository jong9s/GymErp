"""
k6 결과(perf/results/*.json)로 개선 전/후 평균 응답시간 비교 차트(SVG, 라이트/다크)를 생성한다.

    python perf/charts.py

출력: docs/images/latency-light.svg, docs/images/latency-dark.svg
"""
import json
import math
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, 'perf', 'results')
OUT = os.path.join(ROOT, 'docs', 'images')

# (표시 이름, 개선 전 시나리오, 개선 후 시나리오)
ROWS = [
    ('회원 목록  GET /v1/member', 'member', 'member'),
    ('PT 로그 첫 페이지', 'ptlog_first', 'ptlog_first'),
    ('PT 로그 깊은 페이지 (offset)', 'ptlog_deep', 'ptlog_deep'),
    ('PT 로그 깊은 페이지 (커서)', 'ptlog_deep', 'ptlog_deep_cursor'),
    ('회원권 로그 첫 페이지', 'voucher_first', 'voucher_first'),
    ('회원권 로그 깊은 페이지', 'voucher_deep', 'voucher_deep'),
    ('대시보드 KPI  GET /v1/home', 'dashboard', 'dashboard'),
    ('회원 상세  GET /v1/member/{id}', 'member_detail', 'member_detail'),
]

THEMES = {
    'light': dict(surface='#fcfcfb', text='#0b0b0b', text2='#52514e', grid='#e6e5e0',
                  before='#eb6834', after='#2a78d6'),
    'dark': dict(surface='#1a1a19', text='#f0efec', text2='#c3c2b7', grid='#33332f',
                 before='#d95926', after='#3987e5'),
}

W, LEFT, RIGHT, TOP = 880, 250, 110, 78
BAR_H, BAR_GAP, ROW_GAP = 11, 2, 20
ROW_H = BAR_H * 2 + BAR_GAP + ROW_GAP
MIN_MS, MAX_MS = 0.1, 100_000  # 로그 축 범위


def load(scenario, label):
    with open(os.path.join(RESULTS, f'{scenario}-{label}.json'), encoding='utf-8') as f:
        return json.load(f)


def fmt(ms):
    if ms >= 1000:
        return f'{ms / 1000:.1f}s'
    if ms >= 10:
        return f'{ms:.0f}ms'
    return f'{ms:.1f}ms'


TICKS = [(0.1, '0.1ms'), (1, '1ms'), (10, '10ms'), (100, '100ms'), (1000, '1s'), (10000, '10s'), (100000, '100s')]


def x_of(ms):
    span = math.log10(MAX_MS) - math.log10(MIN_MS)
    return LEFT + (math.log10(max(ms, MIN_MS)) - math.log10(MIN_MS)) / span * (W - LEFT - RIGHT)


def bar(x0, y, x1, color, title):
    # 기준선(왼쪽)은 직각, 데이터 끝(오른쪽)만 4px 라운드
    r = min(4, (x1 - x0) / 2, BAR_H / 2)
    d = (f'M{x0:.1f},{y} H{x1 - r:.1f} Q{x1:.1f},{y} {x1:.1f},{y + r} '
         f'V{y + BAR_H - r} Q{x1:.1f},{y + BAR_H} {x1 - r:.1f},{y + BAR_H} H{x0:.1f} Z')
    return f'<path d="{d}" fill="{color}"><title>{title}</title></path>'


def render(theme):
    t = THEMES[theme]
    h = TOP + ROW_H * len(ROWS) + 40
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{h}" viewBox="0 0 {W} {h}" '
           f'font-family="Pretendard, \'Apple SD Gothic Neo\', \'Malgun Gothic\', system-ui, sans-serif">',
           f'<rect width="{W}" height="{h}" fill="{t["surface"]}"/>',
           f'<text x="24" y="30" font-size="17" font-weight="700" fill="{t["text"]}">'
           f'평균 응답시간 - 개선 전 vs 개선 후</text>',
           f'<text x="24" y="50" font-size="12" fill="{t["text2"]}">'
           f'k6 동시 사용자 20명 · 30초 · 로그 스케일 (짧을수록 좋음)</text>']

    # 범례
    lx = W - RIGHT - 170
    for i, (key, name) in enumerate((('before', '개선 전'), ('after', '개선 후'))):
        out.append(f'<rect x="{lx + i * 90}" y="22" width="12" height="12" rx="3" fill="{t[key]}"/>')
        out.append(f'<text x="{lx + i * 90 + 18}" y="32" font-size="12" fill="{t["text"]}">{name}</text>')

    # 그리드 + 축 눈금
    plot_bottom = TOP + ROW_H * len(ROWS) - ROW_GAP + 6
    for ms, tick in TICKS:
        x = x_of(ms)
        out.append(f'<line x1="{x:.1f}" y1="{TOP - 8}" x2="{x:.1f}" y2="{plot_bottom}" stroke="{t["grid"]}" stroke-width="1"/>')
        out.append(f'<text x="{x:.1f}" y="{plot_bottom + 18}" font-size="11" fill="{t["text2"]}" text-anchor="middle">{tick}</text>')

    for i, (name, b_sc, a_sc) in enumerate(ROWS):
        y = TOP + i * ROW_H
        before = load(b_sc, 'before')['latencyMs']['avg']
        after = load(a_sc, 'after')['latencyMs']['avg']
        out.append(f'<text x="{LEFT - 14}" y="{y + BAR_H + 5}" font-size="13" fill="{t["text"]}" text-anchor="end">{name}</text>')
        x0 = x_of(MIN_MS)
        for j, (ms, key, label) in enumerate(((before, 'before', '개선 전'), (after, 'after', '개선 후'))):
            by = y + j * (BAR_H + BAR_GAP)
            x1 = x_of(ms)
            out.append(bar(x0, by, x1, t[key], f'{name} · {label}: 평균 {fmt(ms)}'))
            weight = '700' if key == 'after' else '400'
            out.append(f'<text x="{x1 + 6:.1f}" y="{by + BAR_H - 1}" font-size="11" font-weight="{weight}" '
                       f'fill="{t["text"] if key == "after" else t["text2"]}">{fmt(ms)}</text>')
        speedup = before / after
        out.append(f'<text x="{W - 24}" y="{y + BAR_H + 5}" font-size="12" font-weight="700" fill="{t["text"]}" '
                   f'text-anchor="end">×{speedup:,.0f}</text>')

    out.append('</svg>')
    return '\n'.join(out)


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for theme in THEMES:
        path = os.path.join(OUT, f'latency-{theme}.svg')
        with open(path, 'w', encoding='utf-8') as f:
            f.write(render(theme))
        print('wrote', path)
