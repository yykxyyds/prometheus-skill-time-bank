<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import api from '../../api/index'

const userStore = useUserStore()
const balanceInfo = ref({ balance: 0, frozenBalance: 0, totalEarned: 0, totalSpent: 0 })
const transactions = ref([])

onMounted(async () => {
  const [balRes, txRes] = await Promise.all([
    api.get('/wallet/balance'),
    api.get('/wallet/transactions', { params: { page: 1, size: 50 } })
  ])
  balanceInfo.value = balRes.data || {}
  transactions.value = txRes.data?.records || []
})
</script>

<template>
  <div class="wallet">
    <h2>时间银行</h2>
    <div class="balance-cards">
      <el-card shadow="hover"><div class="card-num">{{ balanceInfo.balance }}</div><div class="card-label">可用余额</div></el-card>
      <el-card shadow="hover"><div class="card-num">{{ balanceInfo.frozenBalance }}</div><div class="card-label">冻结中</div></el-card>
      <el-card shadow="hover"><div class="card-num">{{ balanceInfo.totalEarned }}</div><div class="card-label">累计收入</div></el-card>
      <el-card shadow="hover"><div class="card-num">{{ balanceInfo.totalSpent }}</div><div class="card-label">累计支出</div></el-card>
    </div>
    <h3 style="margin: 24px 0 12px">时间流水</h3>
    <el-table :data="transactions" stripe>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type==='INCOME'||row.type==='GIFT'?'success':'warning'">
            {{ {INCOME:'收入',EXPENSE:'支出',FREEZE:'冻结',UNFREEZE:'解冻',GIFT:'赠送'}[row.type] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="balanceAfter" label="余额" width="100" />
      <el-table-column prop="remark" label="备注" />
    </el-table>
  </div>
</template>

<style scoped>
.balance-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.card-num { font-size: 28px; font-weight: bold; color: #e8784a; text-align: center; }
.card-label { text-align: center; color: #999; font-size: 13px; margin-top: 4px; }
</style>
