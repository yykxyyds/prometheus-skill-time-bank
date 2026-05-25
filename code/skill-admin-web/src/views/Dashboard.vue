<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { useAdminStore } from '../stores/user'

const store = useAdminStore()
const stats = ref({ users: 0, appeals: 0, announcements: 0 })
const loading = ref(true)

onMounted(async () => {
  try {
    const [usersRes, appealsRes, annosRes] = await Promise.all([
      api.get('/admin/users'),
      api.get('/admin/appeal/list', { params: { page: 1, size: 1 } }),
      api.get('/announcement/list', { params: { page: 1, size: 1 } })
    ])
    stats.value = {
      users: usersRes.data?.length || 0,
      appeals: appealsRes.data?.total || 0,
      announcements: annosRes.data?.total || 0
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
})

const cards = [
  {
    label: '注册用户', key: 'users',
    color: '#6366f1', bg: '#eef2ff',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  },
  {
    label: '申诉数量', key: 'appeals',
    color: '#f59e0b', bg: '#fffbeb',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>'
  },
  {
    label: '公告数量', key: 'announcements',
    color: '#10b981', bg: '#ecfdf5',
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 2 11 13"/><path d="M22 2v7l-2 3-2 3-8-8 3-2 3-2h7z"/><path d="M2 13l3 3 3-3"/><path d="M14 19l-3 3-3-3"/></svg>'
  },
  {
    label: '当前管理员', key: 'admin',
    color: '#ec4899', bg: '#fdf2f8',
    val: () => store.userInfo.username,
    svg: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>'
  }
]
</script>

<template>
  <div>
    <div class="page-header">
      <h2>概况看板</h2>
      <p>系统运营数据概览</p>
    </div>
    <div class="stat-grid" v-loading="loading">
      <div v-for="card in cards" :key="card.key" class="stat-card" :style="{ '--accent': card.color, '--accent-bg': card.bg }">
        <div class="stat-icon-wrap">
          <span class="stat-icon" v-html="card.svg"></span>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ card.val ? card.val() : stats[card.key] }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-header p { font-size: 14px; color: #94a3b8; margin: 0; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }

.stat-card {
  background: #fff; border-radius: 16px; padding: 24px;
  border: 1px solid #eef2f6; box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  display: flex; align-items: center; gap: 16px;
  transition: all 0.3s; cursor: default;
}
.stat-card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  transform: translateY(-2px);
  border-color: var(--accent);
}

.stat-icon-wrap {
  width: 52px; height: 52px; border-radius: 14px;
  background: var(--accent-bg); color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-icon { width: 26px; height: 26px; display: flex; }
.stat-icon :deep(svg) { width: 100%; height: 100%; }

.stat-info { min-width: 0; }
.stat-num { font-size: 32px; font-weight: 700; color: #1e293b; line-height: 1.2; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #94a3b8; }
</style>
