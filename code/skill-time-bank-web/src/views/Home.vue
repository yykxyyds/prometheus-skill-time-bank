<script setup>
import { ref, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getSkillList, getCategories } from '../api/skill'
import api from '../api/index'
import { Icon } from '@iconify/vue'

const router = useRouter()
const userStore = useUserStore()

const skills = ref([])
const categories = ref([])
const loading = ref(false)
const query = ref({ page: 1, size: 20, categoryId: null, keyword: '', sort: '' })
const total = ref(0)
const currentPage = ref(1)
const heroStats = ref({ skillCount: 0, userCount: 0, orderCount: 0 })
const announcements = ref([])
const showAnnouncePopup = ref(false)

const CARDS_PER_ROW = 5
const totalPages = computed(() => Math.ceil(total.value / query.value.size) || 1)

// 把技能按行分组，每行 CARDS_PER_ROW 个 → 20/5 = 4 行
const skillRows = computed(() => {
  const rows = []
  for (let i = 0; i < skills.value.length; i += CARDS_PER_ROW) {
    rows.push(skills.value.slice(i, i + CARDS_PER_ROW))
  }
  return rows
})

async function loadData() {
  loading.value = true
  try {
    query.value.page = currentPage.value
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

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadData()
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
  currentPage.value = 1
  await loadData()
}

onMounted(async () => {
  await loadData()
  await loadAnnouncements()
})
onActivated(loadData)

const categoryCovers = {
  '编程开发': { icon: 'mdi:code-braces', color: '#4361ee', emoji: '💻' },
  '设计创意': { icon: 'mdi:palette-swatch-outline', color: '#e8784a', emoji: '🎨' },
  '语言学习': { icon: 'mdi:translate', color: '#4caf50', emoji: '🌍' },
  '音乐艺术': { icon: 'mdi:music-clef-treble', color: '#e91e63', emoji: '🎵' },
  '运动健身': { icon: 'mdi:run-fast', color: '#ff9800', emoji: '💪' },
  '学术辅导': { icon: 'mdi:school-outline', color: '#2196f3', emoji: '🎓' },
  '生活技能': { icon: 'mdi:hand-heart-outline', color: '#9c27b0', emoji: '🏠' },
  '职场咨询': { icon: 'mdi:briefcase-account-outline', color: '#607d8b', emoji: '💼' },
}
function categoryCover(catName) {
  return categoryCovers[catName] || { icon: 'mdi:star', color: '#e8784a', emoji: '⭐' }
}

// 根据技能标题匹配强相关 emoji
function getSkillEmoji(title) {
  if (!title) return null
  const map = [
    ['Python', '🐍'], ['React', '⚛️'], ['Vue', '💚'], ['Spring', '🍃'],
    ['Go', '🔵'], ['SQL', '🗄️'], ['C4D', '🧊'], ['PR', '🎬'], ['Figma', '🖼️'],
    ['TypeScript', '💙'], ['Node', '🟢'], ['Linux', '🐧'], ['Docker', '🐋'],
    ['瑜伽', '🧘'], ['健身', '🏋️'], ['HIIT', '🏃'], ['燃脂', '🔥'],
    ['马拉松', '🏅'], ['运动', '🚴'], ['拉伸', '🧘'], ['增肌', '💪'],
    ['日语', '🗾'], ['韩语', '🇰🇷'], ['法语', '🥐'], ['德语', '🍺'],
    ['英语', '📖'], ['雅思', '🎯'], ['商务英语', '💼'], ['写作', '✍️'],
    ['吉他', '🎸'], ['尤克里里', '🪕'], ['钢琴', '🎹'], ['音乐', '🎵'],
    ['电影', '🎥'], ['配乐', '🎼'], ['弹唱', '🎤'],
    ['咖啡', '☕'], ['手冲', '☕'], ['拿铁', '☕'], ['拉花', '☕'],
    ['烹饪', '🍳'], ['家常菜', '🥘'], ['烘焙', '🧁'], ['营养', '🥗'],
    ['插画', '🖌️'], ['绘画', '🖼️'], ['设计', '✏️'], ['摄影', '📷'],
    ['概念设计', '🎮'], ['角色', '🎮'], ['B端', '🖥️'], ['UI', '📱'],
    ['税务', '💰'], ['PMP', '📋'], ['投资', '📈'], ['理财', '💎'],
    ['简历', '📄'], ['面试', '🤝'], ['项目管理', '📋'], ['职场', '💼'],
    ['数学', '📐'], ['考研', '🎓'], ['法考', '⚖️'], ['线性代数', '📐'],
    ['高等数学', '📐'], ['学术', '📚'], ['考试', '📝'], ['冲刺', '📝'],
    ['小红书', '📱'], ['运营', '📊'], ['自媒体', '🎯'], ['文案', '📝'],
    ['动漫', '🎌'], ['零基础', '🌱'], ['入门', '🚪'], ['进阶', '📈'],
    ['实战', '⚔️'], ['速成', '⚡'], ['教学', '📖'], ['辅导', '📝'],
    ['计划', '📋'], ['训练', '🎯'], ['课程', '📚'], ['技巧', '💡'],
    ['指南', '🧭'], ['攻略', '🗺️'], ['秘籍', '📜'], ['认证', '🏆'],
    ['数据分析', '📊'], ['编程', '💻'], ['开发', '🛠️'],
  ]
  for (const [key, emoji] of map) {
    if (title.includes(key)) return emoji
  }
  return null
}
</script>

<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="highlight highlight-lg">学</span>你想学，<span class="highlight highlight-lg">教</span>你想教
        </h1>
        <p class="hero-subtitle">
          一个用时间币串起技能供需的互助社区。
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
        <!-- 设计 --><div class="bubble b1" style="color:#B0A3D4"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 3a9 9 0 0 0 0 18c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 5-5 0-4.42-4.03-8-9-8zM6.5 12c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/></svg></div>
        <!-- 编程 --><div class="bubble b2" style="color:#8BAAC4"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M14.6 16.6l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4zm-5.2 0L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4z"/></svg></div>
        <!-- 吉他 --><div class="bubble b3" style="color:#D4A585"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M19.5 2.5L22 5l-1.5 1.5L19 5l-2 2 1.5 1.5L17 10l-2-2-1.4 1.4A5.5 5.5 0 0 0 11 10.5V12H9v1c0 1.1-.9 2-2 2H6v1a3 3 0 0 0 3 3h1.5a5.5 5.5 0 0 0 2.6-6.4L14.5 11l2 2 1.5-1.5L16.5 10l2-2L20 9.5 21.5 8l-2-2L21 4.5 19.5 3 18 4.5 19.5 6l-1.5 1.5-1.5-1.5-2 2L13 6.5 14.5 5l-1.5-1.5L11.5 5 9.5 3 8 4.5 10.5 7l-1 1c-1.1-1.1-2.5-2-4.5-2C3.5 6 2 7.5 2 9.5S3.5 13 5.5 13c1.2 0 2.2-.5 3-1.2l2.5 2.5 1.5-1.5L10 10.5h.5a3.5 3.5 0 0 1 1.1-1.4L13 10.5l1.5-1.5-2-2z"/></svg></div>
        <!-- 翻译 --><div class="bubble b4" style="color:#8EBF9E"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M12.87 15.07l-2.54-2.51.03-.03A17.52 17.52 0 0 0 14.07 6H17V4h-7V2H8v2H1v2h11.17C11 7.92 9.87 9.63 9 11.35 8.07 9.58 6.89 8 5.41 6.59L4 8c1.65 1.53 3.03 3.22 4.16 5.07-1.08.32-2.29.68-3.66 1.07l1 1.87c1.32-.37 2.51-.73 3.57-1.05l2.54 2.54L13 19h2v-3l3.5 3.5L20 18l-4.5-4.5L13 16v-2z"/></svg></div>
        <!-- 咖啡 --><div class="bubble b5" style="color:#C4AA7A"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M2 21V19h20v2H2zm18-4H4V8c0-2.21 1.79-4 4-4h7c2.21 0 4 1.79 4 4v1h1c1.65 0 3 1.35 3 3v.5c0 1.65-1.35 3-3 3h-1v1zm0-3h1c.55 0 1-.45 1-1v-.5c0-.55-.45-1-1-1h-1v2.5z"/></svg></div>
        <!-- 瑜伽 --><div class="bubble b6" style="color:#D49B9B"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 2a2 2 0 0 1 2 2 2 2 0 0 1-2 2 2 2 0 0 1-2-2 2 2 0 0 1 2-2zm4.5 4h-3l-1 4.2L15 13v6h-2v-5l-1-1.5L10 19H7l3-7.5-1-2.5c-.5.8-1.3 1.5-2.5 2V14H5V9.5c1.5-.6 2.5-1.5 3-2.5.5-1 1-1.5 1.5-2h7c.6 0 1 .4 1 1s-.4 1-1 1z"/></svg></div>
        <!-- 数学 --><div class="bubble b7" style="color:#9F9BCF"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M7 2h10v2H7V2zm0 4h2v2H7V6zm4 0h6v2h-6V6zm-4 4h2v2H7v-2zm4 0h6v2h-6v-2zm-4 4h2v2H7v-2zm4 0h6v2h-6v-2zm-4 4h2v2H7v-2zm4 0h6v2h-6v-2zM3 2h2v20H3V2zm16 0h2v20h-2V2z"/></svg></div>
        <!-- 摄影 --><div class="bubble b8" style="color:#8EB4BF"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M4 4h3l2-2h6l2 2h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm8 3a5 5 0 0 0-5 5 5 5 0 0 0 5 5 5 5 0 0 0 5-5 5 5 0 0 0-5-5zm0 2a3 3 0 0 1 3 3 3 3 0 0 1-3 3 3 3 0 0 1-3-3 3 3 0 0 1 3-3z"/></svg></div>
        <!-- 烹饪 --><div class="bubble b9" style="color:#D49B9F"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M6 1h2v6.6c-.6.3-1.2.7-1.7 1.1L6 1zm4 .1l.5 4.4c-.7.1-1.4.4-2 .7L8 1.3 10 1.1zm4 0l-.5 5.2c.7-.1 1.4-.3 2-.6L16 1.3l-2-.2zM18 1l-.5 6c.5.2 1 .5 1.5.8L19.5 1H18zM5 19h14v2H5v-2zm1-6h12s-1 5-6 5-6-5-6-5z"/></svg></div>
        <!-- 演讲 --><div class="bubble b10" style="color:#B5A3D0"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-3 3 3 3 0 0 1-3-3V5a3 3 0 0 1 3-3zm7 8a1 1 0 0 1 1 1 8 8 0 0 1-6.5 7.87V21H16v2H8v-2h2.5v-2.13A8 8 0 0 1 4 11a1 1 0 0 1 2 0 6 6 0 0 0 12 0 1 1 0 0 1 1-1z"/></svg></div>
        <!-- 图表 --><div class="bubble b11" style="color:#8FBFAF"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M16 11.78l4.24-4.24 1.41 1.41L16 14.6l-4.24-4.24-5.66 5.66-1.41-1.41L11.35 8l4.65 3.78zM4 2h16a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm0 2v16h16V4H4z"/></svg></div>
        <!-- 绘画 --><div class="bubble b12" style="color:#BFA87A"><svg viewBox="0 0 24 24"><path fill="currentColor" d="M18.5 1.15c-.53 0-1.07.2-1.47.6l-1.44 1.44 3.22 3.22 1.44-1.44c.4-.4.6-.94.6-1.47 0-.53-.2-1.07-.6-1.47a2.05 2.05 0 0 0-1.47-.6h-.24l-.04-.08zM14.19 4.4L2 16.59V22h5.41L19.6 9.81 14.19 4.4z"/></svg></div>
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

    <!-- 技能滚动行 -->
    <section class="skills-section" v-loading="loading">
      <div class="section-header">
        <div>
          <h2>技能广场</h2>
          <span class="section-sub">发现身边的高手</span>
        </div>
        <button class="publish-btn" v-if="userStore.isLoggedIn" @click="router.push('/my-skills')">
          <Icon icon="mdi:plus-circle" /> 发布技能
        </button>
      </div>

      <el-empty
        v-if="!loading && skills.length === 0"
        description="暂未找到相关技能，换个关键词试试"
        :image-size="120"
      />

      <div v-else class="scroll-rows">
        <div
          v-for="(row, rowIdx) in skillRows"
          :key="rowIdx"
          class="scroll-row"
        >
          <div class="scroll-fade scroll-fade-left" />
          <div class="scroll-fade scroll-fade-right" />
          <div
            class="scroll-track"
            :class="rowIdx % 2 === 0 ? 'scroll-left' : 'scroll-right'"
            :style="{ animationDuration: row.length * 5 + 's' }"
          >
            <!-- 原始卡片 -->
            <article
              v-for="skill in row"
              :key="skill.id"
              class="skill-card"
              @click="$router.push(`/skill/${skill.id}`)"
            >
              <div
                class="card-cover"
                :style="skill.coverImage
                  ? { backgroundImage: `url(${skill.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
                  : { background: `linear-gradient(135deg, ${categoryCover(skill.categoryName).color}18, ${categoryCover(skill.categoryName).color}30)` }"
              >
                <template v-if="!skill.coverImage">
                  <span class="cover-emoji">{{ getSkillEmoji(skill.title) || categoryCover(skill.categoryName).emoji }}</span>
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
            <!-- 复制一份实现无缝循环 -->
            <article
              v-for="skill in row"
              :key="'dup-' + skill.id"
              class="skill-card"
              @click="$router.push(`/skill/${skill.id}`)"
            >
              <div
                class="card-cover"
                :style="skill.coverImage
                  ? { backgroundImage: `url(${skill.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }
                  : { background: `linear-gradient(135deg, ${categoryCover(skill.categoryName).color}18, ${categoryCover(skill.categoryName).color}30)` }"
              >
                <template v-if="!skill.coverImage">
                  <span class="cover-emoji">{{ getSkillEmoji(skill.title) || categoryCover(skill.categoryName).emoji }}</span>
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
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-bar" v-if="totalPages > 1">
        <button class="page-btn" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <template v-for="p in totalPages" :key="p">
          <button v-if="p <= 5 || p > totalPages - 2 || Math.abs(p - currentPage) <= 1"
            :class="['page-btn', { active: p === currentPage }]" @click="goPage(p)">{{ p }}</button>
          <span v-else-if="p === 6 || p === totalPages - 2" class="page-dots">...</span>
        </template>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
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
.highlight-lg {
  font-size: 56px;
  font-weight: 900;
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

/* 浮动气泡 */
.hero-visual {
  position: relative;
  width: 400px;
  height: 300px;
  flex-shrink: 0;
}

.bubble {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1), 0 0 0 3px rgba(255,255,255,0.7);
  animation: bobble var(--dur) ease-in-out infinite;
  animation-delay: var(--delay);
  cursor: default;
  transition: transform 0.25s cubic-bezier(.34,1.56,.64,1), box-shadow 0.25s;
  z-index: 1;
  color: #888;
}
.bubble svg {
  width: var(--sz);
  height: var(--sz);
  color: #fff;
}
.bubble:hover {
  transform: scale(1.3) !important;
  box-shadow: 0 8px 30px rgba(0,0,0,0.18), 0 0 0 6px rgba(255,255,255,0.8);
  z-index: 20;
}
.bubble svg {
  width: var(--sz);
  height: var(--sz);
}

/* 气泡颜色已在模板 inline style 中 */

/* 12 个气泡 — 随机散落，无规律不对称 */
.b1  { --sz:28px; width:56px; height:56px; --dur:4.2s; --delay:0.0s; top:23px;   left:37px;   }
.b2  { --sz:30px; width:60px; height:60px; --dur:5.4s; --delay:0.4s; top:68px;   left:312px;  }
.b3  { --sz:24px; width:44px; height:44px; --dur:3.6s; --delay:0.8s; top:133px;  left:7px;    }
.b4  { --sz:32px; width:64px; height:64px; --dur:6.0s; --delay:1.2s; top:12px;   left:178px;  }
.b5  { --sz:22px; width:40px; height:40px; --dur:4.6s; --delay:1.6s; top:188px;  left:342px;  }
.b6  { --sz:34px; width:68px; height:68px; --dur:5.8s; --delay:2.0s; top:92px;   left:128px;  }
.b7  { --sz:26px; width:48px; height:48px; --dur:3.4s; --delay:2.4s; top:243px;  left:18px;   }
.b8  { --sz:28px; width:52px; height:52px; --dur:5.0s; --delay:2.8s; top:157px;  left:292px;  }
.b9  { --sz:32px; width:60px; height:60px; --dur:4.8s; --delay:3.2s; top:47px;   left:242px;  }
.b10 { --sz:24px; width:44px; height:44px; --dur:4.0s; --delay:3.6s; top:223px;  left:348px;  }
.b11 { --sz:30px; width:56px; height:56px; --dur:5.6s; --delay:4.0s; top:252px;  left:105px;  }
.b12 { --sz:26px; width:48px; height:48px; --dur:3.8s; --delay:4.4s; top:178px;  left:82px;   }

@keyframes bobble {
  0%, 100% { transform: translateY(0) translateX(0) rotate(0deg); }
  25%  { transform: translateY(-10px) translateX(4px) rotate(2deg); }
  50%  { transform: translateY(-4px) translateX(-3px) rotate(-1deg); }
  75%  { transform: translateY(-14px) translateX(2px) rotate(1deg); }
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
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.section-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px;
}
.section-sub {
  font-size: 14px;
  color: #bbb;
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
  flex-shrink: 0;
}
.publish-btn:hover {
  box-shadow: 0 4px 14px rgba(232,120,74,0.3);
  transform: translateY(-1px);
}

/* ========== 滚动行容器 ========== */
.scroll-rows {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.scroll-row {
  position: relative;
  overflow: hidden;
}
/* 左右渐变遮罩 */
.scroll-fade {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 80px;
  z-index: 2;
  pointer-events: none;
}
.scroll-fade-left {
  left: 0;
  background: linear-gradient(to right, #faf8f5 0%, transparent 100%);
}
.scroll-fade-right {
  right: 0;
  background: linear-gradient(to left, #faf8f5 0%, transparent 100%);
}

/* 滚动轨道 */
.scroll-track {
  display: flex;
  gap: 20px;
  width: max-content;
}
.scroll-track:hover {
  animation-play-state: paused;
}

@keyframes scrollLeft {
  0%   { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
@keyframes scrollRight {
  0%   { transform: translateX(-50%); }
  100% { transform: translateX(0); }
}

.scroll-left {
  animation: scrollLeft 30s linear infinite;
}
.scroll-right {
  animation: scrollRight 30s linear infinite;
}

/* 卡片 */
.skill-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #f0e8e0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  width: 260px;
}
.skill-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
  border-color: rgba(232, 120, 74, 0.2);
}

/* 卡片封面 */
.card-cover {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.cover-emoji {
  font-size: 56px;
  transition: transform 0.3s ease;
  filter: grayscale(0.15);
}
.skill-card:hover .cover-emoji {
  transform: scale(1.15);
}
.cover-category {
  position: absolute;
  top: 10px;
  right: 12px;
  background: rgba(0,0,0,0.12);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

/* 分页 */
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 32px;
}
.page-btn {
  min-width: 38px;
  height: 38px;
  padding: 0 12px;
  border: 1px solid #e8e0d8;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) {
  border-color: #e8784a;
  color: #e8784a;
}
.page-btn.active {
  background: #e8784a;
  color: #fff;
  border-color: #e8784a;
  font-weight: 600;
}
.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.page-dots {
  padding: 0 4px;
  color: #bbb;
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
