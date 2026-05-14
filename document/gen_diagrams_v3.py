# -*- coding: utf-8 -*-
"""
Diagrams V3 — Large fonts, generous spacing, zero overlaps.
Figure sizes much larger; Word will scale them down to ~5.8in wide.
"""
import sys, os
sys.stdout.reconfigure(encoding='utf-8')
os.chdir(os.path.dirname(os.path.abspath(__file__)))

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
import numpy as np

plt.rcParams['font.family'] = 'SimHei'
plt.rcParams['axes.unicode_minus'] = False

OUT = "screenshots/diagrams"
os.makedirs(OUT, exist_ok=True)
DPI = 200

# ============================================================
# DIAGRAM 1: Flow Chart — Simple, clean, big fonts
# ============================================================
print("1/4 Flow Chart...")
fig, ax = plt.subplots(figsize=(14, 7))
ax.set_xlim(0, 14); ax.set_ylim(0, 7)
ax.axis('off')

def draw_flow_box(ax, x, y, w, h, text, color='#4472C4', fs=14):
    box = FancyBboxPatch((x-w/2, y-h/2), w, h,
        boxstyle="round,pad=0.15", facecolor=color,
        edgecolor='#2F5496', linewidth=1.5, alpha=0.92)
    ax.add_patch(box)
    lines = text.split('\n')
    for i, line in enumerate(lines):
        ax.text(x, y + (len(lines)-1)*0.14 - i*0.22, line, ha='center', va='center',
                fontsize=fs, color='white', fontweight='bold')

ax.text(7, 6.6, '系统总体处理流程图', ha='center', fontsize=26, fontweight='bold', color='#2F5496')

# Row 1
draw_flow_box(ax, 3, 5.2, 3.0, 1.3, '用户注册/登录\n(赠送100时间币)', '#4472C4', 16)
draw_flow_box(ax, 11, 5.2, 3.0, 1.3, '管理员登录', '#548235', 16)

# Arrows down
ax.annotate('', xy=(3, 4.2), xytext=(3, 4.55),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))
ax.annotate('', xy=(11, 4.2), xytext=(11, 4.55),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))

# Row 2
draw_flow_box(ax, 3, 3.4, 3.0, 1.3, '发布技能 / 悬赏', '#4472C4', 16)
draw_flow_box(ax, 11, 3.4, 3.0, 1.3, '技能审核\n(通过 / 拒绝)', '#548235', 16)

# Cross arrows with labels
ax.annotate('', xy=(9.2, 4.5), xytext=(4.8, 4.5),
            arrowprops=dict(arrowstyle='<->', color='#999', lw=1.5, ls='dashed'))
ax.text(7, 4.8, '审核通过后上架', ha='center', fontsize=12, color='#888')

ax.annotate('', xy=(9.2, 2.9), xytext=(4.8, 2.9),
            arrowprops=dict(arrowstyle='<->', color='#999', lw=1.5, ls='dashed'))
ax.text(7, 3.2, '申诉介入', ha='center', fontsize=12, color='#888')

# Arrow down
ax.annotate('', xy=(3, 2.4), xytext=(3, 2.75),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))

# Row 3
draw_flow_box(ax, 3, 1.6, 3.0, 1.3, '浏览 / 搜索 → 下单\n(冻结时间币)', '#ED7D31', 16)

# Arrow right
ax.annotate('', xy=(7.5, 1.6), xytext=(4.5, 1.6),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))

# Row 4
draw_flow_box(ax, 11, 1.0, 4.0, 1.3, '服务完成 → 双方确认\n解冻转账 → 双盲评价', '#BF8F00', 16)

plt.tight_layout(pad=0.5)
fig.savefig(f'{OUT}/flow_chart.png', dpi=DPI, bbox_inches='tight', facecolor='white')
plt.close()
print("  ✓ flow_chart.png")

# ============================================================
# DIAGRAM 2: Architecture — MUCH taller, wide spacing, no overlaps
# ============================================================
print("2/4 Architecture...")
fig, ax = plt.subplots(figsize=(20, 12))
ax.set_xlim(0, 20); ax.set_ylim(0, 12)
ax.axis('off')

ax.text(10, 11.6, '系统分层架构图', ha='center', fontsize=26, fontweight='bold', color='#2F5496')

# Layer positions — tall gaps
layers = [
    (10.0, '表现层（Presentation）'),
    (8.0, '控制层（Controller）'),
    (5.5, '业务逻辑层（Service）'),
    (3.0, '数据访问层（Data Access）'),
]
layer_h = 1.2

for y, title in layers:
    rect = FancyBboxPatch((0.5, y-layer_h/2), 19.0, layer_h,
        boxstyle="round,pad=0.15", facecolor='#E8F0FA',
        edgecolor='#B0C4DE', linewidth=1.2, alpha=0.6)
    ax.add_patch(rect)
    ax.text(0.8, y+layer_h/2-0.25, title, ha='left', va='center',
            fontsize=16, fontweight='bold', color='#2F5496')

# ── Layer 1: Presentation (y=10.0) ──
y = 10.0
box_w, box_h = 5.2, 0.8
for i, (name, color) in enumerate([
    ('用户端 SPA\nVue 3 + Element Plus（端口 5173）', '#4472C4'),
    ('管理后台 SPA\nVue 3 + Element Plus（端口 5174）', '#548235')
]):
    x = 5.5 + i * 9.0
    box = FancyBboxPatch((x-box_w/2, y-box_h/2), box_w, box_h,
        boxstyle="round,pad=0.1", facecolor=color,
        edgecolor='#2F5496', linewidth=1.5, alpha=0.9)
    ax.add_patch(box)
    for li, line in enumerate(name.split('\n')):
        ax.text(x, y + 0.08 - li*0.18, line, ha='center', va='center',
                fontsize=13, color='white', fontweight='bold')

# Arrow P→C
ax.annotate('', xy=(10, 8.9), xytext=(10, 9.3),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))
ax.text(11.5, 9.1, 'HTTP / JSON  (JWT Auth)', ha='left', fontsize=12, color='#555')

# ── Layer 2: Controller (y=8.0) ──
y = 8.0
box = FancyBboxPatch((1.5, y-box_h/2+0.05), 17.0, 0.7,
    boxstyle="round,pad=0.1", facecolor='#4472C4',
    edgecolor='#2F5496', linewidth=1.5, alpha=0.9)
ax.add_patch(box)
ax.text(10, y+0.05, 'skill-gateway 聚合入口（端口 8080）：@ComponentScan + @MapperScan + CORS + 拦截器',
        ha='center', fontsize=14, color='white', fontweight='bold')

# Arrow C→B
ax.annotate('', xy=(10, 6.4), xytext=(10, 7.3),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))

# ── Layer 3: Business Logic (y=5.5) ──
y = 5.5
modules = [
    ('skill-\ncommon', '#8B5CF6'),
    ('skill-\nuser', '#4472C4'),
    ('skill-\nskill', '#ED7D31'),
    ('skill-\norder', '#548235'),
    ('skill-\nwallet', '#BF8F00'),
    ('skill-\nadmin', '#C00000'),
]
# Spread 6 modules evenly over [3, 17] → 2.8in spacing
m_w, m_h = 2.0, 0.9
m_xs = [3.5, 6.3, 9.1, 11.9, 14.7, 17.5]
for i, (name, color) in enumerate(modules):
    x = m_xs[i]
    box = FancyBboxPatch((x-m_w/2, y-m_h/2), m_w, m_h,
        boxstyle="round,pad=0.08", facecolor=color,
        edgecolor='#333', linewidth=1.2, alpha=0.92)
    ax.add_patch(box)
    lines = name.split('\n')
    for li, line in enumerate(lines):
        ax.text(x, y + 0.05 - li*0.16, line, ha='center', va='center',
                fontsize=12, color='white', fontweight='bold')

# Annotation note — placed at top-right, outside any layer
ax.text(18.0, 11.0, '模块间通过 Maven 依赖\n直接调用（非 RPC）', ha='center', fontsize=11, color='#777',
        bbox=dict(boxstyle='round', facecolor='#FFF8E1', edgecolor='#E0C090', pad=0.6))

# Arrow B→D
ax.annotate('', xy=(10, 3.9), xytext=(10, 4.9),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))

# ── Layer 4: Data Access (y=3.0) ──
y = 3.0
for i, (name, color) in enumerate([
    ('MyBatis-Plus Mapper\n数据访问接口', '#4472C4'),
    ('HikariCP 连接池\n(max=20, minIdle=5)', '#548235'),
]):
    x = 5.5 + i * 9.0
    box = FancyBboxPatch((x-box_w/2, y-box_h/2+0.05), box_w, 0.65,
        boxstyle="round,pad=0.08", facecolor=color,
        edgecolor='#2F5496', linewidth=1.2, alpha=0.9)
    ax.add_patch(box)
    for li, line in enumerate(name.split('\n')):
        ax.text(x, y+0.05 - li*0.15, line, ha='center', va='center',
                fontsize=12, color='white', fontweight='bold')

# Arrow D→DB
ax.annotate('', xy=(10, 1.9), xytext=(10, 2.5),
            arrowprops=dict(arrowstyle='->', color='#555', lw=2.5))
ax.text(11.5, 2.2, 'JDBC', ha='left', fontsize=12, color='#555')

# ── Database ──
y = 1.2
box = FancyBboxPatch((4, y-0.3), 12, 0.6,
    boxstyle="round,pad=0.1", facecolor='#C00000',
    edgecolor='#8B0000', linewidth=2, alpha=0.92)
ax.add_patch(box)
ax.text(10, y, 'MySQL 8.0 — prometheus_skill_bank（15 张表，InnoDB，utf8mb4）',
        ha='center', fontsize=15, color='white', fontweight='bold')

plt.tight_layout(pad=0.5)
fig.savefig(f'{OUT}/architecture.png', dpi=DPI, bbox_inches='tight', facecolor='white')
plt.close()
print("  ✓ architecture.png")

# ============================================================
# DIAGRAM 3: Class Diagram — huge figure, big fonts
# ============================================================
print("3/4 Class Diagram...")
fig, ax = plt.subplots(figsize=(22, 15))
ax.set_xlim(0, 22); ax.set_ylim(0, 15)
ax.axis('off')

ax.text(11, 14.6, '系统概要设计类图', ha='center', fontsize=26, fontweight='bold', color='#2F5496')

# Classes with positions and key attributes
classes = {
    'User':       {'pos': (4.5, 12.0), 'attrs': ['id: BIGINT (PK)', 'username, password', 'role, status, balance', 'email, phone, avatar', 'bio, create_time'], 'color': '#4472C4'},
    'Skill':      {'pos': (9.5, 13.0), 'attrs': ['id: BIGINT (PK)', 'user_id (FK→User)', 'category_id (FK)', 'title, price, status', 'description, cover_image'], 'color': '#ED7D31'},
    'SkillCat':   {'pos': (9.5, 10.0), 'attrs': ['id: BIGINT (PK)', 'name', 'sort_order'], 'color': '#BF8F00'},
    'Bounty':     {'pos': (16.0, 13.0), 'attrs': ['id: BIGINT (PK)', 'user_id (FK→User)', 'title, reward, status', 'applicant_id (FK)'], 'color': '#548235'},
    'BountyApp':  {'pos': (16.0, 10.0), 'attrs': ['id: BIGINT (PK)', 'bounty_id (FK)', 'applicant_id (FK)', 'status'], 'color': '#A5C863'},
    'SkillOrder': {'pos': (9.5, 6.5), 'attrs': ['id (PK), order_no (UK)', 'buyer_id, seller_id', 'skill_id, amount', 'status (1~5)', 'buyer_confirm, seller_confirm'], 'color': '#C00000'},
    'Review':     {'pos': (3.0, 5.0), 'attrs': ['id: BIGINT (PK)', 'order_id (FK)', 'reviewer_id, target_id', 'score (1~5), comment'], 'color': '#8B5CF6'},
    'TimeTrans':  {'pos': (3.0, 2.0), 'attrs': ['id: BIGINT (PK)', 'user_id, order_id', 'type, amount', 'balance_after'], 'color': '#4472C4'},
    'ChatMsg':    {'pos': (14.5, 5.0), 'attrs': ['id: BIGINT (PK)', 'order_id (FK)', 'sender_id (FK)', 'content, msg_type'], 'color': '#548235'},
    'Notif':      {'pos': (14.5, 2.0), 'attrs': ['id: BIGINT (PK)', 'user_id (FK)', 'type, title', 'is_read'], 'color': '#8B5CF6'},
    'UserFollow': {'pos': (19.5, 12.0), 'attrs': ['id: BIGINT (PK)', 'follower_id (FK)', 'following_id (FK)'], 'color': '#BF8F00'},
    'PvtMsg':     {'pos': (19.5, 9.0), 'attrs': ['id: BIGINT (PK)', 'sender_id (FK)', 'receiver_id (FK)', 'content, is_read'], 'color': '#548235'},
    'Appeal':     {'pos': (19.5, 5.0), 'attrs': ['id: BIGINT (PK)', 'order_id (FK)', 'user_id (FK)', 'reason, status'], 'color': '#C00000'},
    'Announce':   {'pos': (19.5, 2.0), 'attrs': ['id: BIGINT (PK)', 'title, content', 'publisher_id (FK)'], 'color': '#BF8F00'},
    'UserTag':    {'pos': (4.5, 8.5), 'attrs': ['id: BIGINT (PK)', 'user_id (FK)', 'tag_name, score'], 'color': '#4472C4'},
}

# Draw class boxes
for name, info in classes.items():
    x, y = info['pos']
    attrs = info['attrs']
    w, h = 3.4, 0.4 + len(attrs) * 0.28

    # Box
    box = FancyBboxPatch((x-w/2, y-h/2), w, h,
        boxstyle="round,pad=0.1", facecolor='white',
        edgecolor=info['color'], linewidth=2.5)
    ax.add_patch(box)

    # Header
    hdr_h = 0.55
    hdr = FancyBboxPatch((x-w/2, y+h/2-hdr_h), w, hdr_h,
        boxstyle="round,pad=0.05", facecolor=info['color'],
        edgecolor=info['color'], linewidth=1)
    ax.add_patch(hdr)
    ax.text(x, y+h/2-hdr_h/2, f'<<entity>>  {name}', ha='center', va='center',
            fontsize=14, color='white', fontweight='bold')

    # Attributes
    for i, attr in enumerate(attrs):
        ax.text(x-w/2 + 0.25, y+h/2 - hdr_h - 0.2 - i*0.28, attr,
                ha='left', va='center', fontsize=11, color='#333')

# Key relationships
rels = [
    ('User', 'Skill', '1', '*'),
    ('User', 'Bounty', '1', '*'),
    ('User', 'SkillOrder', '1', '*'),
    ('Skill', 'SkillCat', '*', '1'),
    ('Skill', 'SkillOrder', '1', '*'),
    ('SkillOrder', 'Review', '1', '2'),
    ('SkillOrder', 'ChatMsg', '1', '*'),
    ('SkillOrder', 'TimeTrans', '1', '*'),
    ('Bounty', 'BountyApp', '1', '*'),
    ('User', 'UserFollow', '1', '*'),
    ('User', 'PvtMsg', '1', '*'),
    ('User', 'Notif', '1', '*'),
    ('SkillOrder', 'Appeal', '1', '*'),
    ('User', 'UserTag', '1', '*'),
    ('User', 'Announce', '1', '*'),
]

for src, dst, c1, c2 in rels:
    if src in classes and dst in classes:
        x1, y1 = classes[src]['pos']
        x2, y2 = classes[dst]['pos']
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
            arrowprops=dict(arrowstyle='->', color='#CCCCCC', lw=1.5,
            connectionstyle='arc3,rad=0.08'))
        mx, my = (x1+x2)/2 + 0.15, (y1+y2)/2 + 0.15
        ax.text(mx, my, f'{c1}..{c2}', ha='center', va='center',
                fontsize=10, color='#AAA')

plt.tight_layout(pad=0.5)
fig.savefig(f'{OUT}/class_diagram.png', dpi=DPI, bbox_inches='tight', facecolor='white')
plt.close()
print("  ✓ class_diagram.png")

# ============================================================
# DIAGRAM 4: ER Diagram — very large figure, generous spacing
# ============================================================
print("4/4 ER Diagram...")
fig, ax = plt.subplots(figsize=(24, 16))
ax.set_xlim(0, 24); ax.set_ylim(0, 16)
ax.axis('off')

ax.text(12, 15.6, '系统数据库表关系图（ER 图）', ha='center',
        fontsize=26, fontweight='bold', color='#2F5496')

# Tables — well spaced across the canvas
tables = {
    'user':               {'pos': (12.0, 13.5), 'color': '#4472C4'},
    'skill_category':     {'pos': (2.5, 14.0), 'color': '#BF8F00'},
    'skill':              {'pos': (5.5, 12.0), 'color': '#ED7D31'},
    'bounty':             {'pos': (18.5, 12.5), 'color': '#548235'},
    'bounty_application': {'pos': (21.5, 10.0), 'color': '#A5C863'},
    'skill_order':        {'pos': (10.0, 8.5), 'color': '#C00000'},
    'time_transaction':   {'pos': (3.0, 7.5), 'color': '#4472C4'},
    'review':             {'pos': (5.5, 5.0), 'color': '#8B5CF6'},
    'appeal':             {'pos': (12.0, 5.0), 'color': '#C00000'},
    'chat_message':       {'pos': (18.0, 7.5), 'color': '#548235'},
    'notification':       {'pos': (3.5, 2.5), 'color': '#8B5CF6'},
    'announcement':       {'pos': (8.0, 2.5), 'color': '#BF8F00'},
    'user_follow':        {'pos': (12.5, 2.5), 'color': '#BF8F00'},
    'user_skill_tag':     {'pos': (17.0, 2.5), 'color': '#4472C4'},
    'private_message':    {'pos': (21.5, 2.5), 'color': '#548235'},
}

labels = {
    'user':               'user  用户表\nPK: id',
    'skill_category':     'skill_category  技能分类表\nPK: id (自增)',
    'skill':              'skill  技能货架表\nPK: id  |  FK: user_id, category_id',
    'bounty':             'bounty  需求悬赏表\nPK: id  |  FK: user_id, applicant_id',
    'bounty_application': 'bounty_application  悬赏申请表\nPK: id  |  FK: bounty_id, applicant_id',
    'skill_order':        'skill_order  订单表\nPK: id, UK: order_no  |  FK: buyer_id, seller_id, skill_id',
    'time_transaction':   'time_transaction  时间币流水表\nPK: id  |  FK: user_id, order_id',
    'review':             'review  评价表（双盲）\nPK: id  |  FK: order_id, reviewer_id, target_id',
    'appeal':             'appeal  申诉表\nPK: id  |  FK: order_id, user_id',
    'chat_message':       'chat_message  订单聊天消息表\nPK: id  |  FK: order_id, sender_id',
    'notification':       'notification  通知表\nPK: id  |  FK: user_id',
    'announcement':       'announcement  公告表\nPK: id (自增)  |  FK: publisher_id',
    'user_follow':        'user_follow  用户关注表\nPK: id  |  FK: follower_id, following_id',
    'user_skill_tag':     'user_skill_tag  用户技能标签表\nPK: id  |  FK: user_id',
    'private_message':    'private_message  私信消息表\nPK: id  |  FK: sender_id, receiver_id',
}

for name, info in tables.items():
    x, y = info['pos']
    label = labels[name]
    lines = label.split('\n')
    w, h = 3.6, 0.4 * len(lines) + 0.5

    # Shadow
    shadow = FancyBboxPatch((x-w/2+0.08, y-h/2-0.08), w, h,
        boxstyle="round,pad=0.12", facecolor='#E8E8E8',
        edgecolor='none', alpha=0.35)
    ax.add_patch(shadow)

    # Box
    box = FancyBboxPatch((x-w/2, y-h/2), w, h,
        boxstyle="round,pad=0.12", facecolor='white',
        edgecolor=info['color'], linewidth=2.5)
    ax.add_patch(box)

    # Header
    hdr_h = 0.6
    hdr = FancyBboxPatch((x-w/2, y+h/2-hdr_h), w, hdr_h,
        boxstyle="round,pad=0.05", facecolor=info['color'],
        edgecolor=info['color'], linewidth=1.5)
    ax.add_patch(hdr)

    # Table name in header
    ax.text(x, y+h/2-hdr_h/2, lines[0], ha='center', va='center',
            fontsize=14, color='white', fontweight='bold')

    # Key info below header
    for i, line in enumerate(lines[1:]):
        fs = 12 if i == 0 else 11
        c = '#333' if i == 0 else '#555'
        ax.text(x, y+h/2 - hdr_h - 0.25 - i*0.28, line,
                ha='center', va='center', fontsize=fs, color=c)

# Relationships
relationships = [
    ('user', 'skill', '1 : N'),
    ('user', 'bounty', '1 : N'),
    ('skill_category', 'skill', '1 : N'),
    ('user', 'skill_order', '1 : N'),
    ('skill', 'skill_order', '1 : N'),
    ('skill_order', 'review', '1 : 2'),
    ('skill_order', 'chat_message', '1 : N'),
    ('skill_order', 'time_transaction', '1 : N'),
    ('skill_order', 'appeal', '1 : N'),
    ('user', 'time_transaction', '1 : N'),
    ('user', 'review', '1 : N'),
    ('user', 'notification', '1 : N'),
    ('user', 'appeal', '1 : N'),
    ('user', 'user_follow', '1 : N'),
    ('user', 'private_message', '1 : N'),
    ('user', 'user_skill_tag', '1 : 4'),
    ('user', 'announcement', '1 : N'),
    ('bounty', 'bounty_application', '1 : N'),
    ('user', 'bounty_application', '1 : N'),
]

for src, dst, card in relationships:
    if src in tables and dst in tables:
        x1, y1 = tables[src]['pos']
        x2, y2 = tables[dst]['pos']
        ax.plot([x1, x2], [y1, y2], '-', color='#DDDDDD', lw=1.2, alpha=0.6, zorder=0)
        mx, my = (x1+x2)/2 + 0.15, (y1+y2)/2 + 0.15
        ax.text(mx, my, card, ha='center', va='center', fontsize=10, color='#BBB',
                bbox=dict(facecolor='white', edgecolor='none', pad=0.3))

# Legend
lx, ly = 0.5, 15.0
legend_box = FancyBboxPatch((lx, ly-0.9), 7.5, 1.2,
    boxstyle="round,pad=0.12", facecolor='white', edgecolor='#CCC', linewidth=1)
ax.add_patch(legend_box)
ax.text(lx+0.3, ly, '图例', fontsize=14, fontweight='bold', color='#333')
ax.plot([lx+0.3, lx+1.2], [ly-0.35, ly-0.35], '-', color='#DDDDDD', lw=1.2)
ax.text(lx+1.5, ly-0.35, '外键引用关系', fontsize=12, color='#666', va='center')
ax.text(lx+4.2, ly-0.35, 'PK = 主键   FK = 外键   UK = 唯一约束', fontsize=12, color='#666', va='center')
ax.text(lx+0.3, ly-0.7, '1 : N = 一对多    1 : 2 = 一对二    1 : 4 = 一对四', fontsize=12, color='#666')

plt.tight_layout(pad=0.5)
fig.savefig(f'{OUT}/er_diagram.png', dpi=DPI, bbox_inches='tight', facecolor='white')
plt.close()
print("  ✓ er_diagram.png")

# ── Summary ──
print("\n=== Generated Files ===")
for f in sorted(os.listdir(OUT)):
    if f.endswith('.png'):
        sz = os.path.getsize(os.path.join(OUT, f)) / 1024
        print(f"  {f}: {sz:.0f} KB")
