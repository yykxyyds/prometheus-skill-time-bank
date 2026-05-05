<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSkillDetail } from '../api/skill'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import api from '../api/index'
import { Icon } from '@iconify/vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const skill = ref({})
const loading = ref(false)
const orderAmount = ref(1)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSkillDetail(route.params.id)
    skill.value = res.data || {}
  } finally {
    loading.value = false
  }
})

async function handleOrder() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (skill.value.userId === userStore.userId) {
    ElMessage.warning('不能购买自己的技能')
    return
  }
  try {
    await api.post('/order', {
      sellerId: skill.value.userId,
      skillId: skill.value.id,
      amount: skill.value.price * orderAmount.value
    })
    ElMessage.success('下单成功！')
    router.push('/wallet')
  } catch (e) {
    // handled by interceptor
  }
}
</script>

<template>
  <div class="detail" v-loading="loading">
    <template v-if="skill.id">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">技能广场</router-link>
        <span class="sep">/</span>
        <span>{{ skill.categoryName || '技能' }}</span>
        <span class="sep">/</span>
        <span class="current">{{ skill.title }}</span>
      </div>

      <div class="detail-layout">
        <!-- 左侧主体 -->
        <div class="detail-main">
          <div class="cover-wrap">
            <div class="cover-img">
              <span class="cover-letter">{{ skill.title?.charAt(0) }}</span>
            </div>
          </div>

          <div class="section">
            <div class="title-row">
              <h1>{{ skill.title }}</h1>
              <span class="status-tag" :class="skill.status === 1 ? 'online' : 'offline'">
                {{ skill.status === 1 ? '上架中' : '已下架' }}
              </span>
            </div>

            <div class="meta-row">
              <span class="meta-badge"><Icon icon="mdi:eye" /> {{ skill.viewCount || 0 }} 浏览</span>
              <span class="meta-badge"><Icon icon="mdi:check" /> {{ skill.orderCount || 0 }} 单成交</span>
              <span class="meta-badge"><Icon icon="mdi:calendar" /> {{ skill.availableTime || '灵活安排' }}</span>
            </div>

            <div class="desc-section">
              <h3>技能描述</h3>
              <p>{{ skill.description || '暂无描述' }}</p>
            </div>

            <div class="seller-section" v-if="skill.userName">
              <h3>技能提供者</h3>
              <div class="seller-card" @click="router.push(`/profile/${skill.userId}`)">
                <div class="seller-avatar">{{ skill.userName?.charAt(0) }}</div>
                <div class="seller-info">
                  <span class="seller-name">{{ skill.userName }}</span>
                  <span class="seller-hint">点击查看主页</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧预订卡片 -->
        <div class="detail-side">
          <div class="order-card">
            <div class="price-section">
              <span class="price-num">{{ skill.price }}</span>
              <span class="price-unit">时间币 / 小时</span>
            </div>

            <div class="quantity-row" v-if="skill.status === 1">
              <label>服务时长</label>
              <div class="qty-control">
                <button @click="orderAmount = Math.max(1, orderAmount - 1)">-</button>
                <span>{{ orderAmount }}</span>
                <button @click="orderAmount = orderAmount + 1">+</button>
              </div>
            </div>

            <div class="total-row">
              <span>合计</span>
              <span class="total-price">{{ skill.price * orderAmount }} 时间币</span>
            </div>

            <button
              class="order-btn"
              :disabled="skill.status !== 1"
              @click="handleOrder"
            >
              {{ skill.status === 1 ? '立即预约' : '已下架' }}
            </button>

            <p class="order-note">预约后时间币将被冻结，双方确认完成后转账</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.breadcrumb {
  margin-bottom: 20px;
  font-size: 13px;
  color: #bbb;
}
.breadcrumb a { color: #e8784a; }
.breadcrumb .sep { margin: 0 8px; }
.breadcrumb .current { color: #555; }

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

/* ========== 封面 ========== */
.cover-wrap {
  margin-bottom: 24px;
}
.cover-img {
  width: 100%;
  height: 260px;
  background: linear-gradient(135deg, #e8784a 0%, #f0a060 30%, #f5c090 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-letter {
  font-size: 80px;
  font-weight: 800;
  color: rgba(255,255,255,0.85);
  text-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

/* ========== 标题行 ========== */
.section {
  background: #fff;
  border-radius: 14px;
  padding: 28px;
  border: 1px solid #f0e8e0;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.title-row h1 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}
.status-tag {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.status-tag.online { background: #e8f5e9; color: #4caf50; }
.status-tag.offline { background: #f5f5f5; color: #999; }

/* 元数据 */
.meta-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.meta-badge {
  font-size: 13px;
  color: #888;
  background: #fdf9f6;
  padding: 4px 12px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 描述 */
.desc-section {
  margin-bottom: 24px;
}
.desc-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}
.desc-section p {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

/* 提供者 */
.seller-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}
.seller-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid #f0e8e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}
.seller-card:hover {
  border-color: #e8784a;
  background: #fdf9f6;
}
.seller-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}
.seller-info {
  display: flex;
  flex-direction: column;
}
.seller-name {
  font-weight: 600;
  color: #333;
}
.seller-hint {
  font-size: 12px;
  color: #bbb;
}

/* ========== 右侧预订卡 ========== */
.order-card {
  background: #fff;
  border-radius: 14px;
  padding: 28px 24px;
  border: 1px solid #f0e8e0;
  position: sticky;
  top: 84px;
}
.price-section {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f5f0eb;
}
.price-num {
  font-size: 36px;
  font-weight: 800;
  color: #e8784a;
  display: block;
  line-height: 1.1;
}
.price-unit {
  font-size: 13px;
  color: #999;
}
.quantity-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.quantity-row label {
  font-size: 14px;
  color: #555;
  font-weight: 500;
}
.qty-control {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #e8e0d8;
  border-radius: 8px;
  overflow: hidden;
}
.qty-control button {
  width: 32px;
  height: 32px;
  border: none;
  background: #fafafa;
  cursor: pointer;
  font-size: 16px;
  color: #555;
  transition: all 0.2s;
}
.qty-control button:hover { background: #f0a060; color: #fff; }
.qty-control span {
  width: 36px;
  text-align: center;
  font-weight: 600;
  color: #333;
}
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  font-size: 14px;
  color: #555;
}
.total-price {
  font-size: 20px;
  font-weight: 800;
  color: #e8784a;
}
.order-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.5px;
  transition: all 0.3s;
}
.order-btn:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(232,120,74,0.35);
  transform: translateY(-1px);
}
.order-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.order-note {
  text-align: center;
  font-size: 12px;
  color: #bbb;
  margin: 12px 0 0;
  line-height: 1.5;
}
</style>
