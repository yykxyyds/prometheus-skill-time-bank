<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getProfile, updateProfile } from '../../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const profile = ref({})
const editing = ref(false)
const form = ref({ email: '', phone: '', bio: '' })

onMounted(async () => {
  const res = await getProfile()
  profile.value = res.data || {}
  form.value = { email: profile.value.email || '', phone: profile.value.phone || '', bio: profile.value.bio || '' }
})

async function save() {
  await updateProfile(form.value)
  ElMessage.success('保存成功')
  editing.value = false
  const res = await getProfile()
  profile.value = res.data || {}
}
</script>

<template>
  <div class="profile">
    <h2>个人中心</h2>
    <el-card shadow="hover">
      <div class="profile-header">
        <div class="avatar">{{ userStore.username?.charAt(0)?.toUpperCase() }}</div>
        <div>
          <h3>{{ userStore.username }}</h3>
          <el-tag>{{ userStore.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag>
        </div>
      </div>
      <el-divider />
      <el-descriptions v-if="!editing" :column="2" border>
        <el-descriptions-item label="邮箱">{{ profile.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ profile.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ profile.bio || '这个人很懒，什么都没写' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ profile.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.bio" type="textarea" /></el-form-item>
      </el-form>
      <el-button v-if="!editing" type="primary" @click="editing = true" style="margin-top:16px;background:#e8784a;border-color:#e8784a">编辑资料</el-button>
      <template v-else>
        <el-button type="primary" @click="save" style="background:#e8784a;border-color:#e8784a">保存</el-button>
        <el-button @click="editing = false">取消</el-button>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 16px; }
.avatar {
  width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, #f0a060, #e8784a);
  display: flex; align-items: center; justify-content: center; color: #fff; font-size: 28px; font-weight: bold;
}
</style>
