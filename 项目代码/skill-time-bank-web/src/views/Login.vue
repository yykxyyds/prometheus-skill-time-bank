<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, register } from '../api/user'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const router = useRouter()
const userStore = useUserStore()
const isLogin = ref(true)
const form = ref({ username: '', password: '', email: '' })
const loading = ref(false)

async function submit() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (!isLogin.value && !form.value.email) {
    ElMessage.warning('注册请填写邮箱')
    return
  }
  loading.value = true
  try {
    const api = isLogin.value ? login : register
    const res = await api(form.value)
    userStore.setUser(res.data)
    ElMessage.success(isLogin.value ? '欢迎回来！' : '注册成功，赠送 20 时间币')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌面板 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="brand-icon">P</span>
        </div>
        <h1 class="brand-name">Prometheus</h1>
        <p class="brand-tagline">技能时间银行</p>
        <div class="brand-features">
          <div class="feature-item">
            <Icon icon="mdi:star" class="feature-icon" />
            <span>用技能交换时间币</span>
          </div>
          <div class="feature-item">
            <Icon icon="mdi:account-group" class="feature-icon" />
            <span>结识志同道合的朋友</span>
          </div>
          <div class="feature-item">
            <Icon icon="mdi:lightbulb-on" class="feature-icon" />
            <span>让每份才华都被看见</span>
          </div>
        </div>
      </div>
      <div class="brand-circles">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="circle c3"></div>
      </div>
    </div>

    <!-- 右侧表单面板 -->
    <div class="login-right">
      <div class="form-container">
        <div class="form-header">
          <h2>{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
          <p>{{ isLogin ? '登录你的账号，继续技能交换之旅' : '注册即赠 20 时间币，开始你的第一次交换' }}</p>
        </div>

        <form class="login-form" @submit.prevent="submit">
          <div class="input-group">
            <label>用户名</label>
            <div class="input-wrap">
              <Icon icon="mdi:account" class="input-icon" />
              <input
                v-model="form.username"
                type="text"
                placeholder="请输入用户名"
                class="form-input"
              />
            </div>
          </div>

          <div class="input-group" v-if="!isLogin">
            <label>邮箱</label>
            <div class="input-wrap">
              <Icon icon="mdi:email" class="input-icon" />
              <input
                v-model="form.email"
                type="email"
                placeholder="请输入邮箱"
                class="form-input"
              />
            </div>
          </div>

          <div class="input-group">
            <label>密码</label>
            <div class="input-wrap">
              <Icon icon="mdi:lock" class="input-icon" />
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                class="form-input"
              />
            </div>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>{{ isLogin ? '登录' : '注册' }}</span>
          </button>
        </form>

        <div class="form-footer">
          <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
          <a href="#" @click.prevent="isLogin = !isLogin">
            {{ isLogin ? '立即注册' : '去登录' }}
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: calc(100vh - 200px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0,0,0,0.06);
}

/* ========== 左侧品牌面板 ========== */
.login-left {
  flex: 1;
  background: linear-gradient(160deg, #2c3e50 0%, #34495e 40%, #3d566e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 60px 48px;
}
.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}
.brand-logo {
  margin-bottom: 20px;
}
.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border-radius: 18px;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  box-shadow: 0 8px 24px rgba(232, 120, 74, 0.35);
}
.brand-name {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}
.brand-tagline {
  font-size: 15px;
  color: rgba(255,255,255,0.55);
  margin: 0 0 40px;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.75);
  font-size: 14px;
}
.feature-icon {
  font-size: 20px;
  color: #f0a060;
  flex-shrink: 0;
}

/* 装饰圆圈 */
.brand-circles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.06);
}
.c1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -100px;
}
.c2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: -60px;
}
.c3 {
  width: 100px;
  height: 100px;
  top: 50%;
  left: 60%;
  border-color: rgba(240, 160, 96, 0.12);
}

/* ========== 右侧表单面板 ========== */
.login-right {
  width: 480px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
}
.form-container {
  width: 100%;
  max-width: 360px;
}
.form-header {
  text-align: center;
  margin-bottom: 36px;
}
.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px;
}
.form-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

/* 输入框 */
.input-group {
  margin-bottom: 20px;
}
.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}
.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e8e0d8;
  border-radius: 10px;
  padding: 0 14px;
  transition: all 0.3s ease;
  background: #fafafa;
}
.input-wrap:focus-within {
  border-color: #e8784a;
  box-shadow: 0 0 0 3px rgba(232, 120, 74, 0.08);
  background: #fff;
}
.input-icon {
  font-size: 20px;
  color: #bbb;
  flex-shrink: 0;
}
.form-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 0;
  font-size: 15px;
  color: #333;
  background: transparent;
}
.form-input::placeholder {
  color: #ccc;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  margin-top: 8px;
}
.submit-btn:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(232, 120, 74, 0.35);
  transform: translateY(-1px);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部切换 */
.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #999;
}
.form-footer a {
  color: #e8784a;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}
.form-footer a:hover {
  text-decoration: underline;
}
</style>
