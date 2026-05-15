import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value.role || '')
  const isAdmin = computed(() => role.value === 'ADMIN')

  function setLogin(data) {
    token.value = data.token
    userInfo.value = { userId: data.userId, username: data.username, role: data.role, balance: data.balance }
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return { token, userInfo, isLoggedIn, role, isAdmin, setLogin, logout }
})
