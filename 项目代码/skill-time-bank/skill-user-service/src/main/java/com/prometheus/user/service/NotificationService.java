package com.prometheus.user.service;

import com.prometheus.user.entity.Notification;

import java.util.List;

public interface NotificationService {

    /**
     * 获取用户通知列表
     */
    List<Notification> getUserNotifications(Long userId);

    /**
     * 发送通知
     */
    void sendNotification(Long userId, String type, String title, String content, Long targetId);

    /**
     * 标记已读
     */
    void markAsRead(Long notificationId, Long userId);
}
