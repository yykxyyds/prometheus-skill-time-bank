<script setup>
import { useAdminStore } from './stores/user'
import { useRoute, useRouter } from 'vue-router'

const store = useAdminStore()
const route = useRoute()
const router = useRouter()

function logout() {
  store.logout()
  router.push('/login')
}

const navItems = [
  { path: '/', label: '概况看板',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>' },
  { path: '/users', label: '用户管理',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>' },
  { path: '/appeals', label: '申诉管理',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>' },
  { path: '/skills', label: '技能审核',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>' },
  { path: '/bounties', label: '悬赏审核',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>' },
  { path: '/announcements', label: '公告管理',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2 11 13"/><path d="M22 2v7l-2 3-2 3-8-8 3-2 3-2h7z"/><path d="M2 13l3 3 3-3"/><path d="M14 19l-3 3-3-3"/></svg>' }
]
</script>

<template>
  <div class="layout" v-if="route.path !== '/login'">
    <aside class="sidebar">
      <div class="logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="logo-icon">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        <span>管理后台</span>
      </div>
      <nav class="nav">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="nav-item" :class="{ active: route.path === item.path }">
          <span class="nav-icon" v-html="item.svg"></span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="admin-info">
          <span class="user-avatar">{{ store.userInfo.username?.charAt(0)?.toUpperCase() }}</span>
          <span class="user-name">{{ store.userInfo.username }}</span>
        </div>
        <button class="logout-btn" @click="logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="logout-icon">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          退出
        </button>
      </div>
    </aside>
    <div class="main-wrap">
      <header class="top-bar">
        <span class="page-title">{{ navItems.find(n => n.path === route.path)?.label || '管理后台' }}</span>
        <div class="top-bar-right">
          <span class="top-bar-time">{{ new Date().toLocaleString('zh-CN', { hour12: false }) }}</span>
        </div>
      </header>
      <main class="main">
        <router-view />
      </main>
      <footer class="admin-footer">
        <span>&copy; 2026 Prometheus 技能时间银行</span>
      </footer>
    </div>
  </div>
  <router-view v-else />
</template>

<style>
.layout { display: flex; min-height: 100vh; background: #f5f6fa; }

/* Sidebar */
.sidebar {
  width: 230px; background: linear-gradient(180deg, #1a1d2e 0%, #232738 100%);
  display: flex; flex-direction: column; flex-shrink: 0;
  position: sticky; top: 0; height: 100vh;
}
.logo {
  padding: 22px 20px; font-size: 17px; font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  display: flex; align-items: center; gap: 10px; color: #fff;
  letter-spacing: 0.3px;
}
.logo-icon { width: 22px; height: 22px; color: #818cf8; }
.nav { flex: 1; padding: 14px 10px; display: flex; flex-direction: column; gap: 2px; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 11px 14px; color: rgba(255,255,255,0.55);
  font-size: 14px; transition: all 0.25s; cursor: pointer;
  border-radius: 8px; text-decoration: none; font-weight: 500;
  position: relative;
}
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.07); }
.nav-item.active {
  color: #fff; background: rgba(99,102,241,0.18);
  box-shadow: inset 3px 0 0 #818cf8;
}
.nav-icon { width: 20px; height: 20px; display: flex; align-items: center; flex-shrink: 0; }
.nav-icon :deep(svg) { width: 100%; height: 100%; }

/* Sidebar footer */
.sidebar-footer {
  padding: 16px 14px; border-top: 1px solid rgba(255,255,255,0.06);
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
}
.admin-info { display: flex; align-items: center; gap: 8px; min-width: 0; }
.user-avatar {
  width: 30px; height: 30px; border-radius: 8px; background: linear-gradient(135deg, #818cf8, #6366f1);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.user-name { font-size: 13px; color: rgba(255,255,255,0.7); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.logout-btn {
  background: none; border: none; color: rgba(255,255,255,0.4);
  padding: 6px 8px; border-radius: 6px; font-size: 12px; cursor: pointer;
  transition: all 0.2s; display: flex; align-items: center; gap: 4px; flex-shrink: 0;
}
.logout-btn:hover { color: #f56c6c; background: rgba(245,108,108,0.1); }
.logout-icon { width: 14px; height: 14px; }

/* Top bar */
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 28px; background: #fff; border-bottom: 1px solid #f0f0f0;
  position: sticky; top: 0; z-index: 10;
}
.page-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.top-bar-time { font-size: 13px; color: #94a3b8; }

/* Main area */
.main-wrap { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.main { flex: 1; padding: 28px; overflow-y: auto; }
.admin-footer {
  padding: 14px 28px; text-align: center; font-size: 12px;
  color: #94a3b8; border-top: 1px solid #f0f0f0; background: #fff;
}

/* Shared page styles used across admin pages */
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-header p { font-size: 14px; color: #94a3b8; margin: 0; }

.table-panel {
  background: #fff; border-radius: 14px; overflow: hidden;
  border: 1px solid #eef2f6; box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}
.table-panel .el-table { border-radius: 14px; }
.table-panel .el-table th.el-table__cell {
  background: #f8fafc; color: #64748b; font-weight: 600; font-size: 13px;
}
.table-panel .el-table .el-table__body tr:hover > td { background: #f8fafc; }

.text-muted { color: #94a3b8; }
</style>
