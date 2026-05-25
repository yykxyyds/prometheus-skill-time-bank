<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/index'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

const route = useRoute()
const router = useRouter()
const form = ref({
  orderId: route.query.orderId || '',
  reason: '',
  evidence: ''
})
const evidenceImages = ref([])
const uploading = ref(false)
const loading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

async function uploadEvidence(e) {
  const files = e.target?.files
  if (!files || files.length === 0) return
  uploading.value = true
  try {
    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        ElMessage.warning(`"${file.name}" 不是图片，已跳过`)
        continue
      }
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning(`"${file.name}" 超过5MB，已跳过`)
        continue
      }
      const fd = new FormData()
      fd.append('file', file)
      const res = await api.post('/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      evidenceImages.value.push(res.data)
    }
  } catch { /* handled */ } finally {
    uploading.value = false
    e.target.value = ''
  }
}

function removeImage(idx) {
  evidenceImages.value.splice(idx, 1)
}

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
      evidence: form.value.evidence,
      evidenceImages: evidenceImages.value.length > 0 ? JSON.stringify(evidenceImages.value) : null
    })
    ElMessage.success('申诉已提交，请等待管理员处理')
    router.push('/orders/buyer')
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
        <el-input-number v-if="!route.query.orderId" v-model="form.orderId" :min="1" placeholder="输入订单ID" style="width:100%" size="large" />
        <el-input v-else :model-value="form.orderId" disabled size="large" />
        <span class="form-hint">订单ID来自订单详情页跳转</span>
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
        <label>图片证据（可上传多张）</label>
        <div class="evidence-upload-area">
          <label class="upload-btn" :class="{ uploading }">
            <Icon :icon="uploading ? 'mdi:loading' : 'mdi:camera-plus'" />
            <span>{{ uploading ? '上传中...' : '选择图片' }}</span>
            <input type="file" accept="image/*" multiple hidden @change="uploadEvidence" :disabled="uploading" />
          </label>
          <div class="evidence-previews" v-if="evidenceImages.length > 0">
            <div v-for="(url, idx) in evidenceImages" :key="idx" class="evidence-preview-item">
              <img :src="url" class="evidence-thumb" @click="previewUrl = url" />
              <button class="remove-btn" @click="removeImage(idx)"><Icon icon="mdi:close-circle" /></button>
            </div>
          </div>
        </div>
        <span class="form-hint">支持 JPG/PNG 格式，单张不超过 5MB</span>
      </div>

      <div class="form-actions">
        <button class="btn-cancel" @click="router.back()">取消</button>
        <button class="btn-submit" :disabled="loading || uploading" @click="submit">
          <Icon icon="mdi:send" v-if="!loading" />
          {{ loading ? '提交中...' : '提交申诉' }}
        </button>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible" title="证据图片" width="560px" top="8vh" destroy-on-close>
      <img v-if="previewUrl" :src="previewUrl" style="width:100%;border-radius:8px" />
    </el-dialog>
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

/* 图片上传 */
.evidence-upload-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 2px dashed #e0d8d0;
  border-radius: 10px;
  background: #faf8f5;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  width: fit-content;
}
.upload-btn:hover { border-color: #e8784a; color: #e8784a; background: #fdf9f6; }
.upload-btn.uploading { opacity: 0.6; cursor: not-allowed; }
.upload-btn Icon { font-size: 20px; }

.evidence-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.evidence-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0e8e0;
}
.evidence-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}
.evidence-thumb:hover { transform: scale(1.08); }
.remove-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  border: none;
  background: rgba(0,0,0,0.45);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  padding: 0;
  transition: background 0.2s;
}
.remove-btn:hover { background: rgba(245,108,108,0.85); }

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
