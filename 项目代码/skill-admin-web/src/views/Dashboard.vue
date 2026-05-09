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
</script>

<template>
  <div>
    <h2 style="margin-bottom:24px;font-size:24px;">概况看板</h2>
    <div class="stat-grid" v-loading="loading">
      <div class="stat-card">
        <div class="stat-num">{{ stats.users }}</div>
        <div class="stat-label">注册用户</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.appeals }}</div>
        <div class="stat-label">申诉数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.announcements }}</div>
        <div class="stat-label">公告数量</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ store.userInfo.username }}</div>
        <div class="stat-label">当前管理员</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.stat-card {
  background: #fff; border-radius: 14px; padding: 28px; text-align: center;
  border: 1px solid #f0e8e0; transition: all 0.3s;
}
.stat-card:hover { box-shadow: 0 4px 14px rgba(0,0,0,0.06); }
.stat-num { font-size: 36px; font-weight: 700; color: #6366f1; margin-bottom: 6px; }
.stat-label { font-size: 14px; color: #94a3b8; }
</style>
