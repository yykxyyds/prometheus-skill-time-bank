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
    <div class="login-card">
      <div class="login-header">
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
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}
.login-card {
  background: #fff; border-radius: 16px; padding: 40px;
  width: 380px; box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.login-header { text-align: center; margin-bottom: 32px; }
.login-header h1 { font-size: 28px; font-weight: 700; color: #1e293b; margin-bottom: 6px; }
.login-header p { font-size: 14px; color: #94a3b8; }
.login-form { display: flex; flex-direction: column; gap: 4px; }
</style>
