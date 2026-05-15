import api from './index'

export function getNotifications() {
  return api.get('/notification/list')
}

export function markNotificationRead(id) {
  return api.put(`/notification/${id}/read`)
}

export function getNotificationUnreadCount() {
  return api.get('/notification/unread-count')
}
