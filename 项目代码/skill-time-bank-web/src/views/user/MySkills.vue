<script setup>
import { ref, onMounted } from 'vue'
import { getMySkills, offlineSkill } from '../../api/skill'
import { ElMessage, ElMessageBox } from 'element-plus'

const skills = ref([])
onMounted(async () => {
  const res = await getMySkills()
  skills.value = res.data || []
})

async function handleOffline(id) {
  await ElMessageBox.confirm('确定下架该技能？', '提示', { type: 'warning' })
  await offlineSkill(id)
  ElMessage.success('已下架')
  const res = await getMySkills()
  skills.value = res.data || []
}
</script>

<template>
  <div>
    <h2>我的技能</h2>
    <el-table :data="skills" stripe>
      <el-table-column prop="title" label="技能名称" />
      <el-table-column prop="price" label="价格(时间币/小时)" width="160" />
      <el-table-column prop="viewCount" label="浏览量" width="100" />
      <el-table-column prop="orderCount" label="订单数" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="danger" size="small" @click="handleOffline(row.id)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
