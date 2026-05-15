<script setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getConversations, getPrivateMessages, sendMessage, markAsRead } from '../api/message'
import { getNotifications, markNotificationRead } from '../api/notification'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const conversations = ref([])
const notifications = ref([])
const messages = ref([])
const activeConv = ref(null)
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)
const listRef = ref(null)
const showNotifications = ref(true)

async function loadNotifications() {
  try {
    const res = await getNotifications()
    notifications.value = res.data || []
  } catch { /* silent */ }
}

async function handleNotifClick(notif) {
  if (notif.isRead === 0) {
    try { await markNotificationRead(notif.id) } catch {}
    notif.isRead = 1
    userStore.refreshUnread()
  }
  if (notif.type === 'BOUNTY' || notif.type === 'bounty') {
    router.push(`/bounty/${notif.targetId}`)
  }
}

function formatNotifTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(async () => {
  await Promise.all([loadConversations(), loadNotifications()])
  const qUserId = route.query.userId
  if (qUserId) {
    const conv = conversations.value.find(c => String(c.otherUserId) === qUserId)
    if (conv) {
      openConversation(conv)
    } else {
      const friendNames = localStorage.getItem('friendNames')
      const names = friendNames ? JSON.parse(friendNames) : {}
      activeConv.value = { otherUserId: qUserId, otherUsername: names[qUserId] || '用户' }
      messages.value = []
      await loadMessages(activeConv.value.otherUserId)
    }
  }
})

watch(() => route.query.userId, async (val) => {
  if (val && String(activeConv.value?.otherUserId) !== val) {
    const conv = conversations.value.find(c => String(c.otherUserId) === val)
    if (conv) openConversation(conv)
    else {
      activeConv.value = { otherUserId: val, otherUsername: '用户' }
      messages.value = []
      await loadMessages(activeConv.value.otherUserId)
    }
  }
})

async function loadConversations() {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch { /* handled */ }
}

async function loadMessages(otherUserId) {
  try {
    const res = await getPrivateMessages(otherUserId)
    messages.value = res.data || []
    await nextTick()
    scrollToBottom()
    markAsRead(otherUserId).then(() => {
      if (activeConv.value) activeConv.value.unreadCount = 0
      userStore.refreshUnread()
    }).catch(() => {})
  } catch { /* handled */ }
}

async function openConversation(conv) {
  activeConv.value = conv
  messages.value = []
  loading.value = true
  try {
    const res = await getPrivateMessages(conv.otherUserId)
    messages.value = res.data || []
    await nextTick()
    scrollToBottom()
    conv.unreadCount = 0
    markAsRead(conv.otherUserId).then(() => userStore.refreshUnread()).catch(() => {})
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  const content = inputText.value.trim()
  if (!content || !activeConv.value || sending.value) return
  sending.value = true
  try {
    const msg = await sendMessage({ receiverId: activeConv.value.otherUserId, content })
    messages.value.push(msg.data)
    inputText.value = ''
    await nextTick()
    scrollToBottom()
    loadConversations()
  } catch { /* handled */ }
  finally { sending.value = false }
}

function scrollToBottom() {
  const el = listRef.value
  if (el) el.scrollTop = el.scrollHeight
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  if (d.getFullYear() === now.getFullYear()) return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()}`
}

function goProfile(uid) {
  router.push(`/profile/${uid}`)
}
</script>

<template>
  <div class="messages-page">
    <div class="msg-container" v-if="conversations.length > 0 || activeConv">
      <!-- 左侧对话列表 -->
      <div class="conv-sidebar" :class="{ hidden: activeConv && $route.query.userId }">
        <div class="conv-header">
          <h3>消息</h3>
        </div>
        <!-- 通知区域 -->
        <div v-if="notifications.length > 0 && showNotifications" class="notif-section">
          <div class="notif-section-header">
            <span class="notif-section-title"><Icon icon="mdi:bell" /> 通知</span>
            <button class="notif-collapse-btn" @click="showNotifications = false">
              <Icon icon="mdi:chevron-up" />
            </button>
          </div>
          <div class="notif-list">
            <div
              v-for="notif in notifications"
              :key="notif.id"
              class="notif-item"
              :class="{ unread: notif.isRead === 0 }"
              @click="handleNotifClick(notif)"
            >
              <div class="notif-dot" v-if="notif.isRead === 0"></div>
              <div class="notif-content">
                <div class="notif-title">{{ notif.title }}</div>
                <div class="notif-desc">{{ notif.content }}</div>
                <div class="notif-time">{{ formatNotifTime(notif.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>
        <!-- 已折叠通知 -->
        <div v-if="notifications.length > 0 && !showNotifications" class="notif-collapsed" @click="showNotifications = true">
          <Icon icon="mdi:bell" />
          <span>{{ notifications.filter(n => n.isRead === 0).length > 0 ? notifications.filter(n => n.isRead === 0).length + ' 条新通知' : '通知' }}</span>
          <Icon icon="mdi:chevron-down" />
        </div>

        <div class="conv-list" v-if="conversations.length > 0">
          <div
            v-for="conv in conversations"
            :key="conv.otherUserId"
            class="conv-item"
            :class="{ active: activeConv?.otherUserId === conv.otherUserId }"
            @click="openConversation(conv)"
          >
            <div class="conv-avatar" @click.stop="goProfile(conv.otherUserId)">
              <img v-if="conv.otherAvatar" :src="conv.otherAvatar" />
              <span v-else class="conv-avatar-text">{{ (conv.otherUsername || '?').charAt(0).toUpperCase() }}</span>
            </div>
            <div class="conv-info">
              <div class="conv-top">
                <span class="conv-name">{{ conv.otherUsername || '用户' }}</span>
                <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-bottom">
                <span class="conv-preview">{{ conv.lastContent || '' }}</span>
                <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="notifications.length === 0" class="conv-empty">
          <Icon icon="mdi:message-text-outline" class="empty-icon" />
          <p>暂无消息</p>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area" :class="{ empty: !activeConv }">
        <template v-if="activeConv">
          <div class="chat-top">
            <button class="back-btn" @click="router.push('/messages')"><Icon icon="mdi:arrow-left" /></button>
            <span class="chat-partner" @click="goProfile(activeConv.otherUserId)">{{ activeConv.otherUsername || '用户' }}</span>
          </div>
          <div ref="listRef" class="chat-body" v-loading="loading">
            <div v-for="msg in messages" :key="msg.id" class="msg-bubble-row" :class="{ mine: msg.senderId === userStore.userId }">
              <div class="msg-bubble">
                <div class="msg-content">{{ msg.content }}</div>
                <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
          </div>
          <div class="chat-input-area">
            <input
              v-model="inputText"
              class="chat-input"
              placeholder="输入消息..."
              maxlength="1000"
              @keyup.enter="handleSend"
            />
            <button class="send-btn" :disabled="!inputText.trim() || sending" @click="handleSend">
              {{ sending ? '发送中...' : '发送' }}
            </button>
          </div>
        </template>
        <div v-else class="chat-placeholder">
          <Icon icon="mdi:message-text-outline" class="placeholder-icon" />
          <p>选择一个对话</p>
        </div>
      </div>
    </div>

    <!-- 无任何对话 -->
    <div v-else class="msg-empty-full">
      <Icon icon="mdi:message-text-outline" class="empty-icon-lg" />
      <h3>暂无消息</h3>
      <p>去其他用户主页发私信吧</p>
      <router-link to="/" class="go-home-btn">去技能广场</router-link>
    </div>
  </div>
</template>

<style scoped>
.messages-page {
  height: calc(100vh - 140px);
  min-height: 480px;
}
.msg-container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #f0e8e0;
}

/* 左侧 */
.conv-sidebar {
  width: 340px;
  flex-shrink: 0;
  border-right: 1px solid #f0e8e0;
  display: flex;
  flex-direction: column;
  background: #fcfaf8;
}
.conv-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #f0e8e0;
}
.conv-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
}
.conv-list {
  flex: 1;
  overflow-y: auto;
}
.conv-item {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.15s;
  border-bottom: 1px solid #f8f4f0;
}
.conv-item:hover { background: rgba(232,120,74,0.04); }
.conv-item.active { background: rgba(232,120,74,0.08); }
.conv-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0a060, #e8784a);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.conv-avatar img { width: 100%; height: 100%; object-fit: cover; }
.conv-avatar-text { font-size: 18px; font-weight: 700; color: #fff; }
.conv-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
.conv-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
}
.conv-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-preview {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.conv-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 10px;
  flex-shrink: 0;
  margin-left: 6px;
}
.conv-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  gap: 8px;
}
.conv-empty p { font-size: 14px; margin: 0; }
.empty-icon { font-size: 48px; }

/* 右侧聊天 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-area.empty {
  align-items: center;
  justify-content: center;
}
.chat-top {
  padding: 14px 20px;
  border-bottom: 1px solid #f0e8e0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.back-btn {
  display: none;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
  padding: 4px;
}
.chat-partner {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  cursor: pointer;
}
.chat-partner:hover { color: #e8784a; }
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #faf8f5;
}
.msg-bubble-row {
  display: flex;
  justify-content: flex-start;
}
.msg-bubble-row.mine {
  justify-content: flex-end;
}
.msg-bubble {
  max-width: 70%;
  padding: 10px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f0e8e0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.msg-bubble-row.mine .msg-bubble {
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
}
.msg-content {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  text-align: right;
}
.msg-bubble-row.mine .msg-time { color: rgba(255,255,255,0.6); }
.chat-input-area {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #f0e8e0;
  background: #fff;
}
.chat-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e8e0d8;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  background: #fafafa;
}
.chat-input:focus {
  border-color: #e8784a;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(232,120,74,0.08);
}
.send-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.send-btn:hover {
  box-shadow: 0 4px 14px rgba(232,120,74,0.3);
}
.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 占位 */
.chat-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ddd;
  gap: 8px;
}
.placeholder-icon { font-size: 64px; }
.chat-placeholder p { margin: 0; font-size: 15px; color: #ccc; }

/* 空状态全页 */
.msg-empty-full {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0e8e0;
}
.empty-icon-lg { font-size: 80px; color: #eee; }
.msg-empty-full h3 { margin: 0; font-size: 18px; color: #999; }
.msg-empty-full p { margin: 0; font-size: 14px; color: #ccc; }
.go-home-btn {
  display: inline-block;
  margin-top: 8px;
  padding: 10px 28px;
  background: linear-gradient(135deg, #e8784a, #f0a060);
  color: #fff;
  border-radius: 20px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
}
.go-home-btn:hover {
  box-shadow: 0 4px 14px rgba(232,120,74,0.3);
  transform: translateY(-1px);
}

/* 通知区域 */
.notif-section {
  border-bottom: 1px solid #f0e8e0;
  max-height: 220px;
  overflow: hidden;
}
.notif-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px 6px;
}
.notif-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #e8784a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.notif-collapse-btn {
  background: none;
  border: none;
  color: #bbb;
  cursor: pointer;
  font-size: 14px;
  padding: 2px;
}
.notif-collapse-btn:hover { color: #666; }
.notif-list {
  overflow-y: auto;
  max-height: 170px;
}
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 20px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #fdfcf9;
}
.notif-item:hover { background: rgba(232,120,74,0.04); }
.notif-item.unread { background: rgba(232,120,74,0.03); }
.notif-item.unread:hover { background: rgba(232,120,74,0.06); }
.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e8784a;
  flex-shrink: 0;
  margin-top: 5px;
}
.notif-content { flex: 1; min-width: 0; }
.notif-title {
  font-size: 13px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}
.notif-desc {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notif-time {
  font-size: 10px;
  color: #ccc;
  margin-top: 3px;
}
.notif-collapsed {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  font-size: 13px;
  color: #e8784a;
  cursor: pointer;
  border-bottom: 1px solid #f0e8e0;
  transition: background 0.15s;
  font-weight: 500;
}
.notif-collapsed:hover { background: rgba(232,120,74,0.04); }

@media (max-width: 768px) {
  .conv-sidebar {
    width: 100%;
  }
  .conv-sidebar.hidden {
    display: none;
  }
  .back-btn {
    display: block;
  }
}
</style>
