<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import api from '../../api/index'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const order = ref({})
const messages = ref([])
const loading = ref(false)
const newMsg = ref('')
const sending = ref(false)
const chatBox = ref(null)

// 评价表单
const showReview = ref(false)
const reviewForm = ref({
  score: 5,
  comment: '',
  punctualityScore: 5,
  communicationScore: 5,
  professionalScore: 5,
  attitudeScore: 5
})

const isBuyer = ref(false)
const isSeller = ref(false)

const statusMap = {
  1: { label: '待确认', color: '#e6a23c' },
  2: { label: '进行中', color: '#409eff' },
  3: { label: '待确认完成', color: '#e6a23c' },
  4: { label: '已完成', color: '#67c23a' },
  5: { label: '已取消', color: '#909399' }
}

const activeStep = ref(0)

function calcStep(status) {
  // Map status to timeline step
  const stepMap = { 1: 0, 2: 1, 3: 2, 4: 3 }
  return stepMap[status] ?? 0
}

onMounted(async () => {
  loading.value = true
  try {
    const [orderRes, chatRes] = await Promise.all([
      api.get(`/order/${route.params.id}`),
      api.get(`/chat/order/${route.params.id}`)
    ])
    order.value = orderRes.data || {}
    messages.value = chatRes.data || []
    isBuyer.value = userStore.userId == order.value.buyerId
    isSeller.value = userStore.userId == order.value.sellerId
    activeStep.value = calcStep(order.value.status)
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
  await nextTick()
  scrollToBottom()
})

function scrollToBottom() {
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight
  }
}

async function sendMessage() {
  if (!newMsg.value.trim()) return
  sending.value = true
  try {
    const res = await api.post(`/chat/order/${order.value.id}`, { content: newMsg.value })
    messages.value.push(res.data)
    newMsg.value = ''
    await nextTick()
    scrollToBottom()
  } catch (e) { /* handled */ } finally {
    sending.value = false
  }
}

async function handleAction(action) {
  try {
    switch (action) {
      case 'confirm':
        await api.put(`/order/${order.value.id}/confirm`)
        ElMessage.success('已确认接单')
        break
      case 'buyer-complete':
        await api.put(`/order/${order.value.id}/buyer-complete`)
        ElMessage.success('已确认完成')
        break
      case 'seller-complete':
        await api.put(`/order/${order.value.id}/seller-complete`)
        ElMessage.success('已确认完成')
        break
      case 'cancel':
        await api.put(`/order/${order.value.id}/cancel`)
        ElMessage.success('订单已取消')
        break
    }
    const res = await api.get(`/order/${order.value.id}`)
    order.value = res.data || {}
    activeStep.value = calcStep(order.value.status)
  } catch (e) { /* handled */ }
}

async function submitReview() {
  const form = reviewForm.value
  try {
    await api.post('/review', {
      orderId: order.value.id,
      targetId: isBuyer.value ? order.value.sellerId : order.value.buyerId,
      score: form.score,
      comment: form.comment,
      punctualityScore: form.punctualityScore,
      communicationScore: form.communicationScore,
      professionalScore: form.professionalScore,
      attitudeScore: form.attitudeScore
    })
    ElMessage.success('评价提交成功')
    showReview.value = false
  } catch (e) { /* handled */ }
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}
</script>

<template>
  <div class="order-detail" v-loading="loading">
    <div class="breadcrumb">
      <router-link to="/orders/buyer">我的订单</router-link>
      <span class="sep">/</span>
      <span class="current">订单详情</span>
    </div>

    <template v-if="order.id">
      <div class="detail-layout">
        <!-- 左侧：订单信息 + 聊天 -->
        <div class="main-col">
          <!-- 订单状态卡片 -->
          <div class="card status-card">
            <div class="card-header">
              <h2>订单 #{{ order.orderNo }}</h2>
              <span class="status-badge" :style="{ color: statusMap[order.status]?.color, background: statusMap[order.status]?.color + '18' }">
                {{ statusMap[order.status]?.label || '未知' }}
              </span>
            </div>

            <!-- 时间线 -->
            <div class="timeline">
              <div :class="['step', { done: activeStep >= 0 }]">
                <div class="dot"></div>
                <span>待确认</span>
              </div>
              <div class="line" :class="{ done: activeStep >= 1 }"></div>
              <div :class="['step', { done: activeStep >= 1 }]">
                <div class="dot"></div>
                <span>进行中</span>
              </div>
              <div class="line" :class="{ done: activeStep >= 2 }"></div>
              <div :class="['step', { done: activeStep >= 2 }]">
                <div class="dot"></div>
                <span>待确认完成</span>
              </div>
              <div class="line" :class="{ done: activeStep >= 3 }"></div>
              <div :class="['step', { done: activeStep >= 3 }]">
                <div class="dot"></div>
                <span>已完成</span>
              </div>
            </div>

            <!-- 订单详情网格 -->
            <div class="info-grid">
              <div class="info-item">
                <label>金额</label>
                <span class="amount">{{ order.amount }} 时间币</span>
              </div>
              <div class="info-item">
                <label>冻结金额</label>
                <span>{{ order.frozenAmount || 0 }} 币</span>
              </div>
              <div class="info-item">
                <label>买家ID</label>
                <span>#{{ order.buyerId }}</span>
              </div>
              <div class="info-item">
                <label>卖家ID</label>
                <span>#{{ order.sellerId }}</span>
              </div>
              <div class="info-item">
                <label>技能ID</label>
                <span
                  class="link"
                  @click="router.push(`/skill/${order.skillId}`)"
                >#{{ order.skillId }}</span>
              </div>
              <div class="info-item">
                <label>创建时间</label>
                <span>{{ formatTime(order.createTime) }}</span>
              </div>
              <div class="info-item" v-if="order.completedTime">
                <label>完成时间</label>
                <span>{{ formatTime(order.completedTime) }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="actions" v-if="order.status !== 4 && order.status !== 5">
              <template v-if="order.status === 1">
                <button v-if="isSeller" class="btn-primary" @click="handleAction('confirm')">
                  <Icon icon="mdi:check" /> 确认接单
                </button>
                <button v-if="!isSeller" class="btn-cancel" @click="handleAction('cancel')">
                  取消订单
                </button>
                <button v-if="isSeller" class="btn-cancel" @click="handleAction('cancel')">
                  拒绝
                </button>
              </template>

              <template v-if="order.status === 2 || order.status === 3">
                <button
                  v-if="isBuyer && !order.buyerConfirm"
                  class="btn-primary"
                  @click="handleAction('buyer-complete')"
                >
                  确认完成
                </button>
                <button
                  v-if="isSeller && !order.sellerConfirm"
                  class="btn-primary"
                  @click="handleAction('seller-complete')"
                >
                  确认完成
                </button>
                <span v-if="isBuyer && order.buyerConfirm" class="hint">已确认，等待对方</span>
                <span v-if="isSeller && order.sellerConfirm" class="hint">已确认，等待对方</span>
              </template>
            </div>

            <!-- 已完成：评价按钮 -->
            <div class="actions" v-if="order.status === 4">
              <button class="btn-primary" @click="showReview = !showReview">
                <Icon icon="mdi:star" /> {{ showReview ? '收起评价' : '写评价' }}
              </button>
              <router-link to="/orders/buyer" class="btn-link">返回订单列表</router-link>
            </div>
          </div>

          <!-- 评价表单 -->
          <div class="card review-card" v-if="showReview">
            <h3>交易评价</h3>
            <div class="review-form">
              <div class="form-row">
                <label>综合评分</label>
                <el-rate v-model="reviewForm.score" :max="5" />
              </div>
              <div class="form-row">
                <label>文字评价</label>
                <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="写下你的评价..." />
              </div>
              <div class="form-row">
                <label>四维评分</label>
                <div class="dim-scores">
                  <div class="dim-item">
                    <span>按时交付</span>
                    <el-rate v-model="reviewForm.punctualityScore" :max="5" size="small" />
                  </div>
                  <div class="dim-item">
                    <span>沟通能力</span>
                    <el-rate v-model="reviewForm.communicationScore" :max="5" size="small" />
                  </div>
                  <div class="dim-item">
                    <span>专业水平</span>
                    <el-rate v-model="reviewForm.professionalScore" :max="5" size="small" />
                  </div>
                  <div class="dim-item">
                    <span>服务态度</span>
                    <el-rate v-model="reviewForm.attitudeScore" :max="5" size="small" />
                  </div>
                </div>
              </div>
              <button class="btn-primary" @click="submitReview">提交评价</button>
            </div>
          </div>

          <!-- 聊天区 -->
          <div class="card chat-card">
            <h3>订单聊天</h3>
            <div class="chat-messages" ref="chatBox">
              <el-empty v-if="messages.length === 0" description="暂无消息，开始交流吧" :image-size="60" />
              <div
                v-for="m in messages"
                :key="m.id"
                :class="['msg', m.senderId == userStore.userId ? 'msg-me' : 'msg-other']"
              >
                <div class="msg-bubble">
                  <div class="msg-content">{{ m.content }}</div>
                  <div class="msg-time">{{ formatTime(m.createTime) }}</div>
                </div>
              </div>
            </div>
            <div class="chat-input">
              <el-input
                v-model="newMsg"
                placeholder="输入消息..."
                @keyup.enter="sendMessage"
                :disabled="order.status === 5"
              />
              <button class="btn-send" :disabled="sending" @click="sendMessage">
                <Icon icon="mdi:send" />
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧摘要 -->
        <div class="side-col">
          <div class="card">
            <h3>订单摘要</h3>
            <dl class="summary-list">
              <div class="sum-item">
                <dt>订单编号</dt>
                <dd class="mono">{{ order.orderNo }}</dd>
              </div>
              <div class="sum-item">
                <dt>状态</dt>
                <dd :style="{ color: statusMap[order.status]?.color, fontWeight: 600 }">
                  {{ statusMap[order.status]?.label }}
                </dd>
              </div>
              <div class="sum-item">
                <dt>时间币</dt>
                <dd class="amount-text">{{ order.amount }}</dd>
              </div>
              <div class="sum-item">
                <dt>我的角色</dt>
                <dd>{{ isBuyer ? '买方' : '卖方' }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="订单不存在" :image-size="120" />
  </div>
</template>

<style scoped>
.breadcrumb {
  margin-bottom: 20px;
  font-size: 13px;
  color: #bbb;
}
.breadcrumb a { color: #e8784a; }
.breadcrumb .sep { margin: 0 8px; }
.breadcrumb .current { color: #555; }

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  align-items: start;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #f0e8e0;
  margin-bottom: 16px;
}
.card h2 { margin: 0; font-size: 18px; color: #2c3e50; font-weight: 700; }
.card h3 { margin: 0 0 16px; font-size: 16px; color: #333; font-weight: 600; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.status-badge {
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* Timeline */
.timeline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 28px;
  padding: 0 20px;
}
.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #ccc;
}
.step.done { color: #e8784a; }
.step .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e8e0d8;
}
.step.done .dot { background: #e8784a; }
.line {
  flex: 1;
  height: 2px;
  background: #e8e0d8;
  margin: 0 4px;
  margin-bottom: 18px;
}
.line.done { background: #e8784a; }

/* Info grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item label {
  font-size: 12px;
  color: #999;
}
.info-item span {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.amount { color: #e8784a !important; font-weight: 700 !important; font-size: 18px !important; }
.link { color: #e8784a; cursor: pointer; }
.link:hover { text-decoration: underline; }

/* Actions */
.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #f5f0eb;
}
.btn-primary {
  padding: 8px 20px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.btn-primary:hover {
  box-shadow: 0 4px 14px rgba(232, 120, 74, 0.3);
}
.btn-cancel {
  padding: 8px 20px;
  background: #f5f0eb;
  color: #888;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.btn-cancel:hover { background: #ebe5de; }
.btn-link {
  font-size: 14px;
  color: #888;
  text-decoration: none;
}
.hint {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}

/* Chat */
.chat-card {
  display: flex;
  flex-direction: column;
}
.chat-messages {
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
  padding-right: 4px;
}
.msg {
  display: flex;
  max-width: 75%;
}
.msg-me {
  align-self: flex-end;
}
.msg-other {
  align-self: flex-start;
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}
.msg-me .msg-bubble {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-other .msg-bubble {
  background: #f5f0eb;
  color: #333;
  border-bottom-left-radius: 4px;
}
.msg-time {
  font-size: 10px;
  opacity: 0.7;
  margin-top: 4px;
}
.msg-me .msg-time { color: rgba(255,255,255,0.75); }
.msg-other .msg-time { color: #bbb; }

.chat-input {
  display: flex;
  gap: 8px;
  align-items: center;
}
.btn-send {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  transition: all 0.3s;
}
.btn-send:hover:not(:disabled) {
  box-shadow: 0 4px 14px rgba(232, 120, 74, 0.3);
}
.btn-send:disabled { opacity: 0.6; cursor: not-allowed; }

/* Review */
.review-form {
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
.dim-scores {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.dim-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}

/* Side */
.summary-list {
  display: flex;
  flex-direction: column;
}
.sum-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f0eb;
}
.sum-item:last-child { border-bottom: none; }
.sum-item dt { font-size: 13px; color: #999; }
.sum-item dd { font-size: 14px; color: #333; font-weight: 500; }
.mono { font-family: monospace; font-size: 13px; }
.amount-text { color: #e8784a !important; font-weight: 700 !important; }
</style>
