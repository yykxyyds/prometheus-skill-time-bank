<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, register } from '../api/user'
import { ElMessage } from 'element-plus'

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
    ElMessage.success(isLogin.value ? '登录成功' : '注册成功，赠送20时间币')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2>{{ isLogin ? '登录' : '注册' }}</h2>
      <el-form @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item v-if="!isLogin">
          <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" block
          style="background:#e8784a;border-color:#e8784a">
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
      </el-form>
      <p class="toggle">
        {{ isLogin ? '没有账号？' : '已有账号？' }}
        <a href="#" @click.prevent="isLogin = !isLogin">{{ isLogin ? '去注册' : '去登录' }}</a>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}
.login-card {
  width: 400px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}
.toggle {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #999;
}
.toggle a { color: #e8784a; }
</style>
