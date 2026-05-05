<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)

onMounted(async () => {
  await loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const res = await api.get('/admin/users')
    users.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function toggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户 "${user.username}"？`, '提示', { type: 'warning' })
  await api.put(`/admin/users/${user.id}/status`, { status: user.status === 1 ? 0 : 1 })
  ElMessage.success(`已${action}`)
  await loadUsers()
}
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>管理平台所有注册用户</p>
      </div>
    </div>

    <div class="table-panel">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ row.email || '未设置' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90" align="center">
          <template #default="{ row }">
            <span class="role-tag" :class="row.role === 'ADMIN' ? 'admin' : 'user'">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="时间币" width="100" align="center">
          <template #default="{ row }">
            <span class="coin-val">{{ row.balance || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="row-status" :class="row.status === 1 ? 'on' : 'off'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <button
              v-if="row.role !== 'ADMIN'"
              :class="['action-btn', row.status === 1 ? 'danger' : 'success']"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </button>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  /* consistent with other pages */
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.table-panel {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #f0e8e0;
}

.text-muted { color: #bbb; }
.coin-val {
  font-weight: 700;
  color: #e8784a;
}
.role-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 4px;
}
.role-tag.admin { background: #fef0f0; color: #f56c6c; }
.role-tag.user { background: #ecf5ff; color: #409eff; }

.row-status {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 4px;
}
.row-status.on { background: #e8f5e9; color: #4caf50; }
.row-status.off { background: #fef0f0; color: #f56c6c; }

.action-btn {
  padding: 4px 14px;
  border: 1px solid;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  font-weight: 500;
}
.action-btn.danger { color: #f56c6c; border-color: #fde2e2; }
.action-btn.danger:hover { background: #fef0f0; }
.action-btn.success { color: #67c23a; border-color: #e1f3d8; }
.action-btn.success:hover { background: #f0f9eb; }
</style>
