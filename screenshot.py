"""Take screenshots for PPT - user side (5173) and admin side (5174)."""
from playwright.sync_api import sync_playwright
import os, time

OUT = r"D:\Agent工作区\Claude Code工作区\综合实训项目\screenshots"
os.makedirs(OUT, exist_ok=True)

def take_user_screenshots(page):
    # Login
    page.goto('http://localhost:5173/login', wait_until='networkidle')
    page.wait_for_timeout(500)
    # Try filling login form
    inputs = page.locator('input')
    count = inputs.count()
    print(f"  Login page has {count} inputs")
    if count >= 2:
        inputs.nth(0).fill('testuser')
        inputs.nth(1).fill('123456')
    page.locator('button').filter(has_text='登录').click()
    page.wait_for_timeout(2500)
    print(f"  After login URL: {page.url}")

    screenshots = [
        ('user-home', 'http://localhost:5173/'),
        ('skill-detail', 'http://localhost:5173/skill/2006'),
        ('wallet', 'http://localhost:5173/wallet'),
        ('profile', 'http://localhost:5173/profile'),
        ('messages', 'http://localhost:5173/messages'),
        ('orders-buyer', 'http://localhost:5173/orders/buyer'),
    ]
    for name, url in screenshots:
        page.goto(url, wait_until='networkidle')
        page.wait_for_timeout(1500)
        filepath = os.path.join(OUT, f'{name}.png')
        page.screenshot(path=filepath)
        sz = os.path.getsize(filepath)
        print(f'  {name}.png: {sz//1024}KB')

def take_admin_screenshots(page):
    # Login as admin
    page.goto('http://localhost:5174/login', wait_until='networkidle')
    page.wait_for_timeout(500)
    inputs = page.locator('input')
    count = inputs.count()
    print(f"  Admin login page has {count} inputs")
    if count >= 2:
        inputs.nth(0).fill('admin')
        inputs.nth(1).fill('admin123')
    page.locator('button').filter(has_text='登录').click()
    page.wait_for_timeout(2500)
    print(f"  After admin login URL: {page.url}")

    screenshots = [
        ('admin-users', 'http://localhost:5174/users'),
        ('admin-skills', 'http://localhost:5174/skills'),
        ('admin-appeals', 'http://localhost:5174/appeals'),
        ('admin-announcements', 'http://localhost:5174/announcements'),
    ]
    for name, url in screenshots:
        page.goto(url, wait_until='networkidle')
        page.wait_for_timeout(1500)
        filepath = os.path.join(OUT, f'{name}.png')
        page.screenshot(path=filepath)
        sz = os.path.getsize(filepath)
        print(f'  {name}.png: {sz//1024}KB')

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)

    print("=== User side (5173) ===")
    ctx = browser.new_context(viewport={"width": 1280, "height": 800})
    page = ctx.new_page()
    take_user_screenshots(page)
    ctx.close()

    print("\n=== Admin side (5174) ===")
    ctx = browser.new_context(viewport={"width": 1280, "height": 800})
    page = ctx.new_page()
    take_admin_screenshots(page)
    ctx.close()

    browser.close()

print("\nDone:", OUT)
