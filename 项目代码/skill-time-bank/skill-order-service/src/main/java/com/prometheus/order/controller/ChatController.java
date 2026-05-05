package com.prometheus.order.controller;

import com.prometheus.common.Result;
import com.prometheus.order.entity.ChatMessage;
import com.prometheus.order.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping("/order/{orderId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long orderId,
                                                  HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(chatService.getMessages(orderId, userId));
    }

    @PostMapping("/order/{orderId}")
    public Result<ChatMessage> sendMessage(@PathVariable Long orderId,
                                            @RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        Long senderId = getUserId(request);
        String content = body.get("content");
        return Result.success(chatService.sendMessage(orderId, senderId, content));
    }
}
