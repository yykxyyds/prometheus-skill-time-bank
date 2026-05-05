<script setup>
import { useUserStore } from './stores/user'
const userStore = useUserStore()
</script>

<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <router-link to="/" class="logo">Prometheus</router-link>
        <nav class="nav-links">
          <router-link to="/">技能广场</router-link>
          <router-link to="/bounty">需求悬赏</router-link>
          <template v-if="userStore.isLoggedIn">
            <router-link to="/wallet">时间银行</router-link>
            <router-link to="/profile">个人中心</router-link>
            <span class="balance">{{ userStore.balance }} 时间币</span>
            <template v-if="userStore.isAdmin">
              <router-link to="/admin/users">管理</router-link>
            </template>
            <a href="#" @click.prevent="userStore.logout()">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
          </template>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  background: #faf8f5;
}
.app-header {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  text-decoration: none;
}
.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}
.nav-links a {
  color: rgba(255,255,255,0.9);
  text-decoration: none;
  font-size: 14px;
}
.nav-links a:hover {
  color: #fff;
}
.balance {
  background: rgba(255,255,255,0.2);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
}
.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>
