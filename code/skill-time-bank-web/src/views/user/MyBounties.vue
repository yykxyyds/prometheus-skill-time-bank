<script setup>
import { ref, onMounted, computed } from 'vue'
import { getBountyList } from '../../api/skill'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

const router = useRouter()
const bounties = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const filterStatus = ref(null)

const statusMap = {
  0: { label: '待审核', type: 'info' },
  1: { label: '招募中', type: 'warning' },
  2: { label: '已接单', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已拒绝', type: 'danger' },
}

const filters = [
  { label: '全部', value: null },
  { label: '招募中', value: 1 },
  { label: '已接单', value: 2 },
  { label: '已完成', value: 3 },
]

onMounted(async () => {
  await loadBounties()
})

async function loadBounties() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      type: 'publish',
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    const res = await getBountyList(params)
    bounties.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function switchFilter(val) {
  filterStatus.value = val
  page.value = 1
  loadBounties()
}

function onPageChange(p) {
  page.value = p
  loadBounties()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function formatDeadline(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

const applicationCount = (b) => b.applicationCount ?? 0
</script>

<template>
  <div class="my-bounties-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h2>我的需求</h2>
        <p>管理我发布的需求悬赏，查看接单进度</p>
      </div>
      <button class="publish-btn" @click="router.push('/bounty/create')">
        <Icon icon="mdi:plus-circle" /> 发布新需求
      </button>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <button
        v-for="f in filters"
        :key="f.value"
        :class="['filter-btn', { active: filterStatus === f.value }]"
        @click="switchFilter(f.value)"
      >
        {{ f.label }}
      </button>
    </div>

    <!-- 列表 -->
    <div class="bounty-list" v-loading="loading">
      <el-empty v-if="!loading && bounties.length === 0" description="还没有发布过需求" :image-size="100" />

      <article
        v-for="b in bounties"
        :key="b.id"
        class="bounty-card"
        @click="$router.push(`/bounty/${b.id}`)"
      >
        <div class="bounty-top">
          <div class="bounty-title-row">
            <h3 class="bounty-title">{{ b.title }}</h3>
            <el-tag :type="statusMap[b.status]?.type || 'info'" size="small" effect="plain">
              {{ statusMap[b.status]?.label || '未知' }}
            </el-tag>
          </div>
        </div>

        <p class="bounty-desc">
          {{ b.description?.substring(0, 120) }}{{ b.description?.length > 120 ? '...' : '' }}
        </p>

        <div class="bounty-meta">
          <span class="meta-item">
            <Icon icon="mdi:star" class="meta-icon reward" />
            <strong>{{ b.reward }}</strong> 时间币
          </span>
          <span class="meta-item" v-if="applicationCount(b) > 0">
            <Icon icon="mdi:account-group" class="meta-icon" />
            {{ applicationCount(b) }} 人申请
          </span>
          <span class="meta-item" v-if="b.deadline">
            <Icon icon="mdi:calendar" class="meta-icon" />
            {{ formatDeadline(b.deadline) }}
          </span>
          <span class="meta-item">
            <Icon icon="mdi:clock-outline" class="meta-icon" />
            {{ formatDeadline(b.createTime) }}
          </span>
        </div>
      </article>
    </div>

    <!-- 分页 -->
    <div v-if="total > size" class="pagination-wrap">
      <el-pagination
        layout="prev, pager, next"
        :page-size="size"
        :total="total"
        :current-page="page"
        background
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.my-bounties-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

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
  margin: 0 0 4px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  letter-spacing: 0.3px;
}
.publish-btn:hover {
  box-shadow: 0 4px 14px rgba(232,120,74,0.3);
  transform: translateY(-1px);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.filter-btn {
  padding: 8px 18px;
  border: 1px solid #e8e0d8;
  border-radius: 8px;
  background: #fff;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s;
}
.filter-btn:hover {
  border-color: #e8784a;
  color: #e8784a;
}
.filter-btn.active {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border-color: transparent;
}

/* 列表 */
.bounty-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bounty-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  border: 1px solid #f0e8e0;
  transition: all 0.3s ease;
  cursor: pointer;
}
.bounty-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border-color: rgba(232,120,74,0.2);
}

.bounty-top {
  margin-bottom: 10px;
}
.bounty-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.bounty-title {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.bounty-desc {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
  margin: 0 0 14px;
}

.bounty-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding-top: 14px;
  border-top: 1px solid #f5f0eb;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}
.meta-icon {
  font-size: 16px;
  flex-shrink: 0;
}
.meta-icon.reward {
  color: #f0a060;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
