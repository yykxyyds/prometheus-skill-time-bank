<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import api from '../../api/index'
import { Icon } from '@iconify/vue'
import { useScrollReveal } from '../../composables/useScrollReveal'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([RadarChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const userStore = useUserStore()

useScrollReveal('.asset-card', { stagger: 100 })
useScrollReveal('.content-panel', { stagger: 150 })

const balanceInfo = ref({ balance: 0, frozenBalance: 0, totalEarned: 0, totalSpent: 0 })
const transactions = ref([])
const reputation = ref({ punctualityScore: 0, communicationScore: 0, professionalScore: 0, attitudeScore: 0 })
const activeTab = ref('transactions')

onMounted(async () => {
  try {
    const [balRes, txRes] = await Promise.all([
      api.get('/wallet/balance'),
      api.get('/wallet/transactions', { params: { page: 1, size: 50 } })
    ])
    balanceInfo.value = balRes.data || {}
    transactions.value = txRes.data?.records || []
  } catch (e) { /* handled by interceptor */ }

  // 获取信誉分
  try {
    const repRes = await api.get(`/review/reputation/${userStore.userId}`)
    if (repRes.data) {
      reputation.value = repRes.data
    }
  } catch (e) { /* 新用户可能无数据 */ }
})

// 雷达图配置
const radarOption = computed(() => ({
  radar: {
    center: ['50%', '55%'],
    radius: '65%',
    indicator: [
      { name: '按时交付', max: 5 },
      { name: '沟通能力', max: 5 },
      { name: '专业水平', max: 5 },
      { name: '服务态度', max: 5 }
    ],
    axisName: {
      color: '#666',
      fontSize: 12,
      borderRadius: 3,
      padding: [3, 5]
    },
    splitArea: {
      areaStyle: {
        color: ['rgba(232,120,74,0.02)', 'rgba(232,120,74,0.02)', 'rgba(232,120,74,0.04)', 'rgba(232,120,74,0.04)']
      }
    },
    splitLine: {
      lineStyle: { color: 'rgba(232,120,74,0.15)' }
    },
    axisLine: {
      lineStyle: { color: 'rgba(232,120,74,0.2)' }
    }
  },
  series: [{
    type: 'radar',
    data: [{
      value: [
        reputation.value.punctualityScore || 0,
        reputation.value.communicationScore || 0,
        reputation.value.professionalScore || 0,
        reputation.value.attitudeScore || 0
      ],
      name: '我的信誉',
      areaStyle: {
        color: {
          type: 'radial',
          x: 0.5, y: 0.5, r: 0.5,
          colorStops: [
            { offset: 0, color: 'rgba(240,160,96,0.35)' },
            { offset: 1, color: 'rgba(232,120,74,0.08)' }
          ]
        }
      },
      lineStyle: { color: '#e8784a', width: 2 },
      itemStyle: { color: '#e8784a' }
    }]
  }]
}))

const typeMap = {
  INCOME: { label: '收入', class: 'tag-income' },
  EXPENSE: { label: '支出', class: 'tag-expense' },
  FREEZE: { label: '冻结', class: 'tag-freeze' },
  UNFREEZE: { label: '解冻', class: 'tag-unfreeze' },
  GIFT: { label: '赠送', class: 'tag-gift' }
}

const statCards = computed(() => [
  { label: '可用余额', value: balanceInfo.value.balance || 0, icon: 'mdi:wallet', color: '#e8784a' },
  { label: '冻结中', value: balanceInfo.value.frozenBalance || 0, icon: 'mdi:lock', color: '#909399' },
  { label: '累计收入', value: balanceInfo.value.totalEarned || 0, icon: 'mdi:arrow-up-bold', color: '#67c23a' },
  { label: '累计支出', value: balanceInfo.value.totalSpent || 0, icon: 'mdi:arrow-down-bold', color: '#f56c6c' }
])
</script>

<template>
  <div class="wallet">
    <div class="page-header">
      <h2>时间银行</h2>
      <p>管理你的时间资产与信誉档案</p>
    </div>

    <!-- 资产卡片 -->
    <div class="asset-cards">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="asset-card reveal-on-scroll"
        :style="{ '--accent': card.color }"
      >
        <div class="asset-icon"><Icon :icon="card.icon" /></div>
        <div class="asset-info">
          <span class="asset-value">{{ card.value.toLocaleString() }}</span>
          <span class="asset-label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <!-- 内容区：流水 + 信誉 -->
    <div class="wallet-content">
      <!-- 信誉雷达图 -->
      <div class="content-panel reputation-panel reveal-on-scroll">
        <h3>信誉档案</h3>
        <div class="radar-wrap">
          <VChart :option="radarOption" autoresize style="height:320px" />
        </div>
        <div class="rep-scores">
          <div class="rep-item">
            <span class="rep-dot" style="background:#e8784a"></span>
            <span>按时 {{ reputation.punctualityScore || 0 }}/5</span>
          </div>
          <div class="rep-item">
            <span class="rep-dot" style="background:#f0a060"></span>
            <span>沟通 {{ reputation.communicationScore || 0 }}/5</span>
          </div>
          <div class="rep-item">
            <span class="rep-dot" style="background:#e89050"></span>
            <span>专业 {{ reputation.professionalScore || 0 }}/5</span>
          </div>
          <div class="rep-item">
            <span class="rep-dot" style="background:#d87040"></span>
            <span>态度 {{ reputation.attitudeScore || 0 }}/5</span>
          </div>
        </div>
      </div>

      <!-- 交易流水 -->
      <div class="content-panel transaction-panel reveal-on-scroll">
        <h3>时间流水</h3>
        <div class="transaction-list" v-if="transactions.length > 0">
          <div
            v-for="tx in transactions"
            :key="tx.id"
            class="tx-item"
          >
            <div class="tx-left">
              <span :class="['tx-badge', (typeMap[tx.type] || {}).class || 'tag-expense']">
                {{ (typeMap[tx.type] || {}).label || tx.type }}
              </span>
            </div>
            <div class="tx-center">
              <span class="tx-remark">{{ tx.remark || '交易记录' }}</span>
              <span class="tx-time">{{ tx.createTime }}</span>
            </div>
            <div class="tx-right">
              <span :class="['tx-amount', tx.type === 'INCOME' || tx.type === 'GIFT' || tx.type === 'UNFREEZE' ? 'positive' : 'negative']">
                {{ tx.type === 'INCOME' || tx.type === 'GIFT' || tx.type === 'UNFREEZE' ? '+' : '-' }}{{ tx.amount }}
              </span>
              <span class="tx-balance">余额 {{ tx.balanceAfter }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无交易记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 28px;
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

/* ========== 资产卡片 ========== */
.asset-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}
.asset-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f0e8e0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}
.asset-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--accent);
  border-radius: 4px 0 0 4px;
}
.asset-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}
.asset-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(232,120,74,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--accent);
  flex-shrink: 0;
}
.asset-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.asset-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--accent);
  line-height: 1.1;
}
.asset-label {
  font-size: 13px;
  color: #999;
  font-weight: 500;
}

/* ========== 内容区双栏 ========== */
.wallet-content {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 20px;
}
.content-panel {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #f0e8e0;
}
.content-panel h3 {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px;
}

/* 雷达图 */
.radar-wrap {
  display: flex;
  justify-content: center;
}
.rep-scores {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 8px;
}
.rep-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}
.rep-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 交易列表 */
.transaction-list {
  display: flex;
  flex-direction: column;
}
.tx-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f0eb;
}
.tx-item:last-child {
  border-bottom: none;
}
.tx-left {
  flex-shrink: 0;
}
.tx-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.tag-income { background: #e8f5e9; color: #4caf50; }
.tag-expense { background: #fef0f0; color: #f56c6c; }
.tag-freeze { background: #fdf6ec; color: #e6a23c; }
.tag-unfreeze { background: #ecf5ff; color: #409eff; }
.tag-gift { background: #f5f0ff; color: #9c27b0; }

.tx-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.tx-remark {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.tx-time {
  font-size: 12px;
  color: #bbb;
}
.tx-right {
  text-align: right;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.tx-amount {
  font-size: 15px;
  font-weight: 700;
}
.tx-amount.positive { color: #4caf50; }
.tx-amount.negative { color: #f56c6c; }
.tx-balance {
  font-size: 12px;
  color: #bbb;
}
</style>
