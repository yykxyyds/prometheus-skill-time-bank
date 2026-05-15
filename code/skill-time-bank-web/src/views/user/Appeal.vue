<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/index'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const router = useRouter()
const form = ref({
  orderId: null,
  reason: '',
  evidence: ''
})
const loading = ref(false)

async function submit() {
  if (!form.value.orderId) {
    ElMessage.warning('请输入关联订单ID')
    return
  }
  if (!form.value.reason.trim()) {
    ElMessage.warning('请填写申诉原因')
    return
  }
  loading.value = true
  try {
    await api.post('/appeal', {
      orderId: form.value.orderId,
      reason: form.value.reason,
      evidence: form.value.evidence
    })
    ElMessage.success('申诉已提交，请等待管理员处理')
    router.push('/wallet')
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="appeal-page">
    <div class="page-header">
      <h2>提交申诉</h2>
      <p>如果你对订单处理有异议，可以提交申诉</p>
    </div>

    <div class="form-card">
      <div class="form-row">
        <label>关联订单ID</label>
        <el-input-number v-model="form.orderId" :min="1" placeholder="输入订单ID" style="width:100%" size="large" />
        <span class="form-hint">可在"我的需求"页面查看订单ID</span>
      </div>

      <div class="form-row">
        <label>申诉原因</label>
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="5"
          placeholder="详细描述你的申诉原因..."
          maxlength="500"
          show-word-limit
        />
      </div>

      <div class="form-row">
        <label>证据说明（可选）</label>
        <el-input
          v-model="form.evidence"
          type="textarea"
          :rows="3"
          placeholder="提供相关证据说明..."
          maxlength="500"
          show-word-limit
        />
      </div>

      <div class="form-actions">
        <button class="btn-cancel" @click="router.back()">取消</button>
        <button class="btn-submit" :disabled="loading" @click="submit">
          <Icon icon="mdi:send" v-if="!loading" />
          {{ loading ? '提交中...' : '提交申诉' }}
        </button>
      </div>
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
  max-width: 660px;
}

.form-row {
  margin-bottom: 22px;
}
.form-row label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}
.form-hint {
  display: block;
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #f5f0eb;
}

.btn-submit {
  padding: 10px 28px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-submit:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(232, 120, 74, 0.35);
}
.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 10px 24px;
  background: #f5f0eb;
  color: #666;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #ebe5de;
}
</style>
