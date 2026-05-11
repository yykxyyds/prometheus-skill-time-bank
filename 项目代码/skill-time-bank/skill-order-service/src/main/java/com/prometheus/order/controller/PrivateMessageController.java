package com.prometheus.order.controller;

import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.order.entity.PrivateMessage;
import com.prometheus.order.service.PrivateMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/private")
@RequiredArgsConstructor
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @RequireAuth
    @PostMapping("/send")
    public Result<PrivateMessage> sendMessage(@RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        Long senderId = getUserId(request);
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = (String) body.get("content");
        return Result.success(privateMessageService.sendMessage(senderId, receiverId, content));
    }

    @RequireAuth
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(privateMessageService.getConversations(userId));
    }

    @RequireAuth
    @GetMapping("/messages/{otherUserId}")
    public Result<List<PrivateMessage>> getMessages(@PathVariable Long otherUserId,
                                                     HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(privateMessageService.getMessages(userId, otherUserId));
    }

    @RequireAuth
    @GetMapping("/unread")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(privateMessageService.getUnreadCount(userId));
    }

    @RequireAuth
    @PutMapping("/read/{otherUserId}")
    public Result<Void> markAsRead(@PathVariable Long otherUserId,
                                    HttpServletRequest request) {
        Long userId = getUserId(request);
        privateMessageService.markAsRead(userId, otherUserId);
        return Result.success();
    }
}
