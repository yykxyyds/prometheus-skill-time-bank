<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { getSkillList, getCategories } from '../api/skill'
import api from '../api/index'
import { Icon } from '@iconify/vue'
import { useScrollReveal } from '../composables/useScrollReveal'
import { ElMessage } from 'element-plus'

const skills = ref([])
const categories = ref([])
const loading = ref(false)
const query = ref({ page: 1, size: 12, categoryId: null, keyword: '', sort: '' })
const total = ref(0)
const heroStats = ref({ skillCount: 0, userCount: 0, orderCount: 0 })
const announcements = ref([])
const showAnnouncePopup = ref(false)

useScrollReveal('.skill-card', { stagger: 80 })

async function loadData() {
  loading.value = true
  try {
    const [skillRes, catRes] = await Promise.all([
      getSkillList(query.value),
      getCategories()
    ])
    skills.value = skillRes.data?.records || []
    total.value = skillRes.data?.total || 0
    categories.value = catRes.data || []
    heroStats.value = {
      skillCount: total.value,
      userCount: 128,
      orderCount: 567
    }
  } finally {
    loading.value = false
  }
}

async function loadAnnouncements() {
  try {
    const res = await api.get('/announcement/list', { params: { page: 1, size: 50 } })
    announcements.value = res.data?.records || []
    if (announcements.value.length > 0) {
      const today = new Date().toDateString()
      const lastShown = localStorage.getItem('announce_last_shown')
      if (lastShown !== today) {
        showAnnouncePopup.value = true
      }
    }
  } catch (e) { /* silent */ }
}

function closeAnnouncePopup() {
  showAnnouncePopup.value = false
  localStorage.setItem('announce_last_shown', new Date().toDateString())
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function search() {
  query.value.page = 1
  await loadData()
}

function handlePageChange(page) {
  query.value.page = page
  loadData()
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

onMounted(async () => {
  await loadData()
  await loadAnnouncements()
})
onActivated(loadData)

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
</script>

<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">
          用<span class="highlight">技能</span>交换<span class="highlight">时间</span>
        </h1>
        <p class="hero-subtitle">
          拒绝金钱交易，回归价值交换。在这里，每个人的技能都值得被看见。
        </p>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">{{ heroStats.skillCount }}</span>
            <span class="stat-label">项技能</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ heroStats.userCount }}</span>
            <span class="stat-label">位用户</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ heroStats.orderCount }}</span>
            <span class="stat-label">笔交易</span>
          </div>
        </div>
      </div>
      <div class="hero-visual">
        <div class="floating-card card-1"><Icon icon="mdi:palette" class="fc-icon" /> 插画设计</div>
        <div class="floating-card card-2"><Icon icon="mdi:laptop" class="fc-icon" /> Python编程</div>
        <div class="floating-card card-3"><Icon icon="mdi:music" class="fc-icon" /> 吉他教学</div>
        <div class="floating-card card-4"><Icon icon="mdi:book-open-page-variant" class="fc-icon" /> 英语翻译</div>
      </div>
    </section>

    <!-- 搜索栏 -->
    <section class="search-section">
      <div class="search-bar">
        <div class="search-input-wrap">
          <Icon icon="mdi:magnify" class="search-icon" />
          <input
            v-model="query.keyword"
            type="text"
            placeholder="搜索你需要的技能..."
            class="search-input"
            @keyup.enter="search"
          />
        </div>
        <el-select
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          @change="search"
          class="category-select"
          size="large"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
        <button class="search-btn" @click="search">搜索</button>
      </div>
    </section>

    <!-- 技能网格 -->
    <section class="skills-section" v-loading="loading">
      <div class="section-header">
        <h2>技能广场</h2>
        <span class="section-sub">发现身边的高手</span>
      </div>

      <el-empty
        v-if="!loading && skills.length === 0"
        description="暂未找到相关技能，换个关键词试试"
        :image-size="120"
      />

      <div v-else class="skill-grid">
        <article
          v-for="(skill, idx) in skills"
          :key="skill.id"
          class="skill-card reveal-on-scroll"
          @click="$router.push(`/skill/${skill.id}`)"
        >
          <div
            class="card-cover"
            :style="skill.coverImage
              ? { backgroundImage: `url(${skill.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
              : { background: categoryCover(skill.categoryName).color + '10' }"
          >
            <template v-if="!skill.coverImage">
              <Icon :icon="categoryCover(skill.categoryName).icon" class="cover-cat-icon" :style="{ color: categoryCover(skill.categoryName).color }" />
              <span class="cover-category">{{ skill.categoryName || '技能' }}</span>
            </template>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ skill.title }}</h3>
            <p class="card-desc">
              {{ skill.description?.substring(0, 60) }}{{ skill.description?.length > 60 ? '...' : '' }}
            </p>
            <div class="card-footer">
              <span class="card-price">
                <Icon icon="mdi:star" class="price-icon" />
                {{ skill.price }} 币/时
              </span>
              <span class="card-user">{{ skill.userName || '匿名' }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 分页 -->
      <div v-if="total > query.size" class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <!-- 首页公告弹窗 -->
    <el-dialog v-model="showAnnouncePopup" title="📢 平台公告" width="560px" top="8vh" destroy-on-close>
      <div class="announce-popup-list">
        <article v-for="item in announcements" :key="item.id" class="popup-announce-item">
          <h3 class="popup-announce-title">
            <span v-if="item.isTop" class="popup-top-badge">置顶</span>
            {{ item.title }}
          </h3>
          <p class="popup-announce-content">{{ item.content }}</p>
          <span class="popup-announce-time">{{ formatTime(item.createTime) }}</span>
        </article>
      </div>
      <template #footer>
        <el-button type="primary" @click="closeAnnouncePopup()">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ========== 英雄区域 ========== */
.hero {
  background: linear-gradient(160deg, #fff8f4 0%, #fff 40%, #faf8f5 100%);
  border-radius: 20px;
  padding: 56px 48px 48px;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(232, 120, 74, 0.08);
}
.hero::before {
  content: '';
  position: absolute;
  top: -80px;
  right: -80px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(240, 160, 96, 0.12) 0%, transparent 70%);
  border-radius: 50%;
}
.hero-content {
  flex: 1;
  z-index: 1;
}
.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1.3;
  margin: 0 0 16px;
  letter-spacing: -1px;
}
.highlight {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-subtitle {
  font-size: 16px;
  color: #888;
  line-height: 1.6;
  margin: 0 0 32px;
  max-width: 420px;
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 28px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-num {
  font-size: 32px;
  font-weight: 800;
  color: #e8784a;
  line-height: 1;
}
.stat-label {
  font-size: 13px;
  color: #999;
}
.stat-divider {
  width: 1px;
  height: 36px;
  background: #e8e0d8;
}

/* 浮动卡片 */
.hero-visual {
  position: relative;
  width: 320px;
  height: 240px;
  flex-shrink: 0;
}
.floating-card {
  position: absolute;
  padding: 10px 20px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  font-size: 14px;
  font-weight: 500;
  color: #555;
  animation: float 4s ease-in-out infinite;
  white-space: nowrap;
}
.card-1 { top: 10px; left: 20px; animation-delay: 0s; }
.card-2 { top: 60px; right: 0; animation-delay: 1s; }
.card-3 { top: 130px; left: 40px; animation-delay: 2s; }
.card-4 { top: 170px; right: 30px; animation-delay: 3s; }
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* ========== 搜索区域 ========== */
.search-section {
  margin-bottom: 36px;
}
.search-bar {
  display: flex;
  gap: 12px;
  background: #fff;
  padding: 8px;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  border: 1px solid #f0e8e0;
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
}
.search-icon {
  font-size: 20px;
  color: #bbb;
  flex-shrink: 0;
}
.fc-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #333;
  background: transparent;
  padding: 10px 0;
}
.search-input::placeholder {
  color: #ccc;
}
.category-select {
  width: 160px;
}
.search-btn {
  padding: 10px 28px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}
.search-btn:hover {
  box-shadow: 0 4px 16px rgba(232, 120, 74, 0.35);
  transform: translateY(-1px);
}

/* ========== 技能区域 ========== */
.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}
.section-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}
.section-sub {
  font-size: 14px;
  color: #bbb;
}

/* 卡片网格 */
.skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 20px;
}
.skill-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #f0e8e0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.skill-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
  border-color: rgba(232, 120, 74, 0.2);
}

/* 卡片封面 */
.card-cover {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.cover-cat-icon {
  font-size: 42px;
  opacity: 0.35;
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.skill-card:hover .cover-cat-icon {
  transform: scale(1.1);
  opacity: 0.5;
}
.cover-category {
  position: absolute;
  top: 12px;
  right: 14px;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 卡片内容 */
.card-body {
  padding: 16px 18px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
  line-height: 1.3;
}
.card-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  margin: 0 0 14px;
  min-height: 36px;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-price {
  font-size: 14px;
  font-weight: 700;
  color: #e8784a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.price-icon {
  font-size: 16px;
  color: #f0a060;
  flex-shrink: 0;
}
.card-user {
  font-size: 12px;
  color: #bbb;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

/* ========== 公告弹窗 ========== */
.announce-popup-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
}
.popup-announce-item {
  border-bottom: 1px solid #f0e8e0;
  padding-bottom: 14px;
}
.popup-announce-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.popup-announce-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.popup-top-badge {
  font-size: 11px;
  background: #e8784a;
  color: #fff;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.popup-announce-content {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  margin: 0 0 6px;
  white-space: pre-wrap;
}
.popup-announce-time {
  font-size: 12px;
  color: #bbb;
}
</style>
