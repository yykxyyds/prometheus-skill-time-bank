package com.prometheus.order.service;

import com.prometheus.order.entity.PrivateMessage;

import java.util.List;
import java.util.Map;

public interface PrivateMessageService {

    PrivateMessage sendMessage(Long senderId, Long receiverId, String content);

    List<PrivateMessage> getMessages(Long userId, Long otherUserId);

    List<Map<String, Object>> getConversations(Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long userId, Long otherUserId);
}
