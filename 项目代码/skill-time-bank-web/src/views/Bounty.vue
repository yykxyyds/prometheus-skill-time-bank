<script setup>
import { ref, onMounted } from 'vue'
import { getBountyList } from '../api/skill'

const bounties = ref([])
const loading = ref(false)
const page = ref(1)

onMounted(async () => {
  loading.value = true
  const res = await getBountyList({ page: 1, size: 20 })
  bounties.value = res.data?.records || []
  loading.value = false
})
</script>

<template>
  <div class="bounty-page">
    <h2>需求悬赏</h2>
    <div class="bounty-list" v-loading="loading">
      <el-empty v-if="!loading && bounties.length === 0" description="暂无悬赏" />
      <el-card v-for="b in bounties" :key="b.id" class="bounty-card" shadow="hover">
        <h3>{{ b.title }}</h3>
        <p>{{ b.description?.substring(0, 80) }}</p>
        <div class="bounty-meta">
          <span class="reward">{{ b.reward }} 时间币</span>
          <el-tag :type="b.status === 1 ? 'warning' : b.status === 2 ? 'primary' : 'info'">
            {{ {1:'已发布',2:'已接单',3:'已完成',4:'已过期'}[b.status] }}
          </el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.bounty-page h2 { margin-bottom: 16px; }
.bounty-list { display: flex; flex-direction: column; gap: 12px; }
.bounty-card h3 { margin: 0 0 8px; }
.bounty-card p { color: #666; font-size: 14px; margin-bottom: 8px; }
.bounty-meta { display: flex; justify-content: space-between; align-items: center; }
.reward { color: #e8784a; font-weight: bold; font-size: 16px; }
</style>
