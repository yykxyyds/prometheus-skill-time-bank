package com.prometheus.user.controller;

import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.user.entity.Notification;
import com.prometheus.user.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    @RequireAuth
    public Result<List<Notification>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/{id}/read")
    @RequireAuth
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    @RequireAuth
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Notification> list = notificationService.getUserNotifications(userId);
        long count = list.stream().filter(n -> n.getIsRead() == 0).count();
        return Result.success(count);
    }
}
