<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getProfile, updateProfile, getUserProfile, uploadAvatar, getFollowStatus, followUser, unfollowUser, getFollowers, getFollowing, getReputation, getUserReviews } from '../../api/user'
import api from '../../api/index'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isSelf = computed(() => !route.params.id || route.params.id === String(userStore.userId))

// 关注状态
const followStatus = ref(null)
const followLoading = ref(false)

async function loadFollowStatus() {
  try {
    const res = await getFollowStatus(route.params.id)
    followStatus.value = res.data
  } catch { /* handled */ }
}

async function handleFollow() {
  followLoading.value = true
  try {
    if (followStatus.value?.isFollowing) {
      await unfollowUser(route.params.id)
      followStatus.value.isFollowing = false
      ElMessage.success('已取消关注')
      profile.value.followerCount = Math.max(0, (profile.value.followerCount || 1) - 1)
    } else {
      await followUser(route.params.id)
      followStatus.value.isFollowing = true
      ElMessage.success('关注成功')
      profile.value.followerCount = (profile.value.followerCount || 0) + 1
    }
  } catch { /* handled */ }
  finally { followLoading.value = false }
}

function goChat() {
  router.push(`/messages?userId=${route.params.id}`)
}

const profile = ref({})
const editing = ref(false)
const form = ref({ email: '', phone: '', bio: '' })
const loading = ref(false)
const uploading = ref(false)
const avatarInput = ref(null)

// 关注/粉丝
const followDialog = ref(false)
const followDialogTitle = ref('')
const followList = ref([])
const followListLoading = ref(false)

async function openFollowDialog(type) {
  followDialogTitle.value = type === 'followers' ? '粉丝' : '关注的人'
  followDialog.value = true
  followListLoading.value = true
  try {
    const userId = route.params.id || userStore.userId
    const res = type === 'followers' ? await getFollowers(userId) : await getFollowing(userId)
    followList.value = res.data || []
  } catch { followList.value = [] }
  finally { followListLoading.value = false }
}

// 雷达图
const reputation = ref({})
const reviews = ref([])
const radarLabels = ['按时', '沟通', '专业', '态度']
const radarMax = 5
const hoveredPoint = ref(null)

const pointCoords = computed(() => {
  const data = reputation.value.radarData
  if (!data || data.length === 0) return []
  const cx = 90, cy = 90, r = 65
  const angles = [-90, 0, 90, 180]
  return radarLabels.map((label, i) => {
    const item = data.find(d => d.tag === label)
    const score = item ? (item.score || 0) : 0
    const ratio = score / radarMax
    const rad = (angles[i] * Math.PI) / 180
    return { x: cx + r * ratio * Math.cos(rad), y: cy + r * ratio * Math.sin(rad), score, label }
  })
})

const radarPoints = computed(() => {
  const data = reputation.value.radarData
  if (!data || data.length === 0) return ''
  const cx = 90, cy = 90, r = 65
  const angles = [-90, 0, 90, 180] // top, right, bottom, left
  const labelOrder = ['按时', '沟通', '专业', '态度']
  return labelOrder.map((label, i) => {
    const item = data.find(d => d.tag === label)
    const score = item ? (item.score || 0) : 0
    const ratio = score / radarMax
    const rad = (angles[i] * Math.PI) / 180
    const x = cx + r * ratio * Math.cos(rad)
    const y = cy + r * ratio * Math.sin(rad)
    return `${x},${y}`
  }).join(' ')
})

const gridPolygons = computed(() => {
  const cx = 90, cy = 90, r = 65
  const angles = [-90, 0, 90, 180]
  return [0.2, 0.4, 0.6, 0.8, 1.0].map(level => {
    return angles.map(a => {
      const rad = (a * Math.PI) / 180
      return `${cx + r * level * Math.cos(rad)},${cy + r * level * Math.sin(rad)}`
    }).join(' ')
  })
})

const axisLabels = computed(() => {
  const cx = 90, cy = 90, r = 75
  const angles = [-90, 0, 90, 180]
  return angles.map((a, i) => {
    const rad = (a * Math.PI) / 180
    return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad), label: radarLabels[i] }
  })
})

// 评价统计计算
const reviewStats = computed(() => {
  const list = reviews.value
  const total = list.length
  if (total === 0) return { goodRate: 0, distribution: [], goodCount: 0, neutralCount: 0, badCount: 0 }

  const dist = [0, 0, 0, 0, 0] // 1星到5星
  let good = 0, neutral = 0, bad = 0
  list.forEach(r => {
    const s = r.score || 0
    dist[s - 1] = (dist[s - 1] || 0) + 1
    if (s >= 4) good++
    else if (s === 3) neutral++
    else bad++
  })

  return {
    goodRate: Math.round(good / total * 100),
    distribution: dist.map(c => ({ count: c, pct: Math.round(c / total * 100) })),
    goodCount: good,
    neutralCount: neutral,
    badCount: bad
  }
})

// ======== 时间银行（仅自己可见） ========
const balanceInfo = ref({ balance: 0, frozenBalance: 0, totalEarned: 0, totalSpent: 0 })
const transactions = ref([])

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

function reviewLabel(score) {
  if (score >= 4) return { text: '好评', cls: 'label-good' }
  if (score === 3) return { text: '中评', cls: 'label-neutral' }
  return { text: '差评', cls: 'label-bad' }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = isSelf.value
      ? await getProfile()
      : await getUserProfile(route.params.id)
    profile.value = res.data || {}
    form.value = {
      email: profile.value.email || '',
      phone: profile.value.phone || '',
      bio: profile.value.bio || ''
    }
  } finally {
    loading.value = false
  }
  if (!isSelf.value && userStore.isLoggedIn) {
    loadFollowStatus()
  }
  // 加载信誉雷达数据
  const userId = route.params.id || userStore.userId
  if (userId) {
    try {
      const repRes = await getReputation(userId)
      reputation.value = repRes.data || {}
    } catch { /* ignore */ }
    try {
      const revRes = await getUserReviews(userId)
      reviews.value = revRes.data || []
    } catch { /* ignore */ }
  }
  // 加载时间银行数据（仅自己可见）
  if (isSelf.value) {
    try {
      const [balRes, txRes] = await Promise.all([
        api.get('/wallet/balance'),
        api.get('/wallet/transactions', { params: { page: 1, size: 20 } })
      ])
      balanceInfo.value = balRes.data || {}
      transactions.value = txRes.data?.records || []
    } catch { /* ignore */ }
  }
})

async function save() {
  await updateProfile(form.value)
  ElMessage.success('资料已更新')
  editing.value = false
  const res = await getProfile()
  profile.value = res.data || {}
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function triggerUpload() {
  avatarInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('仅支持图片格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过5MB')
    return
  }
  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    const avatarUrl = res.data
    await updateProfile({ avatar: avatarUrl })
    profile.value.avatar = avatarUrl
    ElMessage.success('头像已更新')
  } catch { /* handled */ }
  finally { uploading.value = false }
  e.target.value = ''
}
</script>

<template>
  <div class="profile" v-loading="loading">
    <!-- 顶部个人信息卡片 -->
    <div class="profile-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="user-avatar" :class="{ clickable: isSelf, uploading }" @click="triggerUpload">
          <img v-if="profile.avatar" :src="profile.avatar" class="avatar-img" />
          <span v-else class="avatar-text">{{ (profile.username || userStore.username || '?').charAt(0).toUpperCase() }}</span>
          <div v-if="isSelf" class="avatar-overlay">
            <Icon icon="mdi:camera" class="camera-icon" />
          </div>
          <input ref="avatarInput" type="file" accept="image/*" hidden @change="handleFileChange" />
        </div>
        <div class="user-main">
          <div class="user-name-row">
            <h2>{{ isSelf ? userStore.username : (profile.username || '用户') }}</h2>
            <span class="role-badge" :class="{ admin: userStore.role === 'ADMIN' }">
              {{ userStore.role === 'ADMIN' ? '管理员' : '用户' }}
            </span>
          </div>
          <p class="user-bio">{{ profile.bio || '这个人很懒，什么都没写~' }}</p>
          <div class="user-meta">
            <span class="meta-item"><Icon icon="mdi:email" /> {{ profile.email || '未设置邮箱' }}</span>
            <span class="meta-item"><Icon icon="mdi:phone" /> {{ profile.phone || '未设置手机' }}</span>
            <span class="meta-item"><Icon icon="mdi:calendar" /> {{ profile.createTime || '-' }} 加入</span>
          </div>
        </div>
        <button
          v-if="isSelf && !editing"
          class="edit-btn"
          @click="editing = true"
        >
          编辑资料
        </button>
        <div v-if="!isSelf && userStore.isLoggedIn" class="profile-actions">
          <button class="action-btn follow-btn" :class="{ following: followStatus?.isFollowing }" :disabled="followLoading" @click="handleFollow">
            {{ followStatus?.isFollowing ? '已关注' : '关注' }}
          </button>
          <button class="action-btn msg-btn" @click="goChat">
            <Icon icon="mdi:message-text" /> 发私信
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑表单 -->
    <div v-if="editing" class="edit-panel">
      <h3>修改资料</h3>
      <div class="edit-form">
        <div class="form-row">
          <label>邮箱</label>
          <input v-model="form.email" type="email" placeholder="请输入邮箱" class="text-input" />
        </div>
        <div class="form-row">
          <label>手机</label>
          <input v-model="form.phone" type="text" placeholder="请输入手机号" class="text-input" />
        </div>
        <div class="form-row">
          <label>简介</label>
          <textarea v-model="form.bio" placeholder="介绍一下你自己吧..." class="text-input text-area" rows="3"></textarea>
        </div>
        <div class="form-actions">
          <button class="save-btn" @click="save">保存修改</button>
          <button class="cancel-btn" @click="editing = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 统计 -->
    <div class="profile-grid">
      <div class="grid-panel stats-panel">
        <h3>技能统计</h3>
        <div class="stats-grid">
          <div class="stat-box">
            <span class="stat-num">{{ profile.skillCount || 0 }}</span>
            <span class="stat-desc">发布技能</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ profile.orderCount || 0 }}</span>
            <span class="stat-desc">完成交易</span>
          </div>
          <div class="stat-box clickable" @click="openFollowDialog('followers')">
            <span class="stat-num">{{ profile.followerCount || 0 }}</span>
            <span class="stat-desc">粉丝 <Icon icon="mdi:chevron-right" class="stat-arrow" /></span>
          </div>
          <div class="stat-box clickable" @click="openFollowDialog('following')">
            <span class="stat-num">{{ profile.followingCount || 0 }}</span>
            <span class="stat-desc">关注 <Icon icon="mdi:chevron-right" class="stat-arrow" /></span>
          </div>
        </div>
      </div>

      <div class="grid-panel info-panel">
        <h3>详细信息</h3>
        <dl class="info-list">
          <div class="info-row">
            <dt>用户名</dt>
            <dd>{{ profile.username || userStore.username || '-' }}</dd>
          </div>
          <div class="info-row">
            <dt>邮箱</dt>
            <dd>{{ profile.email || '未设置' }}</dd>
          </div>
          <div class="info-row">
            <dt>手机</dt>
            <dd>{{ profile.phone || '未设置' }}</dd>
          </div>
          <div class="info-row">
            <dt>角色</dt>
            <dd>{{ userStore.role === 'ADMIN' ? '管理员' : '普通用户' }}</dd>
          </div>
          <div class="info-row">
            <dt>注册时间</dt>
            <dd>{{ profile.createTime || '-' }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 时间资产（仅自己可见） -->
    <div v-if="isSelf" class="wallet-section">
      <h3><Icon icon="mdi:bank" /> 时间资产</h3>
      <div class="asset-cards">
        <div
          v-for="card in statCards"
          :key="card.label"
          class="asset-card"
          :style="{ '--accent': card.color }"
        >
          <div class="asset-icon"><Icon :icon="card.icon" /></div>
          <div class="asset-info">
            <span class="asset-value">{{ card.value.toLocaleString() }}</span>
            <span class="asset-label">{{ card.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 信誉雷达图 -->
    <div class="radar-panel" v-if="reputation.radarData && reputation.radarData.length > 0">
      <h3>信誉雷达</h3>
      <div class="radar-body">
        <div class="radar-chart-wrap">
          <svg viewBox="0 0 180 180" class="radar-svg"
            @mouseleave="hoveredPoint = null"
          >
            <!-- 背景光晕 -->
            <circle cx="90" cy="90" r="70" fill="url(#radarGlow)" class="bg-glow" />
            <defs>
              <radialGradient id="radarGlow">
                <stop offset="0%" stop-color="#e8784a" stop-opacity="0.15" />
                <stop offset="100%" stop-color="#e8784a" stop-opacity="0" />
              </radialGradient>
            </defs>
            <!-- 网格 -->
            <polygon v-for="(pts, i) in gridPolygons" :key="i"
              :points="pts"
              fill="none" stroke="#e8e0d8" stroke-width="0.8"
            />
            <!-- 轴线 -->
            <line v-for="(pt, i) in axisLabels" :key="'l'+i"
              x1="90" y1="90" :x2="pt.x" :y2="pt.y"
              stroke="#e8e0d8" stroke-width="0.6"
            />
            <!-- 数据区域 -->
            <polygon v-if="radarPoints" :points="radarPoints"
              class="radar-polygon"
              fill="rgba(232,120,74,0.15)" stroke="#e8784a" stroke-width="1.5"
            />
            <!-- 高亮轴线（从中心到 hover 的点） -->
            <line v-for="(pt, i) in pointCoords" :key="'hl'+i"
              x1="90" y1="90" :x2="pt.x" :y2="pt.y"
              stroke="#e8784a" stroke-width="0"
              class="highlight-line"
              :class="{ 'highlight-line-on': hoveredPoint === i }"
            />
            <!-- 声纳环 -->
            <circle v-for="(pt, i) in pointCoords" :key="'s'+i"
              :cx="pt.x" :cy="pt.y" r="4" fill="none" stroke="#e8784a" stroke-width="1.5"
              class="sonar-ring"
              :class="{ 'sonar-ring-on': hoveredPoint === i }"
            />
            <!-- 数据点 hover 热区 -->
            <circle v-for="(pt, i) in pointCoords" :key="'h'+i"
              :cx="pt.x" :cy="pt.y" r="14" fill="transparent"
              style="cursor:pointer"
              @mouseenter="hoveredPoint = i"
              @mouseleave="hoveredPoint = null"
            />
            <!-- 数据点 -->
            <circle v-if="radarPoints" v-for="(pt, i) in pointCoords" :key="'d'+i"
              :cx="pt.x" :cy="pt.y" r="3"
              fill="#e8784a" stroke="#fff" stroke-width="1.5"
              class="radar-dot"
              :style="{ animationDelay: (0.6 + i * 0.15) + 's' }"
            />
            <!-- 分数 tag（hover 时在轴线末端展开） -->
            <g v-for="(pt, i) in pointCoords" :key="'v'+i"
              class="score-tag"
              :class="{ 'score-tag-on': hoveredPoint === i }"
            >
              <circle :cx="pt.x" :cy="pt.y" r="13" fill="#fff" stroke="#e8784a" stroke-width="1.5" />
              <text :x="pt.x" :y="pt.y + 0.5" text-anchor="middle" dominant-baseline="middle"
                font-size="10" font-weight="700" fill="#e8784a"
              >{{ pt.score }}</text>
            </g>
            <!-- 标签 -->
            <text v-for="(pt, i) in axisLabels" :key="'t'+i"
              :x="pt.x" :y="pt.y"
              text-anchor="middle" dominant-baseline="middle"
              font-size="10" fill="#999"
              class="radar-label"
              :style="{ animationDelay: (0.8 + i * 0.08) + 's' }"
            >{{ pt.label }}</text>
          </svg>
        </div>
        <div class="radar-stats">
          <div class="radar-stat-item">
            <span class="radar-stat-num">{{ reputation.avgScore || '-' }}</span>
            <span class="radar-stat-desc">综合评分</span>
          </div>
          <div class="radar-stat-item">
            <span class="radar-stat-num">{{ reputation.reviewCount || 0 }}</span>
            <span class="radar-stat-desc">评价次数</span>
          </div>
          <div class="radar-stat-item" v-if="reputation.goodRate !== undefined">
            <span class="radar-stat-num goodrate-num">{{ reputation.goodRate }}%</span>
            <span class="radar-stat-desc">好评率</span>
          </div>
        </div>
        <!-- 竖向评分分布 -->
        <div class="dist-section-v" v-if="reputation.scoreDistribution">
          <div
            v-for="(c, i) in [...reputation.scoreDistribution].reverse()"
            :key="i"
            class="dist-bar-v"
          >
            <span class="dist-bar-v-label">{{ 5 - i }}星</span>
            <div class="dist-bar-v-track">
              <div
                class="dist-bar-v-fill"
                :class="'fill-' + (5 - i)"
                :style="{ height: (reputation.reviewCount > 0 ? c / Math.max(...reputation.scoreDistribution) * 100 : 0) + '%' }"
              ></div>
            </div>
            <span class="dist-bar-v-count">{{ c }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="radar-panel radar-empty" v-else-if="!loading">
      <h3>信誉雷达</h3>
      <div class="radar-empty-text">
        <Icon icon="mdi:radar" class="radar-empty-icon" />
        <p>暂无评价数据，完成交易后可获得信誉评分</p>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="reviews-panel" v-if="reviews.length > 0">
      <h3>收到的评价 <span class="reviews-count">共 {{ reviews.length }} 条</span></h3>

      <!-- 评价条目 -->
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="ri-head">
          <div class="ri-user">
            <img v-if="r.reviewerAvatar" :src="r.reviewerAvatar" class="ri-avatar" />
            <span v-else class="ri-avatar ri-avatar-text">{{ (r.reviewerName || '?').charAt(0).toUpperCase() }}</span>
            <span class="ri-name">{{ r.reviewerName || '用户' }}</span>
            <span class="ri-role" :class="r.reviewerRole === 'BUYER' ? 'role-buyer' : 'role-seller'">
              {{ r.reviewerRole === 'BUYER' ? '买家' : '卖家' }}
            </span>
            <span class="ri-label" :class="reviewLabel(r.score).cls">{{ reviewLabel(r.score).text }}</span>
          </div>
          <div class="ri-meta">
            <el-rate v-model="r.score" disabled :max="5" size="small" />
            <span class="ri-time">{{ formatTime(r.createTime) }}</span>
          </div>
        </div>
        <div class="ri-body">
          <div class="ri-dims">
            <span class="ri-dim">按时 <i>{{ r.punctualityScore }}</i></span>
            <span class="ri-dim">沟通 <i>{{ r.communicationScore }}</i></span>
            <span class="ri-dim">专业 <i>{{ r.professionalScore }}</i></span>
            <span class="ri-dim">态度 <i>{{ r.attitudeScore }}</i></span>
          </div>
          <div v-if="r.orderContext" class="ri-context">
            <Icon icon="mdi:shopping-outline" /> {{ r.orderContext }}
          </div>
          <p v-if="r.comment" class="ri-comment">{{ r.comment }}</p>
          <p v-else class="ri-comment ri-comment-empty">该用户未填写文字评价</p>
        </div>
      </div>
    </div>
    <div class="reviews-panel reviews-empty" v-else-if="!loading">
      <h3>收到的评价</h3>
      <div class="reviews-empty-text">
        <Icon icon="mdi:comment-text-outline" class="reviews-empty-icon" />
        <p>暂无评价</p>
      </div>
    </div>

    <!-- 时间流水（仅自己可见） -->
    <div v-if="isSelf" class="wallet-section">
      <h3><Icon icon="mdi:swap-horizontal" /> 时间流水</h3>
      <div class="transaction-list" v-if="transactions.length > 0">
        <div v-for="tx in transactions" :key="tx.id" class="tx-item">
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
      <el-empty v-else description="暂无交易记录" :image-size="60" />
    </div>

    <!-- 关注/粉丝弹窗 -->
    <el-dialog v-model="followDialog" :title="followDialogTitle" width="380px" center>
      <div v-loading="followListLoading" class="follow-dialog-body">
        <div v-if="followList.length === 0 && !followListLoading" class="follow-empty">
          暂无{{ followDialogTitle }}
        </div>
        <div
          v-for="u in followList"
          :key="u.id"
          class="follow-user-item"
          @click="followDialog = false; router.push(`/profile/${u.id}`)"
        >
          <div class="fu-avatar">
            <img v-if="u.avatar" :src="u.avatar" class="fu-avatar-img" />
            <span v-else>{{ (u.username || '?').charAt(0).toUpperCase() }}</span>
          </div>
          <div class="fu-info">
            <span class="fu-name">{{ u.username }}</span>
            <span class="fu-bio" v-if="u.bio">{{ u.bio?.substring(0, 30) }}{{ u.bio?.length > 30 ? '...' : '' }}</span>
          </div>
          <Icon icon="mdi:chevron-right" class="fu-arrow" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ========== 顶部 Hero ========== */
.profile-hero {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  margin-bottom: 20px;
  border: 1px solid #f0e8e0;
}
.hero-bg {
  height: 100px;
  background: linear-gradient(135deg, #e8784a 0%, #f0a060 40%, #f5c090 100%);
}
.hero-content {
  padding: 0 32px 28px;
  display: flex;
  align-items: flex-end;
  gap: 20px;
  margin-top: -36px;
  position: relative;
}
.user-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  border: 4px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(232,120,74,0.25);
  position: relative;
  overflow: hidden;
}
.user-avatar.clickable { cursor: pointer; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.user-avatar.clickable:hover .avatar-overlay { opacity: 1; }
.user-avatar.uploading .avatar-overlay { opacity: 1; background: rgba(0,0,0,0.5); }
.camera-icon { font-size: 28px; color: #fff; }
.avatar-text { font-size: 36px; font-weight: 800; color: #fff; }
.user-main { flex: 1; padding-top: 8px; }
.user-name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.user-name-row h2 { font-size: 22px; font-weight: 700; color: #2c3e50; margin: 0; }
.role-badge {
  padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
  background: #ecf5ff; color: #409eff;
}
.role-badge.admin { background: #fef0f0; color: #f56c6c; }
.user-bio { font-size: 14px; color: #777; margin: 4px 0 8px; }
.user-meta { display: flex; flex-wrap: wrap; gap: 16px; }
.meta-item { font-size: 13px; color: #999; display: inline-flex; align-items: center; gap: 4px; }
.edit-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  flex-shrink: 0; align-self: flex-start; margin-top: 8px;
  transition: all 0.3s ease;
}
.edit-btn:hover { box-shadow: 0 4px 14px rgba(232,120,74,0.3); transform: translateY(-1px); }

.profile-actions {
  display: flex; gap: 10px; flex-shrink: 0;
  align-self: flex-start; margin-top: 8px;
}
.action-btn {
  padding: 8px 20px; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex; align-items: center; gap: 4px; white-space: nowrap;
}
.follow-btn {
  background: linear-gradient(135deg, #e8784a, #f0a060); color: #fff;
}
.follow-btn:hover { box-shadow: 0 4px 14px rgba(232,120,74,0.3); transform: translateY(-1px); }
.follow-btn.following {
  background: #f5f5f5; color: #999; border: 1px solid #e8e0d8;
}
.follow-btn.following:hover { background: #fee; color: #f56c6c; border-color: #fcc; }
.msg-btn {
  background: #fff; color: #e8784a; border: 1px solid #e8784a;
}
.msg-btn:hover { background: rgba(232,120,74,0.06); box-shadow: 0 4px 14px rgba(232,120,74,0.15); transform: translateY(-1px); }

/* ========== 编辑面板 ========== */
.edit-panel {
  background: #fff; border-radius: 14px; padding: 24px 28px; margin-bottom: 20px;
  border: 1px solid #f0e8e0;
}
.edit-panel h3 { font-size: 17px; font-weight: 600; color: #2c3e50; margin: 0 0 18px; }
.form-row { margin-bottom: 16px; }
.form-row label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
.text-input {
  width: 100%; padding: 10px 14px; border: 1px solid #e8e0d8; border-radius: 8px;
  font-size: 14px; color: #333; background: #fafafa; outline: none;
  transition: all 0.3s; font-family: inherit; box-sizing: border-box;
}
.text-input:focus { border-color: #e8784a; box-shadow: 0 0 0 3px rgba(232,120,74,0.08); background: #fff; }
.text-area { resize: vertical; }
.form-actions { display: flex; gap: 12px; margin-top: 20px; }
.save-btn {
  padding: 9px 24px; background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff; border: none; border-radius: 8px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s;
}
.save-btn:hover { box-shadow: 0 4px 14px rgba(232,120,74,0.3); }
.cancel-btn {
  padding: 9px 24px; background: #f5f5f5; color: #666;
  border: 1px solid #e8e0d8; border-radius: 8px;
  font-size: 14px; cursor: pointer; transition: all 0.3s;
}
.cancel-btn:hover { background: #eee; }

/* ========== 下方网格 ========== */
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.grid-panel {
  background: #fff; border-radius: 14px; padding: 24px;
  border: 1px solid #f0e8e0;
}
.grid-panel h3 { font-size: 16px; font-weight: 600; color: #2c3e50; margin: 0 0 18px; }

.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.stat-box {
  text-align: center; padding: 16px; border-radius: 10px;
  background: #fdf9f6; border: 1px solid #f5ede5;
}
.stat-box.clickable {
  cursor: pointer; transition: all 0.2s;
}
.stat-box.clickable:hover {
  background: #fef5ee; border-color: #e8784a;
}
.stat-num { display: block; font-size: 28px; font-weight: 800; color: #e8784a; line-height: 1.2; }
.stat-desc {
  font-size: 12px; color: #999; margin-top: 4px;
  display: inline-flex; align-items: center; gap: 2px;
}
.stat-arrow { font-size: 14px; }

/* 信息列表 */
.info-list { display: flex; flex-direction: column; }
.info-row {
  display: flex; justify-content: space-between; padding: 12px 0;
  border-bottom: 1px solid #f5f0eb;
}
.info-row:last-child { border-bottom: none; }
.info-row dt { font-size: 13px; color: #999; font-weight: 500; }
.info-row dd { font-size: 14px; color: #333; font-weight: 500; margin: 0; }

/* ========== 雷达图面板 ========== */
.radar-panel {
  background: #fff; border-radius: 14px; padding: 24px;
  border: 1px solid #f0e8e0; margin-bottom: 20px;
}
.radar-panel h3 { font-size: 16px; font-weight: 600; color: #2c3e50; margin: 0 0 18px; }
.radar-body { display: flex; align-items: center; gap: 24px; }
.radar-chart-wrap { flex-shrink: 0; }
.radar-svg { width: 200px; height: 200px; display: block; overflow: visible; }

/* ========== 雷达图动画 ========== */

/* 背景光晕常亮 */
.bg-glow {
  opacity: 1;
  animation: glowPulse 3s ease-in-out infinite;
}
@keyframes glowPulse {
  0% { opacity: 0.4; }
  50% { opacity: 0.7; }
  100% { opacity: 0.4; }
}

/* 入场：数据多边形渐现 */
@keyframes radarIn {
  0% { opacity: 0; transform: scale(0.6); }
  100% { opacity: 1; transform: scale(1); }
}
.radar-polygon {
  animation: radarIn 0.6s ease-out forwards;
  transform-origin: 90px 90px;
}

/* 入场：数据点弹出 */
@keyframes dotPop {
  0% { opacity: 0; r: 0; }
  100% { opacity: 1; r: 3; }
}
.radar-dot {
  animation: dotPop 0.3s ease-out both;
}

/* 声纳环动画（hover 时在数据点外扩散） */
@keyframes sonarWave {
  0% { opacity: 0.7; r: 3; stroke-width: 2; }
  100% { opacity: 0; r: 20; stroke-width: 0.5; }
}
.sonar-ring {
  opacity: 0;
}
.sonar-ring-on {
  opacity: 1;
  animation: sonarWave 1s ease-out infinite;
  pointer-events: none;
}

/* 高亮轴线（从中心到 hover 点的连线） */
.highlight-line {
  transition: stroke-width 0.3s ease;
}
.highlight-line-on {
  stroke-width: 1.2 !important;
  stroke-opacity: 0.45;
}

/* 分数 tag 弹出 */
@keyframes tagPop {
  0% { opacity: 0; transform: scale(0.5); }
  70% { transform: scale(1.1); }
  100% { opacity: 1; transform: scale(1); }
}
.score-tag {
  opacity: 0;
  pointer-events: none;
}
.score-tag-on {
  opacity: 1 !important;
  animation: tagPop 0.25s ease-out both;
  pointer-events: none;
}

/* 标签入场 */
@keyframes labelIn {
  0% { opacity: 0; transform: translateY(4px); }
  100% { opacity: 1; transform: translateY(0); }
}
.radar-label {
  animation: labelIn 0.3s ease-out both;
}

.radar-stats {
  display: flex; flex-direction: column; gap: 20px;
}
.radar-stat-item {
  text-align: center;
  animation: statUp 0.5s ease-out both;
}
.radar-stat-item:nth-child(1) { animation-delay: 0.6s; }
.radar-stat-item:nth-child(2) { animation-delay: 0.7s; }
.radar-stat-item:nth-child(3) { animation-delay: 0.8s; }

@keyframes statUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}
.radar-stat-num { display: block; font-size: 32px; font-weight: 800; color: #e8784a; }
.radar-stat-desc { font-size: 13px; color: #999; }

.radar-empty { text-align: center; }
.radar-empty-text { padding: 24px 0; color: #ccc; }
.radar-empty-icon { font-size: 48px; }
.radar-empty-text p { margin: 8px 0 0; font-size: 14px; }

/* ========== 关注弹窗 ========== */
.follow-dialog-body { max-height: 360px; overflow-y: auto; }
.follow-empty { text-align: center; color: #ccc; padding: 32px 0; font-size: 14px; }
.follow-user-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 4px; cursor: pointer; transition: all 0.2s;
  border-bottom: 1px solid #f5f0eb;
}
.follow-user-item:hover { background: #fdf9f6; border-radius: 8px; padding-left: 8px; padding-right: 8px; }
.fu-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 16px; flex-shrink: 0; overflow: hidden;
}
.fu-avatar-img { width: 100%; height: 100%; object-fit: cover; }
.fu-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.fu-name { font-size: 14px; font-weight: 600; color: #333; }
.fu-bio { font-size: 12px; color: #aaa; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.fu-arrow { font-size: 18px; color: #ccc; flex-shrink: 0; }

@media (max-width: 768px) {
  .hero-content { flex-direction: column; align-items: center; text-align: center; }
  .profile-grid { grid-template-columns: 1fr; }
  .user-meta { justify-content: center; }
  .radar-body { flex-direction: column; }
}

/* ========== 评价列表 ========== */
.reviews-panel {
  background: #fff; border-radius: 14px; padding: 24px;
  border: 1px solid #f0e8e0; margin-bottom: 20px;
}
.reviews-panel h3 {
  font-size: 16px; font-weight: 600; color: #2c3e50; margin: 0 0 16px;
  display: flex; align-items: center; gap: 8px;
}
.reviews-count { font-size: 13px; font-weight: 400; color: #bbb; }
.goodrate-num { color: #4caf50 !important; }
/* ========== 竖向评分分布（雷达图右侧） ========== */
.dist-section-v {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 12px;
  height: 160px;
  padding: 0 8px;
  margin-left: auto;
}
.dist-bar-v {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  height: 100%;
  flex: 1;
  max-width: 36px;
}
.dist-bar-v-label {
  font-size: 11px;
  color: #999;
  font-weight: 500;
  flex-shrink: 0;
}
.dist-bar-v-track {
  flex: 1;
  width: 100%;
  background: #f5f0eb;
  border-radius: 6px 6px 0 0;
  position: relative;
  display: flex;
  align-items: flex-end;
  align-self: stretch;
}
.dist-bar-v-fill {
  width: 100%;
  border-radius: 6px 6px 0 0;
  animation: barGrow 0.6s ease-out both;
  min-height: 4px;
  transform-origin: bottom center;
}
.dist-bar-v:nth-child(1) .dist-bar-v-fill { animation-delay: 0.7s; }
.dist-bar-v:nth-child(2) .dist-bar-v-fill { animation-delay: 0.8s; }
.dist-bar-v:nth-child(3) .dist-bar-v-fill { animation-delay: 0.9s; }
.dist-bar-v:nth-child(4) .dist-bar-v-fill { animation-delay: 1.0s; }
.dist-bar-v:nth-child(5) .dist-bar-v-fill { animation-delay: 1.1s; }

@keyframes barGrow {
  0% { transform: scaleY(0); opacity: 0; }
  100% { transform: scaleY(1); opacity: 1; }
}
.dist-bar-v-count {
  font-size: 11px;
  color: #666;
  font-weight: 600;
  flex-shrink: 0;
}
.fill-5 { background: linear-gradient(180deg, #4caf50, #66bb6a); }
.fill-4 { background: linear-gradient(180deg, #8bc34a, #aed581); }
.fill-3 { background: linear-gradient(180deg, #ffc107, #ffd54f); }
.fill-2 { background: linear-gradient(180deg, #ff9800, #ffb74d); }
.fill-1 { background: linear-gradient(180deg, #f44336, #ef5350); }
.dim-tags { display: flex; gap: 12px; flex-wrap: wrap; }
.dim-tag {
  font-size: 13px; color: #666; background: #fff; padding: 6px 14px;
  border-radius: 20px; border: 1px solid #f0e8e0; display: flex; align-items: center; gap: 6px;
}
.dim-tag em { font-style: normal; font-weight: 700; color: #e8784a; }

.review-item {
  padding: 16px 0; border-bottom: 1px solid #f5f0eb;
}
.review-item:last-child { border-bottom: none; padding-bottom: 0; }
.ri-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 10px;
}
.ri-user { display: flex; align-items: center; gap: 10px; }
.ri-avatar {
  width: 36px; height: 36px; border-radius: 50%; object-fit: cover;
  background: linear-gradient(135deg, #f0a060, #e8784a); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 14px; flex-shrink: 0;
}
.ri-avatar-text { font-size: 14px; }
.ri-name { font-size: 14px; font-weight: 600; color: #333; }
.ri-role {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 4px;
}
.ri-role.role-buyer { background: #e8f4fd; color: #409eff; }
.ri-role.role-seller { background: #fef0e8; color: #e8784a; }
.ri-label {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 4px;
}
.ri-label.label-good { background: #e8f5e9; color: #4caf50; }
.ri-label.label-neutral { background: #fff7e6; color: #ff9800; }
.ri-label.label-bad { background: #fef0f0; color: #f44336; }
.ri-context {
  font-size: 12px; color: #999; margin-bottom: 6px;
  display: flex; align-items: center; gap: 4px;
}
.ri-meta { display: flex; align-items: center; gap: 12px; }
.ri-time { font-size: 12px; color: #bbb; white-space: nowrap; }
.ri-body { padding-left: 46px; }
.ri-dims { display: flex; gap: 16px; margin-bottom: 8px; }
.ri-dim { font-size: 12px; color: #999; }
.ri-dim i { font-style: normal; font-weight: 600; color: #e8784a; }
.ri-comment { font-size: 14px; color: #555; line-height: 1.6; margin: 0; }
.ri-comment-empty { color: #ccc; font-style: italic; }

.reviews-empty { text-align: center; }
.reviews-empty-text { padding: 24px 0; color: #ccc; }
.reviews-empty-icon { font-size: 48px; }
.reviews-empty-text p { margin: 8px 0 0; font-size: 14px; }

/* ========== 时间银行（整合到个人页） ========== */
.wallet-section {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #f0e8e0;
  margin-bottom: 20px;
}
.wallet-section h3 {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.asset-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.asset-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
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

/* 交易流水 */
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

@media (max-width: 768px) {
  .asset-cards { grid-template-columns: repeat(2, 1fr); }
}
</style>
