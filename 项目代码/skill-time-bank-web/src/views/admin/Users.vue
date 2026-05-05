<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/index'

const users = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  const res = await api.get('/admin/users')
  users.value = res.data || []
  loading.value = false
})

async function toggleStatus(user) {
  await api.put(`/admin/users/${user.id}/status`, { status: user.status === 1 ? 0 : 1 })
  const res = await api.get('/admin/users')
  users.value = res.data || []
}
</script>

<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="balance" label="时间币" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.role !== 'ADMIN'" size="small" :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
