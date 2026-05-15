import api from './index'

export function getSkillList(params) {
  return api.get('/skill/list', { params })
}

export function getSkillDetail(id) {
  return api.get(`/skill/${id}`)
}

export function publishSkill(data) {
  return api.post('/skill', data)
}

export function updateSkill(data) {
  return api.put('/skill', data)
}

export function offlineSkill(id) {
  return api.put(`/skill/${id}/offline`)
}

export function getMySkills() {
  return api.get('/skill/my')
}

export function getCategories() {
  return api.get('/category/list')
}

export function getBountyList(params) {
  return api.get('/bounty/list', { params })
}

export function publishBounty(data) {
  return api.post('/bounty', data)
}

export function updateBounty(id, data) {
  return api.put(`/bounty/${id}`, data)
}

export function deleteBounty(id) {
  return api.delete(`/bounty/${id}`)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
