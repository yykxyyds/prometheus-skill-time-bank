<script setup>
import { ref, onMounted } from 'vue'
import { publishBounty, updateBounty } from '../../api/skill'
import { useRouter, useRoute } from 'vue-router'
import api from '../../api/index'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const editId = route.query.edit || null
const isEdit = !!editId
const loading = ref(false)
const pageLoading = ref(false)
const form = ref({
  title: '',
  description: '',
  reward: null,
  deadline: ''
})

const rules = {
  title: [{ required: true, message: '请输入悬赏标题', trigger: 'blur' }],
  description: [{ required: true, message: '请描述你的需求', trigger: 'blur' }],
  reward: [{ required: true, message: '请输入悬赏金额', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit) {
    pageLoading.value = true
    try {
      const res = await api.get(`/bounty/${editId}`)
      const b = res.data || {}
      form.value.title = b.title || ''
      form.value.description = b.description || ''
      form.value.reward = b.reward || null
      form.value.deadline = b.deadline ? b.deadline.substring(0, 10) : ''
    } catch { /* handled */ }
    finally { pageLoading.value = false }
  }
})

async function submit() {
  loading.value = true
  try {
    const data = {
      title: form.value.title,
      description: form.value.description,
      reward: form.value.reward,
      deadline: form.value.deadline ? form.value.deadline + 'T23:59:59' : null
    }
    if (isEdit) {
      await updateBounty(editId, data)
      ElMessage.success('修改成功！')
      router.push(`/bounty/${editId}`)
    } else {
      await publishBounty(data)
      ElMessage.success('悬赏发布成功！')
      router.push('/bounty')
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="create-bounty">
    <div class="page-header">
      <div>
        <h2>{{ isEdit ? '编辑需求悬赏' : '发布需求悬赏' }}</h2>
        <p>{{ isEdit ? '修改你的悬赏内容和金额' : '描述你的需求，设置悬赏金额，等待有能力的用户来接单' }}</p>
      </div>
    </div>

    <div class="form-card" v-loading="pageLoading">
      <el-form :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="悬赏标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：帮忙设计一个Logo" maxlength="100" show-word-limit size="large" />
        </el-form-item>

        <el-form-item label="需求描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="详细描述你的需求，包括具体要求、交付物、时间安排等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="悬赏金额（时间币）" prop="reward" class="form-half">
            <el-input-number
              v-model="form.reward"
              :min="10"
              :max="9999"
              :step="10"
              size="large"
              placeholder="输入金额"
            />
          </el-form-item>

          <el-form-item label="截止日期（可选）" prop="deadline" class="form-half">
            <el-input v-model="form.deadline" type="date" size="large" />
          </el-form-item>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="router.back()">取消</button>
          <button type="submit" class="submit-btn" :disabled="loading">
            <Icon icon="mdi:plus-circle" v-if="!loading" />
            {{ loading ? (isEdit ? '保存中...' : '发布中...') : (isEdit ? '保存修改' : '发布悬赏') }}
          </button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 28px;
}
.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 6px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #f0e8e0;
  max-width: 720px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 8px;
  padding-top: 24px;
  border-top: 1px solid #f5f0eb;
}

.cancel-btn {
  padding: 10px 24px;
  background: #f5f0eb;
  color: #666;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.cancel-btn:hover {
  background: #ebe5de;
}

.submit-btn {
  padding: 10px 28px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.3px;
}
.submit-btn:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(232, 120, 74, 0.35);
  transform: translateY(-1px);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
