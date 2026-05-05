<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/index'
import { ElMessage } from 'element-plus'

const appeals = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  const res = await api.get('/appeal/list')
  appeals.value = res.data?.records || []
  loading.value = false
})

async function handleAppeal(id, result) {
  await api.put(`/appeal/${id}/handle`, { result })
  ElMessage.success('处理完成')
  const res = await api.get('/appeal/list')
  appeals.value = res.data?.records || []
}
</script>

<template>
  <div>
    <h2>申诉管理</h2>
    <el-table :data="appeals" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="orderId" label="订单ID" width="180" />
      <el-table-column prop="reason" label="申诉原因" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 3 ? 'success' : 'warning'">
            {{ {1:'待处理',2:'处理中',3:'已处理'}[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" v-if="false">
        <template #default="{ row }">
          <el-button v-if="row.status !== 3" size="small" type="primary" @click="handleAppeal(row.id, '已处理')">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
