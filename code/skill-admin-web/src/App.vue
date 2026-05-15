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
  { path: '/', label: '概况看板', icon: '📊' },
  { path: '/users', label: '用户管理', icon: '👥' },
  { path: '/appeals', label: '申诉管理', icon: '⚖️' },
  { path: '/skills', label: '技能审核', icon: '🔍' },
  { path: '/announcements', label: '公告管理', icon: '📢' }
]
</script>

<template>
  <div class="layout" v-if="route.path !== '/login'">
    <aside class="sidebar">
      <div class="logo">⏱ 管理后台</div>
      <nav class="nav">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path"
          class="nav-item" :class="{ active: route.path === item.path }">
          <span class="nav-icon">{{ item.icon }}</span>
          {{ item.label }}
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <span class="user-name">{{ store.userInfo.username }}</span>
        <button class="logout-btn" @click="logout">退出</button>
      </div>
    </aside>
    <div class="main-wrap">
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
.layout { display: flex; min-height: 100vh; }
.sidebar {
  width: 220px; background: #1e293b; color: #fff;
  display: flex; flex-direction: column; flex-shrink: 0;
}
.logo {
  padding: 20px; font-size: 18px; font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.08); letter-spacing: 0.5px;
}
.nav { flex: 1; padding: 12px 0; }
.nav-item {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 20px; color: rgba(255,255,255,0.65);
  font-size: 14px; transition: all 0.2s; cursor: pointer;
}
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.nav-item.active { color: #fff; background: rgba(99,102,241,0.15); border-right: 3px solid #6366f1; }
.nav-icon { font-size: 16px; }
.sidebar-footer {
  padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.08);
  display: flex; align-items: center; justify-content: space-between;
}
.user-name { font-size: 13px; color: rgba(255,255,255,0.7); }
.logout-btn {
  background: none; border: 1px solid rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.7); padding: 4px 12px; border-radius: 6px;
  font-size: 12px; cursor: pointer; transition: all 0.2s;
}
.logout-btn:hover { border-color: #f56c6c; color: #f56c6c; }
.main-wrap { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.main { flex: 1; padding: 28px; overflow-y: auto; }
.admin-footer {
  padding: 16px 28px; text-align: center; font-size: 12px;
  color: #94a3b8; border-top: 1px solid #f0f0f0;
}
</style>
