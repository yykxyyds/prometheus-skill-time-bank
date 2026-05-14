<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMySkills, offlineSkill, publishSkill, updateSkill, getCategories, uploadImage } from '../../api/skill'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'

const router = useRouter()
const skills = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const categories = ref([])
const form = ref({ id: null, title: '', description: '', price: 0, categoryId: null, coverImage: '' })
const uploading = ref(false)

const onlineCount = computed(() => skills.value.filter(s => s.status === 1).length)

onMounted(async () => {
  loading.value = true
  try {
    const [skillRes, catRes] = await Promise.all([
      getMySkills(),
      getCategories()
    ])
    skills.value = skillRes.data?.records || []
    categories.value = catRes.data || []
  } finally {
    loading.value = false
  }
})

function openCreate() {
  isEdit.value = false
  form.value = { id: null, title: '', description: '', price: 0, categoryId: null, coverImage: '' }
  showDialog.value = true
}

function openEdit(skill) {
  isEdit.value = true
  form.value = { ...skill }
  showDialog.value = true
}

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { ElMessage.warning('仅支持图片格式'); return }
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片不能超过5MB'); return }
  uploading.value = true
  try {
    const res = await uploadImage(file)
    form.value.coverImage = res.data
    ElMessage.success('封面已上传')
  } catch { /* handled */ }
  finally { uploading.value = false }
}

function removeCover() {
  form.value.coverImage = ''
}

async function handleSave() {
  if (!form.value.title || !form.value.price) {
    ElMessage.warning('请填写技能名称和价格')
    return
  }
  if (isEdit.value) {
    await updateSkill(form.value)
    ElMessage.success('已更新')
  } else {
    await publishSkill(form.value)
    ElMessage.success('发布成功')
  }
  showDialog.value = false
  const res = await getMySkills()
  skills.value = res.data || []
}

async function handleOffline(id) {
  await ElMessageBox.confirm('确定下架该技能？', '提示', { type: 'warning' })
  await offlineSkill(id)
  ElMessage.success('已下架')
  const res = await getMySkills()
  skills.value = res.data || []
}
</script>

<template>
  <div class="my-skills">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h2>我的技能</h2>
        <p>管理你发布的技能服务</p>
      </div>
      <button class="publish-btn" @click="openCreate">发布新技能</button>
    </div>

    <!-- 统计小卡片 -->
    <div class="stats-row">
      <div class="mini-stat">
        <span class="mini-num">{{ skills.length }}</span>
        <span class="mini-label">全部技能</span>
      </div>
      <div class="mini-stat">
        <span class="mini-num online">{{ onlineCount }}</span>
        <span class="mini-label">上架中</span>
      </div>
    </div>

    <!-- 技能表格 -->
    <div class="table-panel">
      <el-table :data="skills" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="title" label="技能名称" min-width="160">
          <template #default="{ row }">
            <span class="skill-name" @click="router.push(`/skill/${row.id}`)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(币/时)" width="120" align="center" />
        <el-table-column prop="categoryName" label="分类" width="120" align="center">
          <template #default="{ row }">
            <span class="cat-tag">{{ row.categoryName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column prop="orderCount" label="订单" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="row-status" :class="row.status === 1 ? 'on' : row.status === 2 ? 'pending' : 'off'">
              {{ row.status === 1 ? '上架' : row.status === 2 ? '待审核' : '下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <button class="action-btn edit" @click="openEdit(row)">编辑</button>
            <button v-if="row.status === 1 || row.status === 2" class="action-btn danger" @click="handleOffline(row.id)">下架</button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && skills.length === 0" description="还没有发布技能，快去发布吧" :image-size="100" />
    </div>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑技能' : '发布新技能'" width="520px" destroy-on-close>
      <div class="dialog-form">
        <div class="form-row">
          <label>技能名称</label>
          <el-input v-model="form.title" placeholder="例如：Python编程辅导" />
        </div>
        <div class="form-row">
          <label>分类</label>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <div class="form-row">
          <label>价格（时间币/小时）</label>
          <el-input-number v-model="form.price" :min="1" :max="9999" style="width:100%" />
        </div>
        <div class="form-row">
          <label>技能描述</label>
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述你的技能内容、服务方式..." />
        </div>
        <div class="form-row">
          <label>封面图片 <span class="label-hint">— 传一张和技能相关的图片更好看</span></label>
          <div class="cover-upload">
            <template v-if="form.coverImage">
              <div class="cover-preview">
                <img :src="form.coverImage" alt="封面预览" />
                <button class="cover-remove" @click="removeCover">×</button>
              </div>
            </template>
            <template v-else>
              <label class="cover-upload-box" :class="{ loading: uploading }">
                <input type="file" accept="image/*" hidden @change="handleCoverUpload" :disabled="uploading" />
                <Icon v-if="!uploading" icon="mdi:image-plus" class="upload-placeholder-icon" />
                <span v-if="uploading">上传中...</span>
                <span v-else>+ 上传封面</span>
                <span v-if="!uploading" class="upload-hint">支持 JPG/PNG，不超过 5MB</span>
              </label>
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">{{ isEdit ? '保存修改' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 4px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.publish-btn {
  padding: 10px 22px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 0.3px;
  transition: all 0.3s;
}
.publish-btn:hover {
  box-shadow: 0 4px 14px rgba(232,120,74,0.3);
  transform: translateY(-1px);
}

/* 统计 */
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.mini-stat {
  background: #fff;
  border-radius: 10px;
  padding: 14px 22px;
  border: 1px solid #f0e8e0;
  display: flex;
  flex-direction: column;
  min-width: 120px;
}
.mini-num {
  font-size: 26px;
  font-weight: 800;
  color: #2c3e50;
}
.mini-num.online { color: #4caf50; }
.mini-label { font-size: 12px; color: #999; }

/* 表格面板 */
.table-panel {
  background: #fff;
  border-radius: 14px;
  padding: 0;
  overflow: hidden;
  border: 1px solid #f0e8e0;
}

.skill-name {
  color: #e8784a;
  font-weight: 500;
  cursor: pointer;
}
.skill-name:hover { text-decoration: underline; }
.cat-tag {
  font-size: 12px;
  background: #fdf9f6;
  color: #e8784a;
  padding: 2px 8px;
  border-radius: 4px;
}
.row-status {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 4px;
}
.row-status.on { background: #e8f5e9; color: #4caf50; }
.row-status.pending { background: #fff7e6; color: #e6a23c; }
.row-status.off { background: #f5f5f5; color: #999; }

.action-btn {
  padding: 4px 12px;
  border: 1px solid #e8e0d8;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  margin: 0 2px;
}
.action-btn.edit { color: #409eff; border-color: #d9ecff; }
.action-btn.edit:hover { background: #ecf5ff; }
.action-btn.danger { color: #f56c6c; border-color: #fde2e2; }
.action-btn.danger:hover { background: #fef0f0; }

/* 弹窗表单 */
.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-row label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}
.label-hint {
  font-weight: 400;
  color: #bbb;
  font-size: 12px;
}

/* 封面上传 */
.cover-upload { }
.cover-upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 110px;
  border: 2px dashed #e8e0d8;
  border-radius: 10px;
  cursor: pointer;
  color: #bbb;
  font-size: 14px;
  transition: all 0.3s;
}
.cover-upload-box:hover {
  border-color: #e8784a;
  color: #e8784a;
  background: #fdf9f6;
}
.upload-placeholder-icon {
  font-size: 28px;
  margin-bottom: 2px;
}
.upload-hint {
  font-size: 11px;
  color: #ccc;
}
.cover-upload-box.loading {
  pointer-events: none;
  opacity: 0.6;
}
.cover-preview {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
}
.cover-preview img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
  border-radius: 10px;
}
.cover-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 16px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}
.cover-remove:hover {
  background: rgba(0,0,0,0.7);
}
</style>
