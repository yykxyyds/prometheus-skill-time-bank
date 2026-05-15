import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg))
    }
    return data
  },
  error => {
    if (error.response?.status === 401) {
      const msg = error.response?.data?.msg || '登录已过期，请重新登录'
      ElMessage.error(msg)
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('balance')
      setTimeout(() => { window.location.href = '/login' }, 1500)
      return Promise.reject(error)
    }
    const msg = error.response?.data?.msg || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default api
