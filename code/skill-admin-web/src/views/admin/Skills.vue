<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const skills = ref([])
const loading = ref(false)
const filterStatus = ref('')
const page = ref(1)
const total = ref(0)

const statusFilterOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: '2' },
  { label: '已上架', value: '1' },
  { label: '已拒绝', value: '3' },
  { label: '已下架', value: '0' }
]

const statusMap = {
  0: { label: '已下架', type: 'info' },
  1: { label: '已上架', type: 'success' },
  2: { label: '待审核', type: 'warning' },
  3: { label: '已拒绝', type: 'danger' }
}

async function fetchSkills() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await api.get('/admin/skill/list', { params })
    skills.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    await api.put(`/admin/skill/${id}/status`, { status: 1 })
    ElMessage.success('已通过')
    fetchSkills()
  } catch (e) {
    // handled by interceptor
  }
}

async function reject(id) {
  try {
    await ElMessageBox.confirm('确认拒绝此技能？', '拒绝', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await api.put(`/admin/skill/${id}/status`, { status: 3 })
    ElMessage.success('已拒绝')
    fetchSkills()
  } catch (e) {
    // cancelled or error
  }
}

function handlePageChange(val) {
  page.value = val
  fetchSkills()
}

function handleFilterChange(val) {
  filterStatus.value = val
  page.value = 1
  fetchSkills()
}

onMounted(fetchSkills)
</script>

<template>
  <div class="skill-review-page">
    <div class="page-header">
      <h2>技能审核</h2>
      <el-select :model-value="filterStatus" placeholder="状态筛选" @change="handleFilterChange" style="width:140px">
        <el-option v-for="opt in statusFilterOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </div>

    <el-table :data="skills" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="技能名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="发布者ID" width="90" />
      <el-table-column prop="price" label="价格(币/时)" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.label || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 2">
            <el-button size="small" type="success" @click="approve(row.id)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(row.id)">拒绝</el-button>
          </template>
          <el-tag v-else size="small" type="info">已处理</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.skill-review-page { max-width: 1200px; }
.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 20px; color: #1e293b; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
</style>
