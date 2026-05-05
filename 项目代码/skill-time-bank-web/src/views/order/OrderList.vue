<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import api from '../../api/index'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = computed(() => route.name === 'SellerOrders' ? 'seller' : 'buyer')
const isBuyer = computed(() => mode.value === 'buyer')

const orders = ref([])
const loading = ref(false)
const statusFilter = ref(null)

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const endpoint = isBuyer.value ? '/order/buyer' : '/order/seller'
    const res = await api.get(endpoint, { params: { page: 1, size: 50 } })
    orders.value = res.data?.records || []
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

const statusMap = {
  1: { label: '待确认', tag: 'warning' },
  2: { label: '进行中', tag: 'primary' },
  3: { label: '待确认完成', tag: 'warning' },
  4: { label: '已完成', tag: 'success' },
  5: { label: '已取消', tag: 'info' }
}

const filteredOrders = computed(() => {
  if (!statusFilter.value) return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

async function handleAction(order, action) {
  try {
    switch (action) {
      case 'confirm':
        await api.put(`/order/${order.id}/confirm`)
        ElMessage.success('已确认接单，订单开始进行')
        break
      case 'buyer-complete':
        await api.put(`/order/${order.id}/buyer-complete`)
        ElMessage.success('已确认完成，等待对方确认')
        break
      case 'seller-complete':
        await api.put(`/order/${order.id}/seller-complete`)
        ElMessage.success('已确认完成，等待对方确认')
        break
      case 'cancel':
        await api.put(`/order/${order.id}/cancel`)
        ElMessage.success('订单已取消')
        break
    }
    await loadOrders()
  } catch (e) { /* handled */ }
}
</script>

<template>
  <div class="order-page">
    <div class="page-header">
      <div>
        <h2>{{ isBuyer ? '我购买的' : '我出售的' }}</h2>
        <p>{{ isBuyer ? '管理你作为买方的订单' : '管理你作为卖方的订单' }}</p>
      </div>
      <div class="mode-switch">
        <button :class="{ active: isBuyer }" @click="router.push('/orders/buyer')">我买的</button>
        <button :class="{ active: !isBuyer }" @click="router.push('/orders/seller')">我卖的</button>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <button
        v-for="opt in [{v:null,l:'全部'},{v:1,l:'待确认'},{v:2,l:'进行中'},{v:3,l:'待确认完成'},{v:4,l:'已完成'},{v:5,l:'已取消'}]"
        :key="opt.v"
        :class="['filter-tag', { active: statusFilter === opt.v }]"
        @click="statusFilter = opt.v"
      >
        {{ opt.l }}
      </button>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading">
      <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" :image-size="100" />

      <div v-else class="order-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-top">
            <span class="order-no">#{{ order.orderNo }}</span>
            <el-tag :type="statusMap[order.status]?.tag || 'info'" size="small">
              {{ statusMap[order.status]?.label || '未知' }}
            </el-tag>
          </div>

          <div class="order-body">
            <div class="order-info">
              <span class="order-amount">
                <Icon icon="mdi:star" class="amount-icon" />
                {{ order.amount }} 时间币
              </span>
              <span class="order-skill" v-if="order.skillId">技能 #{{ order.skillId }}</span>
              <span class="order-user">
                {{ isBuyer ? '卖家' : '买家' }} #{{ isBuyer ? order.sellerId : order.buyerId }}
              </span>
            </div>
            <div class="order-time">{{ order.createTime }}</div>
          </div>

          <!-- 操作按钮 -->
          <div class="order-actions" v-if="order.status !== 4 && order.status !== 5">
            <!-- 买方: 待确认 → 无操作，等待卖方确认 -->
            <!-- 卖方: 待确认 → 确认接单 / 取消 -->
            <template v-if="order.status === 1">
              <button v-if="!isBuyer" class="btn-primary" @click="handleAction(order, 'confirm')">确认接单</button>
              <button v-if="!isBuyer" class="btn-cancel" @click="handleAction(order, 'cancel')">拒绝</button>
              <button v-if="isBuyer" class="btn-cancel" @click="handleAction(order, 'cancel')">取消订单</button>
            </template>

            <!-- 进行中 → 双方都可以确认完成 -->
            <template v-if="order.status === 2">
              <button v-if="isBuyer && !order.buyerConfirm" class="btn-primary" @click="handleAction(order, 'buyer-complete')">确认完成</button>
              <button v-if="!isBuyer && !order.sellerConfirm" class="btn-primary" @click="handleAction(order, 'seller-complete')">确认完成</button>
              <span v-if="isBuyer && order.buyerConfirm" class="confirmed-hint">已确认，等待对方</span>
              <span v-if="!isBuyer && order.sellerConfirm" class="confirmed-hint">已确认，等待对方</span>
            </template>

            <!-- 待确认完成 → 未确认方确认 -->
            <template v-if="order.status === 3">
              <button v-if="isBuyer && !order.buyerConfirm" class="btn-primary" @click="handleAction(order, 'buyer-complete')">确认完成</button>
              <button v-if="!isBuyer && !order.sellerConfirm" class="btn-primary" @click="handleAction(order, 'seller-complete')">确认完成</button>
              <span v-if="isBuyer && order.buyerConfirm" class="confirmed-hint">已确认，等待对方</span>
              <span v-if="!isBuyer && order.sellerConfirm" class="confirmed-hint">已确认，等待对方</span>
            </template>
          </div>

          <div class="order-actions" v-else>
            <span class="done-hint" v-if="order.status === 4">
              <Icon icon="mdi:check-circle" /> 已完成
            </span>
            <span class="cancelled-hint" v-else>
              <Icon icon="mdi:close-circle" /> 已取消
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 6px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.mode-switch {
  display: flex;
  background: #f5f0eb;
  border-radius: 10px;
  padding: 3px;
}
.mode-switch button {
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}
.mode-switch button.active {
  background: #fff;
  color: #e8784a;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 筛选 */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.filter-tag {
  padding: 5px 16px;
  border: 1px solid #e8e0d8;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-tag:hover {
  border-color: #e8784a;
  color: #e8784a;
}
.filter-tag.active {
  background: #e8784a;
  color: #fff;
  border-color: #e8784a;
}

/* 订单卡片 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #f0e8e0;
  transition: all 0.2s;
}
.order-card:hover {
  border-color: rgba(232, 120, 74, 0.2);
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}
.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  font-family: monospace;
}
.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.order-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #e8784a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.amount-icon {
  font-size: 18px;
  color: #f0a060;
}
.order-skill, .order-user {
  font-size: 13px;
  color: #999;
}
.order-time {
  font-size: 13px;
  color: #bbb;
}

/* 操作 */
.order-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f5f0eb;
}
.btn-primary {
  padding: 7px 18px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary:hover {
  box-shadow: 0 3px 12px rgba(232, 120, 74, 0.3);
}
.btn-cancel {
  padding: 7px 18px;
  background: #f5f0eb;
  color: #888;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #ebe5de;
}
.confirmed-hint {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}
.done-hint {
  font-size: 14px;
  color: #4caf50;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}
.cancelled-hint {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
