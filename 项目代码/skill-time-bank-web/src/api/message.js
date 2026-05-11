import api from './index'

export function sendMessage(data) {
  return api.post('/chat/private/send', data)
}

export function getConversations() {
  return api.get('/chat/private/conversations')
}

export function getPrivateMessages(otherUserId) {
  return api.get(`/chat/private/messages/${otherUserId}`)
}

export function getUnreadCount() {
  return api.get('/chat/private/unread')
}

export function markAsRead(otherUserId) {
  return api.put(`/chat/private/read/${otherUserId}`)
}
