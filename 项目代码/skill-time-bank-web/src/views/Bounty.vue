<script setup>
import { ref, onMounted } from 'vue'
import { getBountyList } from '../api/skill'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const bounties = ref([])
const loading = ref(false)
const query = ref({ page: 1, size: 20, status: null, type: null })

const filterOpts = [
  { type: null, label: '全部' },
  { type: 'publish', label: '已发布', needLogin: true },
  { type: 'take', label: '已接单', needLogin: true },
  { type: 'complete', label: '已完成', needLogin: true }
]

onMounted(async () => {
  await loadBounties()
})

async function loadBounties() {
  loading.value = true
  try {
    const res = await getBountyList(query.value)
    bounties.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function switchFilter(opt) {
  if (opt.needLogin && !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  query.value.type = opt.type
  query.value.page = 1
  loadBounties()
}

const statusMap = {
  1: { label: '已发布', class: 'st-pub' },
  2: { label: '已接单', class: 'st-taken' },
  3: { label: '已完成', class: 'st-done' },
  4: { label: '已过期', class: 'st-exp' }
}

function statusClass(status) {
  return (statusMap[status] || {}).class || 'st-pub'
}

function statusLabel(status) {
  return (statusMap[status] || {}).label || '未知'
}
</script>

<template>
  <div class="bounty-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h2>需求悬赏</h2>
        <p>发布你的需求，找到合适的技能提供者</p>
      </div>
      <button class="publish-btn" v-if="userStore.isLoggedIn" @click="router.push('/bounty/create')">
        发布悬赏
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <button
        v-for="opt in filterOpts"
        :key="opt.label"
        :class="['filter-tag', { active: query.type === opt.type }]"
        @click="switchFilter(opt)"
      >
        {{ opt.label }}
      </button>
    </div>

    <!-- 悬赏列表 -->
    <div class="bounty-grid" v-loading="loading">
      <el-empty v-if="!loading && bounties.length === 0" description="暂无悬赏需求" :image-size="100" />

      <article
        v-for="b in bounties"
        :key="b.id"
        class="bounty-card"
        @click="$router.push(`/bounty/${b.id}`)"
      >
        <div class="bounty-top">
          <span :class="['bounty-status', statusClass(b.status)]">
            {{ statusLabel(b.status) }}
          </span>
          <span class="bounty-deadline" v-if="b.deadline">
            <Icon icon="mdi:calendar" /> {{ b.deadline }}
          </span>
        </div>

        <h3 class="bounty-title">{{ b.title }}</h3>
        <p class="bounty-desc">
          {{ b.description?.substring(0, 100) }}{{ b.description?.length > 100 ? '...' : '' }}
        </p>

        <div class="bounty-footer">
          <div class="bounty-reward">
            <Icon icon="mdi:star" class="reward-icon" />
            <span class="reward-amount">{{ b.reward }}</span>
            <span class="reward-unit">时间币</span>
          </div>
          <div class="bounty-user" v-if="b.userName">
            <span class="user-avatar-sm">{{ b.userName?.charAt(0) }}</span>
            <span>{{ b.userName }}</span>
          </div>
        </div>
      </article>
    </div>

    <!-- 分页 -->
    <div v-if="bounties.length >= 20" class="pagination-wrap">
      <el-pagination layout="prev, pager, next" :page-size="20" :total="100" background />
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
  margin: 0 0 4px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.publish-btn {
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

/* 筛选 */
.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}
.filter-tag {
  padding: 6px 18px;
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

/* 悬赏网格 */
.bounty-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

/* 卡片 */
.bounty-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  border: 1px solid #f0e8e0;
  transition: all 0.3s ease;
}
.bounty-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border-color: rgba(232,120,74,0.2);
}
.bounty-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.bounty-status {
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.st-pub { background: #fdf6ec; color: #e6a23c; }
.st-taken { background: #ecf5ff; color: #409eff; }
.st-done { background: #e8f5e9; color: #4caf50; }
.st-exp { background: #f5f5f5; color: #999; }

.bounty-deadline {
  font-size: 12px;
  color: #bbb;
}
.bounty-title {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
}
.bounty-desc {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
  margin: 0 0 16px;
}
.bounty-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f5f0eb;
}
.bounty-reward {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.reward-icon { font-size: 16px; color: #f0a060; flex-shrink: 0; }
.reward-amount {
  font-size: 22px;
  font-weight: 800;
  color: #e8784a;
}
.reward-unit {
  font-size: 12px;
  color: #999;
}
.bounty-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
}
.user-avatar-sm {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
