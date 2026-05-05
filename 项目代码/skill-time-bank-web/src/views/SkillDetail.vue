<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSkillDetail } from '../api/skill'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const skill = ref({})

onMounted(async () => {
  const res = await getSkillDetail(route.params.id)
  skill.value = res.data || {}
})

function handleOrder() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  // TODO: 下单流程
}
</script>

<template>
  <div class="detail" v-if="skill.id">
    <div class="detail-main">
      <div class="detail-cover">{{ skill.title?.charAt(0) }}</div>
      <h1>{{ skill.title }}</h1>
      <el-tag v-if="skill.status === 1" type="success">上架中</el-tag>
      <el-tag v-else type="info">已下架</el-tag>
      <p class="desc">{{ skill.description }}</p>
      <p><strong>可用时间：</strong>{{ skill.availableTime || '灵活安排' }}</p>
      <p><strong>浏览量：</strong>{{ skill.viewCount }} | <strong>完成订单：</strong>{{ skill.orderCount }}</p>
    </div>
    <div class="detail-side">
      <el-card shadow="hover">
        <div class="price-block">
          <span class="price">{{ skill.price }}</span>
          <span>时间币 / 小时</span>
        </div>
        <el-button type="primary" block size="large" @click="handleOrder"
          style="background:#e8784a;border-color:#e8784a;margin-top:16px">
          立即预约
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.detail { display: grid; grid-template-columns: 1fr 300px; gap: 24px; }
.detail-cover {
  width: 100%; height: 200px; background: linear-gradient(135deg, #f0a060, #e8784a);
  display: flex; align-items: center; justify-content: center; color: #fff;
  font-size: 64px; font-weight: bold; border-radius: 8px; margin-bottom: 16px;
}
.detail-main h1 { margin: 0 0 12px; }
.desc { color: #666; line-height: 1.8; margin: 16px 0; }
.price-block { text-align: center; }
.price { font-size: 32px; color: #e8784a; font-weight: bold; }
</style>
