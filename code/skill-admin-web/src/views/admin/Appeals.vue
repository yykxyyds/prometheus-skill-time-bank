<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const appeals = ref([])
const roleFilter = ref(null)
const filteredAppeals = computed(() => {
  if (!roleFilter.value) return appeals.value
  return appeals.value.filter(a => a.appellantRole === roleFilter.value)
})
const loading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

onMounted(async () => {
  await loadAppeals()
})

async function loadAppeals() {
  loading.value = true
  try {
    const res = await api.get('/admin/appeal/list', { params: { page: 1, size: 50 } })
    appeals.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function parseImages(row) {
  if (!row.evidenceImages) return []
  try {
    const urls = JSON.parse(row.evidenceImages)
    return Array.isArray(urls) ? urls : []
  } catch { return [] }
}

function openPreview(url) {
  previewUrl.value = url
  previewVisible.value = true
}

const actionTextMap = {
  ACCEPT_REFUND: '支持买家（退款取消订单）',
  ACCEPT_COMPLETE: '支持卖家（强制完成并付款）',
  REJECT: '驳回申诉'
}
async function handleAppeal(appeal, decision) {
  const actionText = actionTextMap[decision]
  const { value: result } = await ElMessageBox.prompt(
      `确定${actionText}吗？请输入处理说明：`,
      '处理申诉',
    { inputType: 'textarea', inputPlaceholder: '描述处理结果...' }
  )
  if (result) {
    await api.put(`/admin/appeal/${appeal.id}/handle`, { result, decision })
    const msgMap = { ACCEPT_REFUND: '已退款并取消订单', ACCEPT_COMPLETE: '已强制完成订单', REJECT: '已驳回申诉' }
    ElMessage.success(msgMap[decision])
    await loadAppeals()
  }
}

const statusMap = {
  1: { label: '待处理', class: 'st-pend' },
  2: { label: '处理中', class: 'st-proc' },
  3: { label: '已处理', class: 'st-done' }
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>申诉管理</h2>
      <p>处理用户纠纷与申诉</p>
    </div>
    <div class="table-panel">
      <el-table :data="filteredAppeals" v-loading="loading" stripe>
        <el-table-column prop="id" label="申诉ID" width="200" />
        <el-table-column prop="orderId" label="关联订单" width="200">
          <template #default="{ row }">
            <span class="link-text">{{ row.orderId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申诉原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="证据" width="140" align="center">
          <template #default="{ row }">
            <div v-if="parseImages(row).length > 0" class="evidences">
              <img
                v-for="(url, i) in parseImages(row).slice(0, 3)"
                :key="i"
                :src="url"
                class="evi-thumb"
                @click="openPreview(url)"
              />
              <span v-if="parseImages(row).length > 3" class="evi-more">+{{ parseImages(row).length - 3 }}</span>
            </div>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="appellantName" label="申诉人" width="100" show-overflow-tooltip />
        <el-table-column label="身份" width="80" align="center">
          <template #default="{ row }">
            <span :class="['role-tag', row.appellantRole === 'BUYER' ? 'role-buyer' : row.appellantRole === 'SELLER' ? 'role-seller' : '']">
              {{ row.appellantRole === 'BUYER' ? '买方' : row.appellantRole === 'SELLER' ? '卖方' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['appeal-status', (statusMap[row.status] || {}).class || 'st-pend']">
              {{ (statusMap[row.status] || {}).label || row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <template v-if="row.status !== 3">
              <template v-if="row.appellantRole === 'BUYER'">
                <button class="action-btn success" @click="handleAppeal(row, 'ACCEPT_REFUND')">退款取消</button>
                <button class="action-btn danger" @click="handleAppeal(row, 'REJECT')">驳回</button>
              </template>
              <template v-else-if="row.appellantRole === 'SELLER'">
                <button class="action-btn warning" @click="handleAppeal(row, 'ACCEPT_COMPLETE')">强制完成</button>
                <button class="action-btn danger" @click="handleAppeal(row, 'REJECT')">驳回</button>
              </template>
              <template v-else>
                <button class="action-btn danger" @click="handleAppeal(row, 'REJECT')">驳回</button>
              </template>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && appeals.length === 0" description="暂无申诉" :image-size="80" />
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="证据图片" width="560px" top="8vh" destroy-on-close>
      <img v-if="previewUrl" :src="previewUrl" style="width:100%;border-radius:8px" />
    </el-dialog>
  </div>
</template>

<style scoped>
.link-text { color: #409eff; font-family: monospace; }
.appeal-status { font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 4px; }
.st-pend { background: #fdf6ec; color: #e6a23c; }
.st-proc { background: #ecf5ff; color: #409eff; }
.st-done { background: #e8f5e9; color: #4caf50; }
.action-btn { padding: 5px 14px; border: 1px solid; border-radius: 8px; font-size: 12px; cursor: pointer; transition: all 0.2s; background: #fff; font-weight: 500; }
.action-btn.primary { color: #409eff; border-color: #d9ecff; }
.action-btn.primary:hover { background: #ecf5ff; }
.action-btn.success { color: #4caf50; border-color: #c8e6c9; }
.action-btn.success:hover { background: #e8f5e9; }
.action-btn.danger { color: #f56c6c; border-color: #fde2e2; }
.action-btn.danger:hover { background: #fef0f0; }
.action-btn.warning { color: #e6a23c; border-color: #f5dab0; }
.action-btn.warning:hover { background: #fdf6ec; }

/* 证据缩略图 */
.evidences { display: flex; align-items: center; gap: 4px; justify-content: center; }
.evi-thumb {
  width: 36px; height: 36px; border-radius: 6px; object-fit: cover;
  cursor: pointer; border: 1px solid #f0e8e0; transition: transform 0.2s;
}
.evi-thumb:hover { transform: scale(1.15); border-color: #e8784a; }
.evi-more { font-size: 11px; color: #999; font-weight: 600; }

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.filter-btn {
  padding: 6px 18px;
  border: 1px solid #e8e0d8;
  border-radius: 20px;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-btn:hover { border-color: #e8784a; color: #e8784a; }
.filter-btn.active { background: #e8784a; color: #fff; border-color: #e8784a; }
.role-tag { font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 4px; }
.role-buyer { background: #e8f5e9; color: #4caf50; }
.role-seller { background: #ecf5ff; color: #409eff; }
</style>
