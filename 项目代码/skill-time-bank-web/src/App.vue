<script setup>
import { useUserStore } from './stores/user'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

const userStore = useUserStore()
const router = useRouter()
</script>

<template>
  <div id="app">
    <!-- 顶部导航 -->
    <header class="app-header" :class="{ scrolled: true }">
      <div class="header-inner">
        <router-link to="/" class="logo-group">
          <span class="logo-icon">P</span>
          <span class="logo-text">Prometheus</span>
        </router-link>

        <nav class="main-nav">
          <router-link to="/" class="nav-item" active-class="nav-active">
            <Icon icon="mdi:compass-rose" class="nav-icon" />技能广场
          </router-link>
          <router-link to="/bounty" class="nav-item" active-class="nav-active">
            <Icon icon="mdi:clipboard-text-search" class="nav-icon" />需求悬赏
          </router-link>

          <template v-if="userStore.isLoggedIn">
            <router-link to="/orders/buyer" class="nav-item" active-class="nav-active">
              <Icon icon="mdi:clipboard-list" class="nav-icon" />我的订单
            </router-link>
            <router-link to="/wallet" class="nav-item" active-class="nav-active">
              <Icon icon="mdi:bank" class="nav-icon" />时间银行
            </router-link>
            <router-link to="/profile" class="nav-item" active-class="nav-active">
              <Icon icon="mdi:account-circle" class="nav-icon" />个人中心
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item nav-login" active-class="nav-active">
              登录
            </router-link>
          </template>
        </nav>

        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <span class="coin-badge">{{ userStore.balance || 0 }} 币</span>
            <template v-if="userStore.isAdmin">
              <router-link to="/admin/users" class="admin-link">管理</router-link>
            </template>
            <button class="logout-btn" @click="userStore.logout(); router.push('/')">退出</button>
          </template>
        </div>
      </div>
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
        <div class="footer-brand">
          <span class="footer-logo">Prometheus</span>
          <p>拒绝金钱交易，回归价值交换的技能互助平台</p>
        </div>
        <div class="footer-links">
          <span>海南大学 · 第10组</span>
          <span>指导教师：刘德才</span>
        </div>
      </div>
    </footer>
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

/* ========== 顶部导航 - 玻璃拟态 ========== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(232, 120, 74, 0.12);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
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

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.coin-badge {
  background: linear-gradient(135deg, #f0a060, #e8784a);
  color: #fff;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
  box-shadow: 0 2px 8px rgba(232, 120, 74, 0.25);
}
.admin-link {
  font-size: 13px;
  color: #e8784a;
  text-decoration: none;
  font-weight: 500;
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid rgba(232, 120, 74, 0.3);
}
.admin-link:hover {
  background: rgba(232, 120, 74, 0.08);
}
.logout-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 13px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 6px;
  transition: all 0.2s;
}
.logout-btn:hover {
  color: #e8784a;
  background: rgba(232, 120, 74, 0.06);
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
  padding: 32px 0;
  margin-top: auto;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.footer-logo {
  font-size: 18px;
  font-weight: 700;
  color: #f0a060;
  letter-spacing: -0.3px;
}
.footer-brand p {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255,255,255,0.45);
}
.footer-links {
  display: flex;
  gap: 24px;
  font-size: 13px;
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
</style>
