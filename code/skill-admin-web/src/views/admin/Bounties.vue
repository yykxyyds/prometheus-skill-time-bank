<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const bounties = ref([])
const loading = ref(false)
const filterStatus = ref('')
const page = ref(1)
const total = ref(0)

const statusFilters = [
  { label: '全部', value: '' },
  { label: '待审核', value: '0' },
  { label: '已发布', value: '1' },
  { label: '已接单', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已拒绝', value: '4' }
]

const statusMap = {
  0: { label: '待审核', class: 'st-pend' },
  1: { label: '已发布', class: 'st-on' },
  2: { label: '已接单', class: 'st-proc' },
  3: { label: '已完成', class: 'st-done' },
  4: { label: '已拒绝', class: 'st-rej' }
}

const pendingCount = computed(() => bounties.value.filter(b => b.status === 0).length)

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 16)
}

async function fetchBounties() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await api.get('/admin/bounty/list', { params })
    bounties.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    await api.put(`/admin/bounty/${id}/status`, { status: 1 })
    ElMessage.success('已通过')
    fetchBounties()
  } catch (e) { /* handled by interceptor */ }
}

async function reject(id) {
  try {
    await ElMessageBox.confirm('确认拒绝此悬赏？', '拒绝', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await api.put(`/admin/bounty/${id}/status`, { status: 4 })
    ElMessage.success('已拒绝')
    fetchBounties()
  } catch (e) { /* cancelled or error */ }
}

function handlePageChange(val) {
  page.value = val
  fetchBounties()
}

function setFilter(val) {
  filterStatus.value = val
  page.value = 1
  fetchBounties()
}

onMounted(fetchBounties)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h2>悬赏审核</h2>
        <p>审核用户发布的需求悬赏</p>
      </div>
      <div class="header-meta">
        <span class="total-badge">共 {{ total }} 条</span>
        <span v-if="pendingCount > 0" class="pending-badge">{{ pendingCount }} 条待审核</span>
      </div>
    </div>

    <div class="filter-bar">
      <button v-for="f in statusFilters" :key="f.value"
        :class="['filter-btn', { active: filterStatus === f.value }]"
        @click="setFilter(f.value)">
        {{ f.label }}
      </button>
    </div>

    <div class="table-panel">
      <el-table :data="bounties" v-loading="loading" stripe>
        <el-table-column type="expand" width="36">
          <template #default="{ row }">
            <div class="expand-content">
              <p><strong>描述：</strong>{{ row.description || '暂无描述' }}</p>
              <p v-if="row.deadline"><strong>截止时间：</strong>{{ formatTime(row.deadline) }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="悬赏标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="bounty-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="悬赏金额" width="120" align="center">
          <template #default="{ row }">
            <span class="coin-val">{{ row.reward || 0 }} <small>币</small></span>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="110" align="center">
          <template #default="{ row }">
            <span class="text-muted">#{{ row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', (statusMap[row.status] || {}).class || '']">
              {{ (statusMap[row.status] || {}).label || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <button class="action-btn success" @click="approve(row.id)">通过</button>
              <button class="action-btn danger" @click="reject(row.id)">拒绝</button>
            </template>
            <span v-else class="done-tag">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-wrap" v-if="total > 10">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }
.page-header p { margin: 2px 0 0; font-size: 14px; color: #94a3b8; }
.header-meta { display: flex; gap: 10px; align-items: center; }
.total-badge { font-size: 13px; color: #94a3b8; }
.pending-badge { font-size: 12px; background: #fef3c7; color: #d97706; padding: 3px 10px; border-radius: 12px; font-weight: 600; }

.filter-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-btn {
  padding: 6px 18px; border: 1px solid #e2e8f0; border-radius: 20px;
  background: #fff; font-size: 13px; color: #64748b; cursor: pointer;
  transition: all 0.2s; font-weight: 500;
}
.filter-btn:hover { border-color: #6366f1; color: #6366f1; }
.filter-btn.active { background: #6366f1; color: #fff; border-color: #6366f1; }

.bounty-title { font-weight: 500; color: #1e293b; }

.coin-val { font-weight: 600; color: #e8784a; }
.coin-val small { font-weight: 400; color: #94a3b8; font-size: 12px; }

.status-tag { font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 4px; }
.st-pend { background: #fef3c7; color: #d97706; }
.st-on { background: #d1fae5; color: #059669; }
.st-proc { background: #dbeafe; color: #2563eb; }
.st-done { background: #f1f5f9; color: #64748b; }
.st-rej { background: #fee2e2; color: #dc2626; }

.done-tag { color: #94a3b8; font-size: 12px; }

.action-btn { padding: 5px 18px; border: 1px solid; border-radius: 8px; font-size: 12px; cursor: pointer; transition: all 0.2s; background: #fff; font-weight: 500; margin: 0 4px; }
.action-btn.success { color: #059669; border-color: #a7f3d0; }
.action-btn.success:hover { background: #d1fae5; border-color: #059669; }
.action-btn.danger { color: #dc2626; border-color: #fecaca; }
.action-btn.danger:hover { background: #fee2e2; border-color: #dc2626; }

.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }

.expand-content { padding: 16px 40px; font-size: 14px; color: #64748b; line-height: 1.8; background: #f8fafc; border-radius: 8px; }
.expand-content p { margin: 0; }
</style>
