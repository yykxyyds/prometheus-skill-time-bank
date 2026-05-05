import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const balance = ref(Number(localStorage.getItem('balance') || '0'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

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
    localStorage.clear()
  }

  return { token, userId, username, role, balance, isLoggedIn, isAdmin, setUser, logout }
})
