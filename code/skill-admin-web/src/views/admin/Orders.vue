<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref(null)

const statusMap = {
  1: { label: '待确认', class: 'st-pend' },
  2: { label: '进行中', class: 'st-proc' },
  3: { label: '待完成', class: 'st-wait' },
  4: { label: '已完成', class: 'st-done' },
  5: { label: '已取消', class: 'st-canc' }
}

onMounted(() => loadOrders())

async function loadOrders() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/admin/order/list', { params })
    orders.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function filterByStatus(status) {
  statusFilter.value = status
  loadOrders()
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>订单管理</h2>
      <p>查看系统所有订单与状态</p>
    </div>

    <div class="filter-bar">
      <button :class="['filter-btn', !statusFilter && 'active']" @click="filterByStatus(null)">全部</button>
      <button v-for="(v, k) in statusMap" :key="k"
        :class="['filter-btn', statusFilter === Number(k) && 'active']"
        @click="filterByStatus(Number(k))">{{ v.label }}</button>
    </div>

    <div class="table-panel">
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单ID" width="200" />
        <el-table-column prop="skillName" label="技能" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.skillName || row.bountyTitle || '-' }}</template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买方" width="120" show-overflow-tooltip />
        <el-table-column prop="sellerName" label="卖方" width="120" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="80" align="center">
          <template #default="{ row }">{{ row.amount }} 币</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['order-status', (statusMap[row.status] || {}).class]">
              {{ (statusMap[row.status] || {}).label || row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="170">
          <template #default="{ row }">{{ row.completedTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" :image-size="80" />
    </div>
  </div>
</template>

<style scoped>
.filter-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-btn {
  padding: 6px 18px; border: 1px solid #e8e0d8; border-radius: 20px;
  background: #fff; font-size: 13px; color: #666; cursor: pointer; transition: all 0.2s;
}
.filter-btn:hover { border-color: #6366f1; color: #6366f1; }
.filter-btn.active { background: #6366f1; color: #fff; border-color: #6366f1; }

.order-status { font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 4px; }
.st-pend { background: #fdf6ec; color: #e6a23c; }
.st-proc { background: #e8f5e9; color: #4caf50; }
.st-wait { background: #ecf5ff; color: #409eff; }
.st-done { background: #f0f0f0; color: #94a3b8; }
.st-canc { background: #fef0f0; color: #f56c6c; }
</style>
