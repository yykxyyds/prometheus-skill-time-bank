import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/index'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const balance = ref(Number(localStorage.getItem('balance') || '0'))
  const unreadCount = ref(0)
  const notifUnreadCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)

  async function refreshUnread() {
    if (!token.value) { unreadCount.value = 0; notifUnreadCount.value = 0; return }
    try {
      const [msgRes, notifRes] = await Promise.all([
        api.get('/chat/private/unread'),
        api.get('/notification/unread-count')
      ])
      unreadCount.value = (msgRes.data || 0)
      notifUnreadCount.value = (notifRes.data || 0)
    } catch { /* silent */ }
  }

function setUser(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    role.value = data.role
    balance.value = data.balance || 0
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
    localStorage.setItem('balance', data.balance || 0)
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    balance.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('balance')
  }

  return { token, userId, username, role, balance, unreadCount, notifUnreadCount, isLoggedIn, setUser, logout, refreshUnread }
})
