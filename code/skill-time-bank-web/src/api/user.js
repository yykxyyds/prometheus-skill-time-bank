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

export function followUser(targetId) {
  return api.post(`/user/follow/${targetId}`)
}

export function unfollowUser(targetId) {
  return api.delete(`/user/follow/${targetId}`)
}

export function getFollowStatus(targetId) {
  return api.get(`/user/follow/${targetId}/status`)
}

export function getFriends() {
  return api.get('/user/friends')
}

export function getFollowers(userId) {
  return api.get(`/user/${userId}/followers`)
}

export function getFollowing(userId) {
  return api.get(`/user/${userId}/following`)
}

export function getReputation(userId) {
  return api.get(`/review/reputation/${userId}`)
}
