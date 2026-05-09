import api from './index'

export function register(data) {
  return api.post('/user/register', data)
}

export function login(data) {
  return api.post('/user/login', data)
}

export function getProfile() {
  return api.get('/user/profile')
}

export function updateProfile(data) {
  return api.put('/user/profile', data)
}

export function getUserProfile(userId) {
  return api.get(`/user/${userId}/profile`)
}

export function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return api.post('/upload/avatar', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
