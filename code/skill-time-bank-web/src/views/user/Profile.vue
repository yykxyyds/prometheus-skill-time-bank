<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getProfile, updateProfile, getUserProfile, uploadAvatar, getFollowStatus, followUser, unfollowUser, getFollowers, getFollowing, getReputation } from '../../api/user'
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
const radarLabels = ['按时', '沟通', '专业', '态度']
const radarMax = 5

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
  }
})

async function save() {
  await updateProfile(form.value)
  ElMessage.success('资料已更新')
  editing.value = false
  const res = await getProfile()
  profile.value = res.data || {}
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

    <!-- 信誉雷达图 -->
    <div class="radar-panel" v-if="reputation.radarData && reputation.radarData.length > 0">
      <h3>信誉雷达</h3>
      <div class="radar-body">
        <div class="radar-chart-wrap">
          <svg viewBox="0 0 180 180" class="radar-svg">
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
              fill="rgba(232,120,74,0.2)" stroke="#e8784a" stroke-width="1.5"
            />
            <!-- 数据点 -->
            <circle v-if="radarPoints" v-for="(pt, i) in radarPoints.split(' ')" :key="'d'+i"
              :cx="pt.split(',')[0]" :cy="pt.split(',')[1]" r="3"
              fill="#e8784a" stroke="#fff" stroke-width="1"
            />
            <!-- 标签 -->
            <text v-for="(pt, i) in axisLabels" :key="'t'+i"
              :x="pt.x" :y="pt.y"
              text-anchor="middle" dominant-baseline="middle"
              font-size="10" fill="#666"
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
.radar-svg { width: 200px; height: 200px; display: block; }
.radar-stats {
  display: flex; flex-direction: column; gap: 20px;
}
.radar-stat-item { text-align: center; }
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
</style>
