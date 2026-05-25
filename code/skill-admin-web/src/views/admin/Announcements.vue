<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const announcements = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, title: '', content: '', isTop: false })

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await api.get('/announcement/list', { params: { page: 1, size: 50 } })
    announcements.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  form.value = { id: null, title: '', content: '', isTop: false }
  showDialog.value = true
}

function openEdit(item) {
  isEdit.value = true
  form.value = { ...item }
  showDialog.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await api.put('/admin/announcement', form.value)
    ElMessage.success('已更新')
  } else {
    await api.post('/admin/announcement', form.value)
    ElMessage.success('发布成功')
  }
  showDialog.value = false
  await loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await api.delete(`/admin/announcement/${id}`)
  ElMessage.success('已删除')
  await loadData()
}
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h2>公告管理</h2>
        <p>发布和管理平台公告</p>
      </div>
      <button class="publish-btn" @click="openCreate">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        发布公告
      </button>
    </div>

    <div class="announce-list" v-loading="loading">
      <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" :image-size="80" />
      <article v-for="item in announcements" :key="item.id" class="announce-card">
        <div class="announce-left">
          <h3>
            <span v-if="item.isTop" class="top-badge">置顶</span>
            {{ item.title }}
          </h3>
          <p class="announce-content">{{ item.content }}</p>
          <span class="announce-time">{{ item.createTime?.replace('T', ' ') }}</span>
        </div>
        <div class="announce-actions">
          <button class="action-btn edit" @click="openEdit(item)">编辑</button>
          <button class="action-btn danger" @click="handleDelete(item.id)">删除</button>
        </div>
      </article>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑公告' : '发布公告'" width="520px" destroy-on-close>
      <div class="dialog-form">
        <div class="form-row">
          <label>标题</label>
          <el-input v-model="form.title" placeholder="公告标题" />
        </div>
        <div class="form-row">
          <label>内容</label>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="公告内容..." />
        </div>
        <div class="form-row">
          <el-checkbox v-model="form.isTop">置顶该公告</el-checkbox>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">{{ isEdit ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-header p { font-size: 14px; color: #94a3b8; margin: 0; }
.publish-btn {
  padding: 10px 22px; background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff; border: none; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; transition: all 0.3s;
  display: flex; align-items: center; gap: 6px;
}
.publish-btn:hover { box-shadow: 0 4px 14px rgba(99,102,241,0.3); transform: translateY(-1px); }
.btn-icon { width: 16px; height: 16px; }

.announce-list { display: flex; flex-direction: column; gap: 12px; }
.announce-card {
  background: #fff; border-radius: 14px; padding: 22px 24px;
  border: 1px solid #eef2f6; box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  display: flex; justify-content: space-between; align-items: flex-start; gap: 16px;
  transition: all 0.25s;
}
.announce-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.announce-left { flex: 1; min-width: 0; }
.announce-left h3 { font-size: 16px; font-weight: 600; color: #1e293b; margin: 0 0 8px; display: flex; align-items: center; gap: 8px; }
.top-badge { font-size: 11px; background: #6366f1; color: #fff; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.announce-content { font-size: 14px; color: #64748b; line-height: 1.6; margin: 0 0 8px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.announce-time { font-size: 12px; color: #94a3b8; }
.announce-actions { display: flex; gap: 8px; flex-shrink: 0; }
.action-btn { padding: 5px 14px; border: 1px solid; border-radius: 8px; font-size: 12px; cursor: pointer; transition: all 0.2s; background: #fff; font-weight: 500; }
.action-btn.edit { color: #409eff; border-color: #d9ecff; }
.action-btn.edit:hover { background: #ecf5ff; border-color: #409eff; }
.action-btn.danger { color: #f56c6c; border-color: #fde2e2; }
.action-btn.danger:hover { background: #fef0f0; border-color: #f56c6c; }
.dialog-form { display: flex; flex-direction: column; gap: 16px; }
.form-row label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
</style>
