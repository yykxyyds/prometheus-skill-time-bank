<script setup>
import { ref } from 'vue'
import { useAdminStore } from '../stores/user'
import { useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const store = useAdminStore()
const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await api.post('/user/login', form.value)
    const data = res.data
    if (data.role !== 'ADMIN') {
      ElMessage.error('该账号无管理员权限')
      return
    }
    store.setLogin(data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch { /* error shown by interceptor */ }
  finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="login-logo-icon">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <h1>管理后台</h1>
        <p>Prometheus 技能时间银行</p>
      </div>
      <el-form @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" size="large" style="width:100%">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  position: relative; overflow: hidden;
}

/* Decorative background shapes */
.login-bg-shapes { position: absolute; inset: 0; pointer-events: none; }
.shape {
  position: absolute; border-radius: 50%; opacity: 0.08;
}
.shape-1 {
  width: 500px; height: 500px; background: #818cf8;
  top: -150px; right: -100px;
}
.shape-2 {
  width: 350px; height: 350px; background: #6366f1;
  bottom: -100px; left: -80px;
}
.shape-3 {
  width: 200px; height: 200px; background: #a78bfa;
  bottom: 30%; right: 10%;
}

.login-card {
  background: #fff; border-radius: 20px; padding: 44px 40px 36px;
  width: 380px; box-shadow: 0 25px 80px rgba(0,0,0,0.3);
  position: relative; animation: cardIn 0.5s ease-out;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.login-header { text-align: center; margin-bottom: 32px; }
.login-logo {
  width: 56px; height: 56px; border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px; color: #fff;
}
.login-logo-icon { width: 28px; height: 28px; }
.login-header h1 { font-size: 26px; font-weight: 700; color: #1e293b; margin-bottom: 6px; }
.login-header p { font-size: 14px; color: #94a3b8; }
.login-form { display: flex; flex-direction: column; gap: 4px; }
</style>
