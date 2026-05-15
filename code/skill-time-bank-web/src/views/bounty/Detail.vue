<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import api from '../../api/index'
import { deleteBounty } from '../../api/skill'
import { Icon } from '@iconify/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserProfile, followUser, unfollowUser, getFollowStatus } from '../../api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const bounty = ref({})
const loading = ref(false)
const applying = ref(false)
const applyMsg = ref('')
const isFollowed = ref(false)
const followLoading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get(`/bounty/${route.params.id}`)
    bounty.value = res.data || {}
    if (userStore.isLoggedIn && bounty.value.userId && bounty.value.userId !== userStore.userId) {
      checkFollowStatus()
    }
  } finally {
    loading.value = false
  }
})

async function checkFollowStatus() {
  try {
    const res = await getFollowStatus(bounty.value.userId)
    isFollowed.value = res.data?.isFollowing || false
  } catch { /* ignore */ }
}

async function toggleFollow() {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  followLoading.value = true
  try {
    if (isFollowed.value) {
      await unfollowUser(bounty.value.userId)
      isFollowed.value = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(bounty.value.userId)
      isFollowed.value = true
      ElMessage.success('已关注')
    }
  } catch { /* handled */ }
  finally { followLoading.value = false }
}

const statusMap = {
  0: { label: '待审核', color: '#e8784a', bg: '#fef0e6' },
  1: { label: '已发布', color: '#e6a23c', bg: '#fdf6ec' },
  2: { label: '已接单', color: '#409eff', bg: '#ecf5ff' },
  3: { label: '已完成', color: '#4caf50', bg: '#e8f5e9' },
  4: { label: '已拒绝', color: '#999', bg: '#f5f5f5' }
}
const statusInfo = computed(() => statusMap[bounty.value.status] || statusMap[4])
const isOwner = computed(() => userStore.isLoggedIn && userStore.userId === bounty.value.userId)
const canApply = computed(() => userStore.isLoggedIn && bounty.value.status === 1 && !isOwner.value)
const showApplyForm = computed(() => bounty.value.status === 1 && !isOwner.value)

const applications = ref([])
const loadingApps = ref(false)
const showApps = ref(false)
const applicantProfiles = ref({})

async function loadApplications() {
  showApps.value = !showApps.value
  if (!showApps.value) return
  loadingApps.value = true
  try {
    const res = await api.get(`/bounty/${bounty.value.id}/applications`)
    applications.value = res.data || []
    // 获取申请人信息
    const profiles = {}
    for (const app of applications.value) {
      try {
        const pr = await getUserProfile(app.applicantId)
        profiles[app.applicantId] = pr.data || {}
      } catch { /* ignore */ }
    }
    applicantProfiles.value = profiles
  } catch (e) { /* handled */ } finally {
    loadingApps.value = false
  }
}

async function handleApply() {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  if (!applyMsg.value.trim()) {
    ElMessage.warning('请填写申请留言')
    return
  }
  applying.value = true
  try {
    await api.post(`/bounty/${bounty.value.id}/apply`, { message: applyMsg.value })
    ElMessage.success('申请已发送')
    applyMsg.value = ''
  } catch (e) { /* handled */ }
  finally { applying.value = false }
}

async function handleAccept(appId) {
  try {
    await api.put(`/bounty/${bounty.value.id}/accept/${appId}`)
    ElMessage.success('已接受申请')
    const res = await api.get(`/bounty/${bounty.value.id}`)
    bounty.value = res.data || {}
    showApps.value = false
  } catch (e) { /* handled */ }
}

async function handleReject(appId) {
  try {
    await api.put(`/bounty/${bounty.value.id}/reject/${appId}`)
    ElMessage.success('已拒绝申请')
    const res = await api.get(`/bounty/${bounty.value.id}`)
    bounty.value = res.data || {}
  } catch (e) { /* handled */ }
}

function handleEdit() {
  router.push(`/bounty/create?edit=${bounty.value.id}`)
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个悬赏吗？删除后不可恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteBounty(bounty.value.id)
    ElMessage.success('悬赏已删除')
    router.push('/bounty')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') { /* handled by interceptor */ }
  }
}

async function handleComplete() {
  try {
    await api.put(`/bounty/${bounty.value.id}/complete`)
    ElMessage.success('悬赏已完成')
    const res = await api.get(`/bounty/${bounty.value.id}`)
    bounty.value = res.data || {}
  } catch (e) { /* handled */ }
}
</script>

<template>
  <div class="bounty-detail" v-loading="loading">
    <template v-if="bounty.id">
      <div class="breadcrumb">
        <router-link to="/bounty">需求悬赏</router-link>
        <span class="sep">/</span>
        <span class="current">{{ bounty.title }}</span>
      </div>

      <div class="detail-layout">
        <!-- 左侧主体 -->
        <div class="detail-main">
          <div class="header-card">
            <div class="title-row">
              <h1>{{ bounty.title }}</h1>
              <span class="status-tag" :style="{ color: statusInfo.color, background: statusInfo.bg }">
                {{ statusInfo.label }}
              </span>
            </div>

            <div class="meta-row">
              <span class="meta-item">
                <Icon icon="mdi:star" class="m-icon" />
                <strong>{{ bounty.reward }}</strong> 时间币
              </span>
              <span class="meta-item" v-if="bounty.deadline">
                <Icon icon="mdi:calendar-clock" class="m-icon" />
                截止 {{ bounty.deadline }}
              </span>
              <span class="meta-item" v-if="bounty.userName">
                <Icon icon="mdi:account" class="m-icon" />
                {{ bounty.userName }}
              </span>
            </div>
          </div>

          <div class="content-card">
            <h3>需求描述</h3>
            <p class="desc-text">{{ bounty.description || '暂无详细描述' }}</p>
          </div>

          <!-- 发布者管理 -->
          <div class="content-card" v-if="isOwner">
            <h3>管理悬赏</h3>
            <div class="owner-actions">
              <button class="btn-primary" @click="loadApplications" v-if="bounty.status === 1">
                <Icon icon="mdi:format-list-bulleted" /> 查看申请列表
              </button>
              <button class="btn-edit" @click="handleEdit" v-if="bounty.status === 0 || bounty.status === 1">
                <Icon icon="mdi:pencil" /> 编辑
              </button>
              <button class="btn-delete" @click="handleDelete" v-if="bounty.status === 0 || bounty.status === 1">
                <Icon icon="mdi:delete-outline" /> 删除
              </button>
              <button class="btn-success" @click="handleComplete" v-if="bounty.status === 2">
                <Icon icon="mdi:check-circle" /> 确认完成
              </button>
            </div>
            <div class="app-list" v-if="showApps && applications.length > 0" v-loading="loadingApps">
              <div v-for="app in applications" :key="app.id" class="app-item">
                <div class="app-user-card">
                  <div class="au-avatar" @click="router.push(`/profile/${app.applicantId}`)">
                    <img v-if="applicantProfiles[app.applicantId]?.avatar" :src="applicantProfiles[app.applicantId].avatar" class="au-avatar-img" @error="$event.target.style.display='none'" />
                    <span v-show="!applicantProfiles[app.applicantId]?.avatar">{{ (applicantProfiles[app.applicantId]?.username || '?').charAt(0) }}</span>
                  </div>
                  <div class="au-info" @click="router.push(`/profile/${app.applicantId}`)">
                    <span class="au-name">{{ applicantProfiles[app.applicantId]?.username || '用户#' + app.applicantId }}</span>
                    <span class="au-msg">{{ app.message }}</span>
                    <span class="au-time">{{ app.createTime }}</span>
                  </div>
                  <div class="au-actions">
                    <button class="btn-sm ghost" @click.stop="router.push(`/messages?userId=${app.applicantId}`)">
                      <Icon icon="mdi:message-text" /> 私信
                    </button>
                    <template v-if="bounty.status === 1">
                      <button class="btn-sm primary" @click="handleAccept(app.id)">接受</button>
                      <button class="btn-sm danger" @click="handleReject(app.id)">拒绝</button>
                    </template>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="showApps && applications.length === 0 && !loadingApps" description="暂无申请" :image-size="60" />
          </div>

          <!-- 申请表单 -->
          <div class="apply-card" v-if="showApplyForm">
            <h3>申请接单</h3>
            <el-input
              v-model="applyMsg"
              type="textarea"
              :rows="3"
              placeholder="简要说明你的能力和方案..."
              maxlength="300"
              show-word-limit
            />
            <button class="apply-btn" :disabled="applying" @click="handleApply" style="margin-top:12px">
              {{ applying ? '提交中...' : (userStore.isLoggedIn ? '提交申请' : '请先登录') }}
            </button>
          </div>

          <div class="apply-card closed" v-else-if="bounty.status !== 1">
            <Icon icon="mdi:lock" /> 该悬赏{{ statusInfo.label }}，不再接受申请
          </div>
        </div>

        <!-- 右侧信息卡 -->
        <div class="side-card">
          <div class="reward-show">
            <span class="reward-num">{{ bounty.reward }}</span>
            <span class="reward-label">时间币悬赏</span>
          </div>
          <div class="side-info">
            <!-- 发布者卡片 -->
            <div class="publisher-card">
              <div class="publisher-avatar" @click="router.push(`/profile/${bounty.userId}`)">
                <img v-if="bounty.userAvatar" :src="bounty.userAvatar" class="avatar-img" @error="$event.target.style.display='none'" />
                <span v-show="!bounty.userAvatar" class="avatar-letter">{{ (bounty.userName || '?').charAt(0) }}</span>
              </div>
              <div class="publisher-name" @click="router.push(`/profile/${bounty.userId}`)">
                {{ bounty.userName || '未知' }}
              </div>
              <div class="publisher-actions" v-if="userStore.isLoggedIn && !isOwner">
                <button
                  class="follow-sm-btn"
                  :class="{ followed: isFollowed }"
                  :disabled="followLoading"
                  @click="toggleFollow"
                >
                  {{ followLoading ? '...' : (isFollowed ? '已关注' : '+ 关注') }}
                </button>
                <button class="msg-sm-btn" @click="router.push(`/messages?userId=${bounty.userId}`)">
                  <Icon icon="mdi:message-text" /> 私信
                </button>
              </div>
            </div>
            <div class="info-row">
              <span>发布时间</span>
              <span>{{ bounty.createTime }}</span>
            </div>
            <div class="info-row" v-if="bounty.deadline">
              <span>截止时间</span>
              <span>{{ bounty.deadline }}</span>
            </div>
            <div class="info-row">
              <span>接单人</span>
              <span>{{ bounty.applicantId ? '#' + bounty.applicantId : '待接单' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="悬赏不存在或已删除" :image-size="120" />
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
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

/* ========== 左侧 ========== */
.header-card, .content-card, .apply-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px 28px;
  border: 1px solid #f0e8e0;
  margin-bottom: 16px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.title-row h1 {
  font-size: 22px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}
.status-tag {
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.meta-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.meta-item {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}
.meta-item strong {
  color: #e8784a;
  font-size: 16px;
}
.m-icon { font-size: 17px; color: #f0a060; }

.content-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}
.desc-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

/* 申请区 */
.apply-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}
.apply-btn {
  padding: 9px 24px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.apply-btn:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(232, 120, 74, 0.35);
}
.apply-btn:disabled { opacity: 0.7; cursor: not-allowed; }

/* Owner actions */
.owner-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}
.btn-primary {
  padding: 9px 18px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-primary:hover {
  box-shadow: 0 4px 14px rgba(232, 120, 74, 0.3);
}
.btn-success {
  padding: 9px 18px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-success:hover {
  background: #5daf34;
}
.btn-edit {
  padding: 9px 18px;
  background: #fff;
  color: #e8784a;
  border: 1px solid #f0c8b0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-edit:hover {
  background: rgba(232,120,74,0.06);
  border-color: #e8784a;
}
.btn-delete {
  padding: 9px 18px;
  background: #fff;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-delete:hover {
  background: #fef0f0;
  border-color: #f56c6c;
}
.app-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.app-item {
  padding: 14px;
  background: #fdf9f6;
  border-radius: 10px;
  border: 1px solid #f0e8e0;
}
.app-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
}
.au-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s;
}
.au-avatar:hover {
  box-shadow: 0 2px 10px rgba(232,120,74,0.3);
}
.au-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}
.au-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.au-name {
  font-weight: 600;
  color: #e8784a;
  font-size: 14px;
}
.au-msg {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.au-time {
  font-size: 11px;
  color: #bbb;
}
.au-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}
.btn-sm.ghost {
  color: #e8784a;
  border-color: #f0c8b0;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.btn-sm.ghost:hover {
  background: rgba(232,120,74,0.06);
}
.btn-sm {
  padding: 4px 14px;
  border: 1px solid;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  background: #fff;
}
.btn-sm.primary {
  color: #67c23a;
  border-color: #c2e7b0;
}
.btn-sm.primary:hover {
  background: #f0f9eb;
}
.btn-sm.danger {
  color: #f56c6c;
  border-color: #fde2e2;
}
.btn-sm.danger:hover {
  background: #fef0f0;
}

.apply-card.closed {
  color: #999;
  text-align: center;
  padding: 20px;
  font-size: 14px;
}

/* ========== 右侧 ========== */
.side-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #f0e8e0;
  position: sticky;
  top: 84px;
  text-align: center;
}
.reward-show {
  padding-bottom: 20px;
  border-bottom: 1px solid #f5f0eb;
  margin-bottom: 16px;
}
.reward-num {
  font-size: 42px;
  font-weight: 800;
  color: #e8784a;
  display: block;
}
.reward-label {
  font-size: 13px;
  color: #999;
}
.side-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #888;
}
.info-row span:last-child {
  color: #555;
  font-weight: 500;
}
.clickable {
  color: #e8784a !important;
  cursor: pointer;
}
.clickable:hover { text-decoration: underline; }

/* 发布者卡片（右侧信息卡内） */
.publisher-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f0eb;
  margin-bottom: 4px;
}
.publisher-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 22px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s;
}
.publisher-avatar:hover {
  box-shadow: 0 2px 12px rgba(232,120,74,0.3);
}
.publisher-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.publisher-avatar .avatar-letter {
  /* Styled by parent */;
}
.publisher-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.publisher-name:hover {
  color: #e8784a;
}
.publisher-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}
.follow-sm-btn, .msg-sm-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}
.follow-sm-btn {
  border: 1px solid #e8784a;
  background: #fff;
  color: #e8784a;
}
.follow-sm-btn:hover:not(:disabled) {
  background: rgba(232,120,74,0.06);
}
.follow-sm-btn.followed {
  background: #f5f0eb;
  color: #999;
  border-color: #ddd;
}
.follow-sm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.msg-sm-btn {
  border: 1px solid #f0c8b0;
  background: #fff;
  color: #e8784a;
}
.msg-sm-btn:hover {
  background: rgba(232,120,74,0.06);
  border-color: #e8784a;
}
</style>
