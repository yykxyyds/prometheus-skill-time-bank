<script setup>
import { ref, onMounted } from 'vue'
import { getSkillList, getCategories } from '../api/skill'

const skills = ref([])
const categories = ref([])
const loading = ref(false)
const query = ref({ page: 1, size: 12, categoryId: null, keyword: '', sort: '' })
const total = ref(0)

onMounted(async () => {
  loading.value = true
  const [skillRes, catRes] = await Promise.all([
    getSkillList(query.value),
    getCategories()
  ])
  skills.value = skillRes.data?.records || []
  total.value = skillRes.data?.total || 0
  categories.value = catRes.data || []
  loading.value = false
})

async function search() {
  query.value.page = 1
  loading.value = true
  const res = await getSkillList(query.value)
  skills.value = res.data?.records || []
  loading.value = false
}
</script>

<template>
  <div class="home">
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="搜索技能..." prefix-icon="Search" clearable
        @keyup.enter="search" style="max-width: 400px" />
      <el-select v-model="query.categoryId" placeholder="分类" clearable @change="search" style="width: 150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="search" style="background:#e8784a;border-color:#e8784a">搜索</el-button>
    </div>

    <div class="skill-grid" v-loading="loading">
      <el-empty v-if="!loading && skills.length === 0" description="暂无技能" />
      <el-card v-for="skill in skills" :key="skill.id" class="skill-card" shadow="hover"
        @click="$router.push(`/skill/${skill.id}`)">
        <div class="skill-cover">{{ skill.title?.charAt(0) }}</div>
        <div class="skill-info">
          <h3>{{ skill.title }}</h3>
          <p class="skill-desc">{{ skill.description?.substring(0, 50) }}{{ skill.description?.length > 50 ? '...' : '' }}</p>
          <div class="skill-meta">
            <span class="price">{{ skill.price }} 时间币/小时</span>
            <span class="views">{{ skill.viewCount }} 浏览</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-pagination v-if="total > query.size" v-model:current-page="query.page" :page-size="query.size"
      :total="total" layout="prev, pager, next" @current-change="search" style="justify-content:center;margin-top:24px" />
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.skill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.skill-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.skill-card:hover { transform: translateY(-2px); }
.skill-cover {
  width: 100%;
  height: 120px;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: bold;
  border-radius: 4px;
  margin-bottom: 12px;
}
.skill-info h3 { margin: 0 0 8px; font-size: 16px; }
.skill-desc { color: #999; font-size: 13px; margin-bottom: 8px; }
.skill-meta { display: flex; justify-content: space-between; font-size: 13px; }
.price { color: #e8784a; font-weight: bold; }
.views { color: #bbb; }
</style>
