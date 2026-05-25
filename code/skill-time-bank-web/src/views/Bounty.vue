<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { getBountyList, getCategories } from '../api/skill'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Icon } from '@iconify/vue'

const router = useRouter()
const userStore = useUserStore()
const bounties = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(6)
const total = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const keyword = ref('')
const categoryId = ref(null)
let searchTimer = null

function equalizeCardHeights() {
  nextTick(() => {
    const cards = document.querySelectorAll('.bounty-card')
    if (cards.length === 0) return
    cards.forEach(c => c.style.height = 'auto')
    let maxH = 0
    cards.forEach(c => { maxH = Math.max(maxH, c.offsetHeight) })
    cards.forEach(c => { c.style.height = maxH + 'px' })
  })
}

const categoryCovers = {
  '编程开发': { icon: 'mdi:code-braces', color: '#4361ee' },
  '设计创意': { icon: 'mdi:palette-swatch-outline', color: '#e8784a' },
  '语言学习': { icon: 'mdi:translate', color: '#4caf50' },
  '音乐艺术': { icon: 'mdi:music-clef-treble', color: '#e91e63' },
  '运动健身': { icon: 'mdi:run-fast', color: '#ff9800' },
  '学术辅导': { icon: 'mdi:school-outline', color: '#2196f3' },
  '生活技能': { icon: 'mdi:hand-heart-outline', color: '#9c27b0' },
  '职场咨询': { icon: 'mdi:briefcase-account-outline', color: '#607d8b' },
}
function categoryCover(catName) {
  return categoryCovers[catName] || { icon: 'mdi:star', color: '#e8784a' }
}

onMounted(async () => {
  await Promise.all([loadBounties(), loadCategories()])
})

onUnmounted(() => {
  if (searchTimer) clearTimeout(searchTimer)
})

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch { /* silent */ }
}

async function loadBounties() {
  loading.value = true
  try {
    const res = await getBountyList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || null,
      categoryId: categoryId.value || null
    })
    bounties.value = res.data?.records || []
    total.value = Number(res.data?.total) || 0
  } finally {
    loading.value = false
    equalizeCardHeights()
  }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadBounties()
  }, 400)
}

function onCategoryChange(catId) {
  categoryId.value = categoryId.value === catId ? null : catId
  page.value = 1
  loadBounties()
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadBounties()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadBounties()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function displayUser(b) {
  // 当前用户是发布者 → 显示申请人；否则显示发布者
  if (userStore.userId && b.userId === userStore.userId) {
    return { name: b.applicantName, avatar: b.applicantAvatar }
  }
  return { name: b.userName, avatar: b.userAvatar }
}

function formatDeadline(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
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
        <Icon icon="mdi:plus-circle" /> 发布悬赏
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <Icon icon="mdi:magnify" class="search-icon" />
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索悬赏需求..."
        @input="onSearchInput"
      />
      <span v-if="total > 0" class="search-count">共 {{ total }} 个悬赏</span>
    </div>

    <!-- 分类筛选 -->
    <div class="category-bar" v-if="categories.length > 0">
      <button
        :class="['cat-tag', { active: categoryId === null }]"
        @click="categoryId = null; page = 1; loadBounties()"
      >全部</button>
      <button
        v-for="c in categories"
        :key="c.id"
        :class="['cat-tag', { active: categoryId === c.id }]"
        @click="onCategoryChange(c.id)"
      >
        <Icon :icon="categoryCover(c.name).icon" class="cat-icon" />
        {{ c.name }}
      </button>
    </div>

    <!-- 悬赏列表 + 左右翻页 -->
    <div class="bounty-list-wrapper">
      <button
        class="nav-arrow nav-left"
        :disabled="page <= 1"
        :class="{ disabled: page <= 1 }"
        @click="prevPage"
      >
        <Icon icon="mdi:chevron-left" />
      </button>

      <div class="bounty-grid" v-loading="loading">
        <el-empty v-if="!loading && bounties.length === 0" description="暂无悬赏需求" :image-size="100" />

        <article
          v-for="b in bounties"
          :key="b.id"
          class="bounty-card"
          @click="$router.push(`/bounty/${b.id}`)"
        >
          <div class="bounty-top">
            <span class="bounty-deadline" v-if="b.deadline">
              <Icon icon="mdi:clock-outline" /> {{ formatDeadline(b.deadline) }} 截止
            </span>
            <span class="bounty-deadline" v-else>长期有效</span>
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
            <div class="bounty-meta">
              <span v-if="b.categoryName" class="bounty-cat-tag" :style="{ color: categoryCover(b.categoryName).color, background: categoryCover(b.categoryName).color + '14' }">
                <Icon :icon="categoryCover(b.categoryName).icon" class="meta-icon" />
                {{ b.categoryName }}
              </span>
              <div class="bounty-user" v-if="displayUser(b).name">
                <img v-if="displayUser(b).avatar" :src="displayUser(b).avatar" class="user-avatar-sm" />
                <span v-else class="user-avatar-sm">{{ displayUser(b).name?.charAt(0) }}</span>
                <span>{{ displayUser(b).name }}</span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <button
        class="nav-arrow nav-right"
        :disabled="page >= totalPages"
        :class="{ disabled: page >= totalPages }"
        @click="nextPage"
      >
        <Icon icon="mdi:chevron-right" />
      </button>
    </div>

    <!-- 页码指示 -->
    <div class="page-indicator" v-if="total > 0">
      第 {{ page }} / {{ totalPages }} 页
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

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #e8e0d8;
  border-radius: 12px;
  padding: 0 18px;
  height: 48px;
  margin-bottom: 16px;
  transition: all 0.3s;
}
.search-bar:focus-within {
  border-color: #e8784a;
  box-shadow: 0 0 0 3px rgba(232,120,74,0.08);
}
.search-icon { font-size: 20px; color: #bbb; flex-shrink: 0; }
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
}
.search-input::placeholder { color: #ccc; }
.search-count { font-size: 13px; color: #bbb; flex-shrink: 0; }

/* 分类筛选 */
.category-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}
.cat-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 16px;
  border: 1px solid #e8e0d8;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.cat-tag:hover {
  border-color: #e8784a;
  color: #e8784a;
}
.cat-tag.active {
  background: #e8784a;
  color: #fff;
  border-color: #e8784a;
  font-weight: 600;
}
.cat-icon { font-size: 16px; flex-shrink: 0; }

/* 列表容器 + 左右箭头 */
.bounty-list-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.nav-arrow {
  flex-shrink: 0;
  width: 36px;
  height: 100px;
  border-radius: 8px;
  border: 1px solid #e8e0d8;
  background: #fff;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s;
  font-size: 22px;
  z-index: 2;
}
.nav-arrow:hover:not(.disabled) {
  background: #e8784a;
  border-color: #e8784a;
  color: #fff;
  box-shadow: 0 4px 12px rgba(232,120,74,0.25);
  transform: scale(1.08);
}
.nav-arrow.disabled {
  opacity: 0.3;
  cursor: not-allowed;
  pointer-events: none;
}

/* 悬赏网格 */
.bounty-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  flex: 1;
}
/* 确保同一行卡片等高（浏览器默认 align-items: stretch） */
.bounty-grid :deep(.el-empty) {
  grid-column: 1 / -1;
}

/* 页码指示 */
.page-indicator {
  text-align: center;
  font-size: 13px;
  color: #aaa;
}

/* 卡片 */
.bounty-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  border: 1px solid #f0e8e0;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  min-height: 220px;
  box-sizing: border-box;
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
.bounty-deadline {
  font-size: 12px;
  color: #bbb;
}
.bounty-title {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bounty-desc {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
  margin: 0 0 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.bounty-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f5f0eb;
  flex-wrap: nowrap;
  overflow: hidden;
  gap: 8px;
  flex-shrink: 0;
}
.bounty-reward {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-shrink: 0;
}
.reward-icon { font-size: 16px; color: #f0a060; flex-shrink: 0; }
.reward-amount { font-size: 22px; font-weight: 800; color: #e8784a; }
.reward-unit { font-size: 12px; color: #999; }
.bounty-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  flex-shrink: 1;
  min-width: 0;
}
.bounty-cat-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.meta-icon { font-size: 14px; }
.bounty-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
  overflow: hidden;
  white-space: nowrap;
  min-width: 0;
}
.bounty-user span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  object-fit: cover;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .nav-arrow { width: 36px; height: 36px; font-size: 18px; }
  .bounty-list-wrapper { gap: 8px; }
  .bounty-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .nav-left, .nav-right { display: none; }
  .bounty-grid { grid-template-columns: 1fr; }
  .bounty-footer { flex-direction: column; align-items: flex-start; }
}
</style>
