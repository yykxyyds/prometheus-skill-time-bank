<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/index'
import { ElMessage } from 'element-plus'

const announcements = ref([])
const loading = ref(false)
const showDialog = ref(false)
const form = ref({ title: '', content: '' })

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  const res = await api.get('/announcement/list')
  announcements.value = res.data?.records || []
  loading.value = false
}

async function create() {
  await api.post('/announcement', form.value)
  ElMessage.success('公告发布成功')
  showDialog.value = false
  form.value = { title: '', content: '' }
  await loadData()
}

async function remove(id) {
  await api.delete(`/announcement/${id}`)
  ElMessage.success('已删除')
  await loadData()
}
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>公告管理</h2>
      <el-button type="primary" @click="showDialog = true" style="background:#e8784a;border-color:#e8784a">发布公告</el-button>
    </div>
    <el-table :data="announcements" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column prop="isTop" label="置顶" width="80">
        <template #default="{ row }">{{ row.isTop ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showDialog" title="发布公告" width="500px">
      <el-input v-model="form.title" placeholder="标题" style="margin-bottom:12px" />
      <el-input v-model="form.content" type="textarea" :rows="4" placeholder="内容" />
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="create">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
