<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const appeals = ref([])
const loading = ref(false)

onMounted(async () => {
  await loadAppeals()
})

async function loadAppeals() {
  loading.value = true
  try {
    const res = await api.get('/appeal/list', { params: { page: 1, size: 50 } })
    appeals.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function handleAppeal(appeal) {
  const { value: result } = await ElMessageBox.prompt(
    '请输入处理结果',
    '处理申诉',
    { inputType: 'textarea', inputPlaceholder: '描述处理结果...' }
  )
  if (result) {
    await api.put(`/appeal/${appeal.id}/handle`, { result })
    ElMessage.success('已处理')
    await loadAppeals()
  }
}

const statusMap = {
  1: { label: '待处理', class: 'st-pend' },
  2: { label: '处理中', class: 'st-proc' },
  3: { label: '已处理', class: 'st-done' }
}
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h2>申诉管理</h2>
        <p>处理用户纠纷与申诉</p>
      </div>
    </div>

    <div class="table-panel">
      <el-table :data="appeals" v-loading="loading" stripe>
        <el-table-column prop="id" label="申诉ID" width="200" />
        <el-table-column prop="orderId" label="关联订单" width="200">
          <template #default="{ row }">
            <span class="link-text">{{ row.orderId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申诉原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="证据" width="100" align="center">
          <template #default="{ row }">
            <span class="text-muted">{{ row.evidence ? '有' : '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['appeal-status', (statusMap[row.status] || {}).class || 'st-pend']">
              {{ (statusMap[row.status] || {}).label || row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <button
              v-if="row.status !== 3"
              class="action-btn primary"
              @click="handleAppeal(row)"
            >
              处理
            </button>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && appeals.length === 0" description="暂无申诉" :image-size="80" />
    </div>
  </div>
</template>

<style scoped>
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
.link-text { color: #409eff; font-family: monospace; }

.appeal-status {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 4px;
}
.st-pend { background: #fdf6ec; color: #e6a23c; }
.st-proc { background: #ecf5ff; color: #409eff; }
.st-done { background: #e8f5e9; color: #4caf50; }

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
.action-btn.primary { color: #409eff; border-color: #d9ecff; }
.action-btn.primary:hover { background: #ecf5ff; }
</style>
