<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from './stores/user'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import api from './api/index'

const userStore = useUserStore()
const router = useRouter()
const menuOpen = ref(false)
const avatarMenuOpen = ref(false)
const userAvatar = ref('')
const announcements = ref([])
const showAnnouncePanel = ref(false)
const loadingAnnounce = ref(false)
const notifications = ref([])
const showNotifPanel = ref(false)
const loadingNotif = ref(false)
let unreadTimer = null

onMounted(() => {
  userStore.refreshUnread()
  loadAvatar()
  unreadTimer = setInterval(() => userStore.refreshUnread(), 30000)
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => {
  if (unreadTimer) clearInterval(unreadTimer)
  document.removeEventListener('click', onDocClick)
})

function onDocClick() {
  avatarMenuOpen.value = false
}

async function loadAvatar() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await api.get('/user/profile')
    userAvatar.value = res.data?.avatar || ''
  } catch { /* silent */ }
}

async function loadAnnouncements() {
  if (announcements.value.length > 0) {
    showAnnouncePanel.value = true
    return
  }
  loadingAnnounce.value = true
  try {
    const res = await api.get('/announcement/list', { params: { page: 1, size: 50 } })
    announcements.value = res.data?.records || []
    showAnnouncePanel.value = true
  } catch (e) { /* silent */ } finally {
    loadingAnnounce.value = false
  }
}

async function loadNotifications() {
  loadingNotif.value = true
  try {
    const res = await api.get('/notification/list')
    notifications.value = Array.isArray(res.data) ? res.data : []
    showNotifPanel.value = true
  } catch (e) { /* silent */ } finally {
    loadingNotif.value = false
  }
}

function notifRoute(type, targetId) {
  if (!targetId) return null
  if (type === 'BOUNTY') return `/bounty/${targetId}`
  if (type === 'SKILL')  return `/skill/${targetId}`
  if (type === 'ORDER')  return `/order/${targetId}`
  return null
}

async function handleNotifClick(notif) {
  // 标记已读
  if (notif.isRead === 0) {
    try { await api.put(`/notification/${notif.id}/read`) } catch { /* silent */ }
  }
  showNotifPanel.value = false
  userStore.refreshUnread()
  // 跳转
  const path = notifRoute(notif.type, notif.targetId)
  if (path) router.push(path)
}

async function markAllNotifRead() {
  const unread = notifications.value.filter(n => n.isRead === 0)
  if (unread.length === 0) return
  await Promise.allSettled(unread.map(n => api.put(`/notification/${n.id}/read`)))
  notifications.value.forEach(n => { n.isRead = 1 })
  userStore.refreshUnread()
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function closeMenu() {
  menuOpen.value = false
}

function handleLogout() {
  avatarMenuOpen.value = false
  userAvatar.value = ''
  userStore.logout()
  router.push('/')
  closeMenu()
}
</script>

<template>
  <div id="app">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="logo-group" @click="closeMenu">
          <span class="logo-icon">P</span>
          <span class="logo-text">Prometheus</span>
        </router-link>

        <!-- 桌面导航 -->
        <nav class="main-nav">
          <router-link to="/" class="nav-item" active-class="nav-active" @click="closeMenu">
            <Icon icon="mdi:compass-rose" class="nav-icon" />技能广场
          </router-link>
          <router-link to="/bounty" class="nav-item" active-class="nav-active" @click="closeMenu">
            <Icon icon="mdi:clipboard-text-search" class="nav-icon" />需求悬赏
          </router-link>
          <a class="nav-item" style="cursor:pointer" @click="loadAnnouncements">
            <Icon icon="mdi:bullhorn" class="nav-icon" />公告
          </a>
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/login" class="nav-item nav-login" active-class="nav-active" @click="closeMenu">
              登录
            </router-link>
          </template>
        </nav>

        <!-- 右侧：消息 + 时间币 + 头像 -->
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <a class="icon-btn" title="通知" @click="loadNotifications">
              <Icon icon="mdi:bell" class="icon-btn-icon" />
              <span v-if="userStore.notifUnreadCount > 0" class="icon-btn-badge">{{ userStore.notifUnreadCount > 99 ? '99+' : userStore.notifUnreadCount }}</span>
            </a>
            <router-link to="/messages" class="icon-btn" title="消息">
              <Icon icon="mdi:message-text" class="icon-btn-icon" />
              <span v-if="userStore.unreadCount > 0" class="icon-btn-badge">{{ userStore.unreadCount > 99 ? '99+' : userStore.unreadCount }}</span>
            </router-link>
            <div class="avatar-wrap" @click.stop="avatarMenuOpen = !avatarMenuOpen">
              <img v-if="userAvatar" :src="userAvatar" class="avatar-img" />
              <span v-else class="avatar-text">{{ userStore.username?.charAt(0) || '?' }}</span>
              <Icon icon="mdi:chevron-down" class="avatar-arrow" :class="{ flip: avatarMenuOpen }" />
              <div v-if="avatarMenuOpen" class="avatar-dropdown" @click.stop>
                <div class="avatar-dropdown-header">
                  <img v-if="userAvatar" :src="userAvatar" class="ad-avatar" />
                  <span v-else class="ad-avatar ad-avatar-text">{{ userStore.username?.charAt(0) || '?' }}</span>
                  <div>
                    <div class="ad-name">{{ userStore.username }}</div>
                    <div class="ad-balance">{{ userStore.balance || 0 }} 时间币</div>
                  </div>
                </div>
                <div class="ad-divider"></div>
                <router-link to="/my-bounties" class="ad-item" @click="avatarMenuOpen = false; closeMenu()">
                  <Icon icon="mdi:clipboard-list" />我的需求
                </router-link>
                <router-link to="/orders/buyer" class="ad-item" @click="avatarMenuOpen = false; closeMenu()">
                  <Icon icon="mdi:clipboard-text-clock" />我的订单
                </router-link>
                <router-link to="/my-skills" class="ad-item" @click="avatarMenuOpen = false; closeMenu()">
                  <Icon icon="mdi:briefcase" />我的技能
                </router-link>
                <router-link to="/profile" class="ad-item" @click="avatarMenuOpen = false; closeMenu()">
                  <Icon icon="mdi:home-account" />个人主页
                </router-link>
                <div class="ad-divider"></div>
                <a class="ad-item ad-logout" @click="handleLogout">
                  <Icon icon="mdi:logout" />退出登录
                </a>
              </div>
            </div>
          </template>
        </div>

        <!-- 汉堡按钮 -->
        <button class="hamburger" :class="{ open: menuOpen }" @click="menuOpen = !menuOpen" aria-label="菜单">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>

      <!-- 移动端抽屉 -->
      <transition name="drawer">
        <div v-if="menuOpen" class="mobile-drawer" @click.self="closeMenu">
          <nav class="drawer-nav">
            <router-link to="/" class="drawer-item" active-class="nav-active" @click="closeMenu">
              <Icon icon="mdi:compass-rose" />技能广场
            </router-link>
            <router-link to="/bounty" class="drawer-item" active-class="nav-active" @click="closeMenu">
              <Icon icon="mdi:clipboard-text-search" />需求悬赏
            </router-link>
            <a class="drawer-item" style="cursor:pointer" @click="loadAnnouncements(); closeMenu()">
              <Icon icon="mdi:bullhorn" />公告
            </a>

            <template v-if="userStore.isLoggedIn">
              <div class="drawer-divider"></div>
              <a class="drawer-item" style="cursor:pointer" @click="loadNotifications(); closeMenu()">
                <Icon icon="mdi:bell" />通知
                <span v-if="userStore.notifUnreadCount > 0" class="nav-badge">{{ userStore.notifUnreadCount > 99 ? '99+' : userStore.notifUnreadCount }}</span>
              </a>
              <router-link to="/messages" class="drawer-item" active-class="nav-active" @click="closeMenu">
                <Icon icon="mdi:message-text" />消息
                <span v-if="userStore.unreadCount > 0" class="nav-badge">{{ userStore.unreadCount > 99 ? '99+' : userStore.unreadCount }}</span>
              </router-link>
              <router-link to="/my-bounties" class="drawer-item" active-class="nav-active" @click="closeMenu">
                <Icon icon="mdi:clipboard-list" />我的需求
              </router-link>
              <router-link to="/orders/buyer" class="drawer-item" active-class="nav-active" @click="closeMenu">
                <Icon icon="mdi:clipboard-text-clock" />我的订单
              </router-link>
              <router-link to="/my-skills" class="drawer-item" active-class="nav-active" @click="closeMenu">
                <Icon icon="mdi:briefcase" />我的技能
              </router-link>
              <router-link to="/profile" class="drawer-item" active-class="nav-active" @click="closeMenu">
                <Icon icon="mdi:home-account" />个人主页
              </router-link>
              <div class="drawer-divider"></div>
              <div class="drawer-footer-info">
                <button class="logout-btn" @click="handleLogout">退出登录</button>
              </div>
            </template>
            <template v-else>
              <div class="drawer-divider"></div>
              <router-link to="/login" class="drawer-item login-item" @click="closeMenu">
                <Icon icon="mdi:login" />登录
              </router-link>
            </template>
          </nav>
        </div>
      </transition>
    </header>

    <!-- 主体内容 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-inner">
        <div class="footer-top">
          <div class="footer-brand">
            <span class="footer-logo">Prometheus</span>
            <p class="footer-tagline">以时间币为纽带，让每个人的技能都有价值</p>
          </div>
          <div class="footer-links">
            <span class="footer-link-heading">快速导航</span>
            <router-link to="/" class="footer-link">技能广场</router-link>
            <router-link to="/bounty" class="footer-link">需求悬赏</router-link>
            <a class="footer-link" href="javascript:void(0)" @click="$router.push('/').then(() => {})">平台公告</a>
          </div>
          <div class="footer-links">
            <span class="footer-link-heading">平台特色</span>
            <span class="footer-link">时间币交易体系</span>
            <span class="footer-link">双盲公正评价</span>
            <span class="footer-link">技能供需匹配</span>
          </div>
        </div>
        <div class="footer-divider"></div>
        <div class="footer-bottom">
          <span>&copy; 2026 Prometheus 技能时间银行</span>
        </div>
      </div>
    </footer>

    <!-- 通知弹窗 -->
    <el-dialog v-model="showNotifPanel" title="通知" width="600px" top="8vh" destroy-on-close>
      <div v-loading="loadingNotif" class="announce-panel-list">
        <el-empty v-if="!loadingNotif && notifications.length === 0" description="暂无通知" :image-size="60" />
        <article v-for="item in notifications" :key="item.id"
          class="panel-announce-item notif-item"
          :class="{ 'notif-unread': item.isRead === 0 }"
          @click="handleNotifClick(item)">
          <h3 class="panel-announce-title">
            <span v-if="item.isRead === 0" class="notif-dot" />
            {{ item.title }}
          </h3>
          <p class="panel-announce-content">{{ item.content }}</p>
          <span class="panel-announce-time">{{ formatTime(item.createTime) }}</span>
        </article>
      </div>
      <template #footer>
        <el-button v-if="notifications.some(n => n.isRead === 0)" @click="markAllNotifRead">全部已读</el-button>
        <el-button type="primary" @click="showNotifPanel = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 公告弹窗 -->
    <el-dialog v-model="showAnnouncePanel" title="📢 平台公告" width="560px" top="8vh" destroy-on-close>
      <div v-loading="loadingAnnounce" class="announce-panel-list">
        <el-empty v-if="!loadingAnnounce && announcements.length === 0" description="暂无公告" :image-size="60" />
        <article v-for="item in announcements" :key="item.id" class="panel-announce-item">
          <h3 class="panel-announce-title">
            <span v-if="item.isTop" class="panel-top-badge">置顶</span>
            {{ item.title }}
          </h3>
          <p class="panel-announce-content">{{ item.content }}</p>
          <span class="panel-announce-time">{{ formatTime(item.createTime) }}</span>
        </article>
      </div>
      <template #footer>
        <el-button type="primary" @click="showAnnouncePanel = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style>
/* ========== 全局重置 ========== */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #faf8f5;
}

/* ========== 顶部导航 ========== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(232, 120, 74, 0.12);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* Logo */
.logo-group {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}
.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  letter-spacing: -1px;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: -0.3px;
}

/* 导航链接 */
.main-nav {
  display: flex;
  gap: 4px;
  flex: 1;
  justify-content: center;
}
.nav-item {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #555;
  text-decoration: none;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}
.nav-item:hover {
  background: rgba(232, 120, 74, 0.08);
  color: #e8784a;
}
.nav-active {
  background: rgba(232, 120, 74, 0.12) !important;
  color: #e8784a !important;
  font-weight: 600;
}
.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.nav-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 10px;
  margin-left: 2px;
  line-height: 1.4;
}
.nav-login {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff !important;
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 600;
}
.nav-login:hover {
  background: linear-gradient(135deg, #d06840, #e89050) !important;
  color: #fff !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(232, 120, 74, 0.3);
}

/* ========== 右侧区域 ========== */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 消息图标按钮 */
.icon-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  text-decoration: none;
  color: #555;
}
.icon-btn:hover {
  background: rgba(232, 120, 74, 0.08);
  color: #e8784a;
}
.icon-btn-icon {
  font-size: 22px;
}
.icon-btn-badge {
  position: absolute;
  top: 0;
  right: -2px;
  background: #f56c6c;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  border-radius: 8px;
  padding: 0 5px;
  box-shadow: 0 1px 3px rgba(245,108,108,0.4);
}

/* ========== 头像下拉 ========== */
.avatar-wrap {
  position: relative;
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 2px;
  border-radius: 20px;
  transition: background 0.2s;
}
.avatar-wrap:hover {
  background: rgba(232, 120, 74, 0.06);
}
.avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0e8e0;
}
.avatar-text {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
}
.avatar-arrow {
  font-size: 16px;
  color: #999;
  transition: transform 0.2s;
}
.avatar-arrow.flip {
  transform: rotate(180deg);
}

/* 头像下拉菜单 */
.avatar-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: 1px solid #f0e8e0;
  min-width: 200px;
  padding: 4px 0;
  z-index: 1100;
}
.avatar-dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}
.ad-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.ad-avatar-text {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}
.ad-name {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
}
.ad-balance {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.ad-divider {
  height: 1px;
  background: #f0e8e0;
  margin: 4px 12px;
}
.ad-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  color: #555;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.15s;
}
.ad-item:hover {
  background: rgba(232, 120, 74, 0.06);
  color: #e8784a;
}
.ad-logout {
  color: #999;
  cursor: pointer;
}
.ad-logout:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.06);
}

/* ========== 主体区域 ========== */
.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 48px;
}

/* 页面切换动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== 全局页脚 ========== */
.app-footer {
  background: #2c3e50;
  color: rgba(255,255,255,0.7);
  margin-top: auto;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 24px;
}
.footer-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 24px;
}
.footer-logo {
  font-size: 20px;
  font-weight: 700;
  color: #f0a060;
  letter-spacing: -0.3px;
}
.footer-tagline {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255,255,255,0.45);
}
.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.footer-link-heading {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  font-weight: 600;
  margin-bottom: 2px;
}
.footer-link {
  font-size: 13px;
  color: rgba(255,255,255,0.55);
  text-decoration: none;
  transition: color 0.2s;
  cursor: pointer;
}
a.footer-link:hover {
  color: #f0a060;
}
.footer-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin: 20px 0 16px;
}
.footer-bottom {
  text-align: center;
  font-size: 12px;
  color: rgba(255,255,255,0.35);
}

/* ========== Element Plus 全局覆盖 ========== */
.el-card {
  border-radius: 12px !important;
  border: 1px solid #f0e8e0 !important;
  transition: all 0.3s ease !important;
}
.el-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08) !important;
}
.el-button--primary {
  background: linear-gradient(135deg, #e8784a, #f0a060) !important;
  border: none !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px !important;
  transition: all 0.3s ease !important;
}
.el-button--primary:hover {
  background: linear-gradient(135deg, #d06840, #e89050) !important;
  box-shadow: 0 4px 16px rgba(232, 120, 74, 0.35) !important;
  transform: translateY(-1px);
}
.el-tag {
  border-radius: 6px !important;
  font-weight: 500 !important;
}
.el-input__wrapper,
.el-select__wrapper {
  border-radius: 8px !important;
  transition: all 0.3s !important;
}
.el-pagination {
  --el-pagination-hover-color: #e8784a !important;
}
.el-pagination .el-pager li.is-active {
  background: #e8784a !important;
}

/* ========== 汉堡按钮 ========== */
.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  flex-shrink: 0;
  z-index: 1100;
}
.hamburger span {
  display: block;
  width: 100%;
  height: 2px;
  background: #555;
  border-radius: 2px;
  transition: all 0.3s;
}
.hamburger.open span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}
.hamburger.open span:nth-child(2) {
  opacity: 0;
}
.hamburger.open span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

/* ========== 移动端抽屉 ========== */
.mobile-drawer {
  position: fixed;
  inset: 0;
  top: 60px;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1050;
}
.drawer-nav {
  width: 280px;
  min-height: calc(100vh - 60px);
  background: #fff;
  padding: 16px 0;
  overflow-y: auto;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.1);
}
.drawer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  font-size: 15px;
  color: #555;
  text-decoration: none;
  transition: all 0.2s;
  font-weight: 500;
}
.drawer-item:hover {
  background: rgba(232, 120, 74, 0.06);
  color: #e8784a;
}
.drawer-item.nav-active {
  background: rgba(232, 120, 74, 0.1);
  color: #e8784a;
  font-weight: 600;
}
.drawer-item.login-item {
  color: #e8784a;
  font-weight: 600;
}
.drawer-divider {
  height: 1px;
  background: #f0e8e0;
  margin: 8px 16px;
}
.drawer-footer-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 8px 24px;
}
.logout-btn {
  align-self: flex-start;
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #e8e0d8;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  border: none;
}
.logout-btn:hover {
  background: #fee;
  color: #f56c6c;
}

/* 抽屉动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.25s;
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}
.drawer-enter-active .drawer-nav,
.drawer-leave-active .drawer-nav {
  transition: transform 0.25s;
}
.drawer-enter-from .drawer-nav,
.drawer-leave-to .drawer-nav {
  transform: translateX(-100%);
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .main-nav {
    display: none !important;
  }
  .header-inner .header-right {
    display: none !important;
  }
  .hamburger {
    display: flex;
  }
}

@media (max-width: 640px) {
  .header-inner {
    padding: 0 16px;
    gap: 12px;
  }
  .logo-text {
    font-size: 16px;
  }
  .logo-icon {
    width: 30px;
    height: 30px;
    font-size: 15px;
  }

  .app-main {
    padding: 16px 12px 32px;
  }

  .footer-top {
    flex-direction: column;
    text-align: center;
    align-items: center;
    gap: 20px;
  }
  .footer-links {
    align-items: center;
  }
}

/* ========== 公告弹窗 ========== */
.announce-panel-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
}
.panel-announce-item {
  border-bottom: 1px solid #f0e8e0;
  padding-bottom: 14px;
}
.panel-announce-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.panel-announce-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-top-badge {
  font-size: 11px;
  background: #e8784a;
  color: #fff;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.panel-announce-content {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  margin: 0 0 6px;
  white-space: pre-wrap;
}
.panel-announce-time {
  font-size: 12px;
  color: #bbb;
}

/* ========== 通知弹窗 ========== */
.notif-item {
  cursor: pointer;
  transition: background 0.15s;
  padding: 12px;
  margin: 0 -12px;
  border-radius: 8px;
  word-break: break-word;
  overflow-wrap: break-word;
}
.notif-item:hover {
  background: #faf5f0;
}
.notif-unread {
  background: #fff8f4;
  border-left: 3px solid #e8784a;
  margin-left: -15px;
  padding-left: 9px;
}
.notif-dot {
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
