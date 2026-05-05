package com.prometheus.order.service;

import com.prometheus.order.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    List<ChatMessage> getMessages(Long orderId, Long userId);
    ChatMessage sendMessage(Long orderId, Long senderId, String content);
}
